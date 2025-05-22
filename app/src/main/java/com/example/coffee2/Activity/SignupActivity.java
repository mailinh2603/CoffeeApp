package com.example.coffee2.Activity;

import android.app.DatePickerDialog;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

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
        binding.birthDateEdt.setOnClickListener(v -> showDatePickerDialog());
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignupActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    binding.birthDateEdt.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void generateAndStoreUser(users newUser) {
        String userId = generateCustomUserId();
        newUser.setUserId(userId);
        DatabaseReference userRef = mDatabase.getReference("users").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    generateAndStoreUser(newUser);
                } else {
                    userRef.setValue(newUser).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Log.i("SignupActivity", "User added with ID: " + userId);
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(SignupActivity.this, "Lỗi khi kiểm tra userId", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(v -> {
            String email = binding.userEdt.getText().toString();
            String password = binding.passEdt.getText().toString();
            String userName = binding.userNameEdt.getText().toString();
            String address = binding.addressEdt.getText().toString();
            String birthDate = binding.birthDateEdt.getText().toString();
            String phoneNumber = binding.phoneNumberEdt.getText().toString();


            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                if (task.isSuccessful()) {
                    users newUser = new users(userName, email, address, birthDate, phoneNumber,"user",System.currentTimeMillis() );
                    generateAndStoreUser(newUser);
                } else {
                    Exception e = task.getException();
                    String message = "Đăng ký thất bại!";

                    if (e != null) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            message = "Email đã được sử dụng!";
                            binding.userEdt.setError("Email đã tồn tại!");
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            message = "Mật khẩu quá yếu. Phải có ít nhất 6 ký tự.";
                            binding.passEdt.setError("Mật khẩu quá yếu!");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Email không hợp lệ!";
                            binding.userEdt.setError("Email không hợp lệ!");
                        } else {
                            message = "Lỗi: " + e.getMessage();
                        }
                    }

                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String generateCustomUserId() {
        int random = (int) (Math.random() * 900000) + 100000;
        return "ID" + random;
    }

}
