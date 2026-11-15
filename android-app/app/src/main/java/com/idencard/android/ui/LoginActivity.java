package com.idencard.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.idencard.android.R;
import com.idencard.android.auth.AuthService;

public class LoginActivity extends AppCompatActivity {

    private AuthService authService;
    private TextView tvStatus;
    private Button btnLoginSso;
    private Button btnQuickDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();
        tvStatus = findViewById(R.id.tvStatus);
        btnLoginSso = findViewById(R.id.btnLoginSso);
        btnQuickDemo = findViewById(R.id.btnQuickDemoLogin);

        btnLoginSso.setOnClickListener(v -> startSsoLoginFlow());
        btnQuickDemo.setOnClickListener(v -> startDemoSession());

        handleRedirectIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleRedirectIntent(intent);
    }

    private void startSsoLoginFlow() {
        tvStatus.setText("Đang mở trình duyệt xác thực SSO...");
        Uri authUri = authService.buildAuthorizationUri();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, authUri);
        startActivity(browserIntent);
    }

    private void handleRedirectIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.toString().startsWith("com.idencard.android://oauth2redirect")) {
            String code = data.getQueryParameter("code");
            String error = data.getQueryParameter("error");

            if (error != null) {
                tvStatus.setText("Lỗi đăng nhập SSO: " + error);
                Toast.makeText(this, "Lỗi đăng nhập: " + error, Toast.LENGTH_LONG).show();
                return;
            }

            if (code != null) {
                tvStatus.setText("Đang trao đổi mã ủy quyền lấy Token...");
                authService.exchangeCodeForToken(code, new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(String accessToken, String idToken) {
                        tvStatus.setText("Đăng nhập SSO thành công!");
                        navigateToCardMain(accessToken, "cardholder");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        tvStatus.setText("Lỗi: " + errorMessage);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void startDemoSession() {
        // Quick bypass for immediate local simulator testing
        navigateToCardMain("demo-mock-jwt-token-cardholder", "Nguyễn Văn A (cardholder)");
    }

    private void navigateToCardMain(String token, String username) {
        Intent intent = new Intent(this, CardMainActivity.class);
        intent.putExtra("EXTRA_ACCESS_TOKEN", token);
        intent.putExtra("EXTRA_USERNAME", username);
        startActivity(intent);
        finish();
    }
}
