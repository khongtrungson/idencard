package com.idencard.android.auth;

import android.net.Uri;

public final class OidcAuthConfig {

    private OidcAuthConfig() {}

    // Android emulator alias for host machine localhost:9000
    public static final String ISSUER_URI = "http://10.0.2.2:9000";
    public static final String AUTHORIZE_ENDPOINT = ISSUER_URI + "/oauth2/authorize";
    public static final String TOKEN_ENDPOINT = ISSUER_URI + "/oauth2/token";
    public static final String USERINFO_ENDPOINT = ISSUER_URI + "/userinfo";

    public static final String CLIENT_ID = "idencard-android";
    public static final String REDIRECT_URI = "com.idencard.android://oauth2redirect";
    public static final String SCOPE = "openid profile email card:read card:write";

    public static Uri getAuthorizeUri() {
        return Uri.parse(AUTHORIZE_ENDPOINT);
    }

    public static Uri getTokenUri() {
        return Uri.parse(TOKEN_ENDPOINT);
    }

    public static Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }
}
