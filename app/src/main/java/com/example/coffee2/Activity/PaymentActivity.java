package com.example.coffee2.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.example.coffee2.Helper.qr.QrService;
import com.example.coffee2.databinding.ActivityPaymentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentActivity extends BaseActivity {

    private ActivityPaymentBinding binding;
    private DatabaseReference dbRef;
    private String billId;
    private double total;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbRef = FirebaseDatabase.getInstance().getReference();

        billId = getIntent().getStringExtra("billId");
        total = getIntent().getDoubleExtra("total", 0.0);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String amount = String.format("%.2f", total);
        String content = billId + "_" + userId + "_" + amount;
        String bankNumber = "3200999999";

        int imageWidth = 300;
        int imageHeight = 300;

        Bitmap bitmap = QrService.convertBase64ToBitmap(content, amount, bankNumber, imageWidth, imageHeight);

        binding.imgQr.setImageBitmap(bitmap);

        startCountdown();

        binding.backBtn3.setOnClickListener(v -> {
            updateBillStatus("Hủy");
            Toast.makeText(PaymentActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void startCountdown() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvCountdown.setText("Còn lại: " + millisUntilFinished / 1000 + " giây");
            }

            @Override
            public void onFinish() {
                updateBillStatus("Không thành công");
                Toast.makeText(PaymentActivity.this, "Thời gian thanh toán đã hết", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void updateBillStatus(String status) {
        dbRef.child("Bill").child(billId).child("orderStatus").setValue(status);
    }
}
