package com.example.coffee2.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.coffee2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private AppCompatButton resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các View từ layout
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Xử lý sự kiện nhấn nút reset mật khẩu
        resetPasswordButton.setOnClickListener(v -> resetPassword());

        // Xử lý sự kiện nhấn nút quay lại
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showDialog("Vui lòng điền đầy đủ thông tin", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showDialog("Mật khẩu mới không khớp", false);
            return;
        }

        if (newPassword.length() < 6) {
            showDialog("Mật khẩu mới phải có ít nhất 6 ký tự", false);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showDialog("Người dùng chưa đăng nhập", false);
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                showDialog("Đổi mật khẩu thành công", true);
                            } else {
                                showDialog("Đổi mật khẩu thất bại. Vui lòng thử lại sau", false);
                            }
                        });
            } else {
                showDialog("Mật khẩu hiện tại không chính xác", false);
            }
        });
    }


    // Hàm hiển thị AlertDialog
    private void showDialog(String message,boolean shouldFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false) // Không cho phép bấm ra ngoài để đóng
                .setPositiveButton("OK", (dialog, id) -> {
                    dialog.dismiss(); // Chờ người dùng bấm OK mới đóng
                    if (shouldFinish) {
                        finish(); // Chỉ đóng activity nếu được yêu cầu
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
