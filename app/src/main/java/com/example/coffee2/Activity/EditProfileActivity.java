package com.example.coffee2.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffee2.Domain.users;
import com.example.coffee2.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private EditText fullNameEditText, emailEditText, addressEditText, PhoneNumberEditText, BirthDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fullNameEditText = binding.editUserName;
        emailEditText = binding.editEmail;
        addressEditText = binding.editAddress;
        PhoneNumberEditText = binding.editPhoneNumber;
        BirthDateEditText = binding.editBirthDate;

        // Lấy userId từ Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("EditProfile", "UserId nhận được: " + userId);

        // Lấy thông tin người dùng từ Firebase
        getUserData(userId);

        binding.btnSave.setOnClickListener(v -> updateUserData(userId));
        BirthDateEditText.setOnClickListener(v -> showDatePickerDialog());
        binding.backBtn.setOnClickListener(v -> finish());
    }

    // Hàm lấy thông tin người dùng từ Firebase
    private void getUserData(String userId) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            users user = snapshot.getValue(users.class);
                            if (user != null) {
                                fullNameEditText.setText(user.getUserName());
                                emailEditText.setText(user.getEmail());
                                addressEditText.setText(user.getAddress());
                                PhoneNumberEditText.setText(user.getPhoneNumber());
                                BirthDateEditText.setText(user.getBirthDate());
                            }
                        } else {
                            showDialog("Không tìm thấy thông tin người dùng.");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showDialog("Lỗi truy vấn dữ liệu: " + error.getMessage());
                    }
                });
    }


    // Hàm cập nhật thông tin người dùng
    private void updateUserData(String userId) {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phoneNumber = PhoneNumberEditText.getText().toString().trim();
        String birthDate = BirthDateEditText.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            showDialog("Vui lòng nhập đầy đủ họ tên và email.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("UserName", fullName);
        updates.put("Email", email);
        updates.put("Address", address);
        updates.put("PhoneNumber", phoneNumber);
        updates.put("BirthDate", birthDate);

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showDialog("Cập nhật thông tin thành công!");
                    } else {
                        showDialog("Cập nhật thất bại. Vui lòng thử lại.");
                    }
                });
    }


    // Hiển thị dialog chọn ngày
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, yearSelected, monthOfYear, dayOfMonth) -> {
            String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, yearSelected);
            BirthDateEditText.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Hiển thị thông báo lỗi hoặc thành công
    private void showDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
