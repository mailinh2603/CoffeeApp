package com.example.coffee2.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.example.coffee2.Domain.users;
import com.example.coffee2.Helper.qr.QrService;
import com.example.coffee2.databinding.ActivityPaymentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends BaseActivity {

    private ActivityPaymentBinding binding;
    private DatabaseReference dbRef;
    private String billId;
    private double total;
    private String userId; // Biến global để lưu userId tìm được

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbRef = FirebaseDatabase.getInstance().getReference();

        billId = getIntent().getStringExtra("billId");
        total = getIntent().getDoubleExtra("total", 0.0);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Tìm userId theo email trong bảng "users"
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            users user = userSnap.getValue(users.class);
                            if (user != null && user.getEmail().equals(userEmail)) {
                                userId = userSnap.getKey();  // Lấy userId
                                Log.d("PaymentActivity", "userId tìm được theo email: " + userId);
                                generateQRCode(userId);
                                break;
                            }
                        }
                        if (userId == null) {
                            Toast.makeText(PaymentActivity.this, "Không tìm thấy userId với email", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(PaymentActivity.this, "Lỗi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });

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

    private void generateQRCode(String userIdFromDb) {
        // Format tiền VNĐ, chuyển sang chuỗi số thuần (bỏ dấu chấm, ký tự ₫ và mọi loại khoảng trắng)
        String amountFormatted = formatVND(total)
                .replaceAll("[₫]", "")
                .replaceAll("\\.", "")
                .replaceAll("\\s+", "")  // loại bỏ mọi khoảng trắng (space, tab, non-breaking space...)
                .trim();

        String content = billId + "_" + userIdFromDb + "_" + amountFormatted;
        String bankNumber = "9912031106";
        Log.d("PaymentActivity", "QR content: " + content);
        Log.d("PaymentActivity", "QR content: " + amountFormatted);
        int imageWidth = 300;
        int imageHeight = 300;

        Bitmap bitmap = QrService.convertBase64ToBitmap(content, amountFormatted, bankNumber, imageWidth, imageHeight);

        binding.imgQr.setImageBitmap(bitmap);
    }


    private String formatVND(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        return currencyFormatter.format(amount);
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
