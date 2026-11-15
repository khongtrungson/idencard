package com.idencard.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.idencard.android.R;
import com.idencard.android.card.CardTransceiver;
import com.idencard.android.card.SocketCardTransceiver;
import com.idencard.android.domain.CardInfo;
import com.idencard.android.domain.CardOperationsService;
import com.idencard.protocol.ApduResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardMainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CardTransceiver transceiver;
    private CardOperationsService cardService;

    private TextView tvUserGreeting;
    private TextView tvCardStatus;
    private TextView tvConsoleLog;
    private EditText etPin;
    private Button btnConnectSimulator;
    private Button btnConnectNfc;
    private Button btnReadCardInfo;
    private Button btnVerifyPin;
    private Button btnGetIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_main);

        bindViews();
        setupUserData();
        setupListeners();
    }

    private void bindViews() {
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        tvCardStatus = findViewById(R.id.tvCardStatus);
        tvConsoleLog = findViewById(R.id.tvConsoleLog);
        etPin = findViewById(R.id.etPin);
        btnConnectSimulator = findViewById(R.id.btnConnectSimulator);
        btnConnectNfc = findViewById(R.id.btnConnectNfc);
        btnReadCardInfo = findViewById(R.id.btnReadCardInfo);
        btnVerifyPin = findViewById(R.id.btnVerifyPin);
        btnGetIdentity = findViewById(R.id.btnGetIdentity);
    }

    private void setupUserData() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("EXTRA_USERNAME");
        if (username != null) {
            tvUserGreeting.setText("Xin chào, " + username + " (SSO Authenticated)");
        }
    }

    private void setupListeners() {
        btnConnectSimulator.setOnClickListener(v -> connectSimulator());
        btnConnectNfc.setOnClickListener(v -> setupNfc());
        btnReadCardInfo.setOnClickListener(v -> readCardInfo());
        btnVerifyPin.setOnClickListener(v -> verifyPin());
        btnGetIdentity.setOnClickListener(v -> getIdentity());
    }

    private void connectSimulator() {
        log("Đang kết nối tới JavaCard Simulator (Socket: 10.0.2.2:9025)...");
        executor.execute(() -> {
            try {
                transceiver = new SocketCardTransceiver("10.0.2.2", 9025);
                transceiver.connect();
                cardService = new CardOperationsService(transceiver);

                // Auto Select Applet upon connect
                ApduResponse selectResp = cardService.selectApplet();

                mainHandler.post(() -> {
                    tvCardStatus.setText("Trạng thái: Đã kết nối " + transceiver.getTransceiverName());
                    tvCardStatus.setTextColor(getResources().getColor(R.color.success));
                    enableCardButtons(true);
                    log("Đã kết nối Socket! Lệnh SELECT Applet trả về SW: " + String.format("0x%04X", selectResp.getStatusWord()));
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    tvCardStatus.setText("Lỗi kết nối Socket Simulator: " + e.getMessage());
                    tvCardStatus.setTextColor(getResources().getColor(R.color.error));
                    log("[LỖI] Không thể mở socket: " + e.getMessage());
                });
            }
        });
    }

    private void setupNfc() {
        log("Chế độ NFC: Sẵn sàng chạm thẻ chip vào mặt lưng thiết bị.");
        Toast.makeText(this, "Hãy đưa thẻ JavaCard vào vùng đọc NFC của máy", Toast.LENGTH_SHORT).show();
    }

    private void readCardInfo() {
        if (cardService == null) return;
        executor.execute(() -> {
            try {
                CardInfo info = cardService.readCardInfo();
                mainHandler.post(() -> {
                    log("-> Thông tin thẻ:\n   + Serial: " + info.getSerialNumber()
                            + "\n   + Số lần thử PIN còn lại: " + info.getPinTriesRemaining()
                            + "\n   + Đã xác thực PIN: " + info.isPinValidated());
                });
            } catch (IOException e) {
                mainHandler.post(() -> log("[LỖI] Đọc thông tin thẻ: " + e.getMessage()));
            }
        });
    }

    private void verifyPin() {
        if (cardService == null) return;
        String pin = etPin.getText().toString().trim();
        if (pin.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                boolean ok = cardService.verifyPin(pin);
                mainHandler.post(() -> {
                    if (ok) {
                        log("-> Xác thực mã PIN THÀNH CÔNG! (SW: 0x9000)");
                        Toast.makeText(this, "Xác thực PIN thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        log("-> Xác thực mã PIN THẤT BẠI!");
                        Toast.makeText(this, "Sai mã PIN!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> log("[LỖI] Xác thực PIN: " + e.getMessage()));
            }
        });
    }

    private void getIdentity() {
        if (cardService == null) return;
        executor.execute(() -> {
            try {
                String identity = cardService.getIdentity();
                mainHandler.post(() -> log("-> Dữ liệu Định Danh Thẻ: [" + identity + "]"));
            } catch (IOException e) {
                mainHandler.post(() -> log("[LỖI] Lấy định danh (Cần verify PIN trước): " + e.getMessage()));
            }
        });
    }

    private void enableCardButtons(boolean enabled) {
        btnReadCardInfo.setEnabled(enabled);
        btnVerifyPin.setEnabled(enabled);
        btnGetIdentity.setEnabled(enabled);
    }

    private void log(String message) {
        tvConsoleLog.append(message + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transceiver != null) {
            transceiver.disconnect();
        }
        executor.shutdown();
    }
}
