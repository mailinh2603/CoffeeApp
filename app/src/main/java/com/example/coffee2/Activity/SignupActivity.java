package com.example.coffee2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffee2.Domain.users;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        TextView loginNowTxt = findViewById(R.id.loginNowTxt);
        loginNowTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        setVariable();
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(v -> {
            String email = binding.userEdt.getText().toString();
            String password = binding.passEdt.getText().toString();
            String userName = binding.userNameEdt.getText().toString();
            String address = binding.addressEdt.getText().toString();
            String birthDate = binding.birthDateEdt.getText().toString();
            String phoneNumber = binding.phoneNumberEdt.getText().toString();

            // Kiểm tra mật khẩu có ít nhất 6 ký tự
            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng ký tài khoản Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                if (task.isSuccessful()) {
                    // Lấy userId từ Firebase Authentication
                    String userId = mAuth.getCurrentUser().getUid();

                    // Tạo đối tượng người dùng
                    users newUser = new users(userName, email, address, birthDate, phoneNumber);

                    // Ghi vào Realtime Database với key là userId (không dùng push())
                    DatabaseReference userRef = mDatabase.getReference("users").child(userId);

                    userRef.setValue(newUser).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Log.i("SignupActivity", "User added with Auth ID as key");
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Log.e("SignupActivity", "Error adding user", dbTask.getException());
                            Toast.makeText(SignupActivity.this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.i("SignupActivity", "failure:" + task.getException());
                    Toast.makeText(SignupActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }
}
