package com.idencard.android.auth;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthService {

    private final OkHttpClient httpClient;
    private final Handler mainHandler;
    private String currentCodeVerifier;

    public interface AuthCallback {
        void onSuccess(String accessToken, String idToken);
        void onError(String errorMessage);
    }

    public AuthService() {
        this.httpClient = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public Uri buildAuthorizationUri() {
        this.currentCodeVerifier = PkceHelper.generateCodeVerifier();
        String codeChallenge = PkceHelper.generateCodeChallenge(currentCodeVerifier);

        return Uri.parse(OidcAuthConfig.AUTHORIZE_ENDPOINT).buildUpon()
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", OidcAuthConfig.CLIENT_ID)
                .appendQueryParameter("redirect_uri", OidcAuthConfig.REDIRECT_URI)
                .appendQueryParameter("scope", OidcAuthConfig.SCOPE)
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("code_challenge_method", "S256")
                .build();
    }

    public void exchangeCodeForToken(String authCode, AuthCallback callback) {
        if (currentCodeVerifier == null) {
            callback.onError("Missing code_verifier for PKCE");
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", OidcAuthConfig.CLIENT_ID)
                .add("code", authCode)
                .add("redirect_uri", OidcAuthConfig.REDIRECT_URI)
                .add("code_verifier", currentCodeVerifier)
                .build();

        Request request = new Request.Builder()
                .url(OidcAuthConfig.TOKEN_ENDPOINT)
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onError("Token exchange failed HTTP " + response.code()));
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                try {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String accessToken = json.has("access_token") ? json.get("access_token").getAsString() : "";
                    String idToken = json.has("id_token") ? json.get("id_token").getAsString() : "";
                    mainHandler.post(() -> callback.onSuccess(accessToken, idToken));
                } catch (Exception ex) {
                    mainHandler.post(() -> callback.onError("Failed to parse token response: " + ex.getMessage()));
                }
            }
        });
    }
}
