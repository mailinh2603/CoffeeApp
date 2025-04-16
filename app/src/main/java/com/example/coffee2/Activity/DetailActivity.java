package com.example.coffee2.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coffee2.Adapter.CommentAdapter;
import com.example.coffee2.Domain.Comment;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.Domain.users;
import com.example.coffee2.Helper.ManagmentCart;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends BaseActivity {
    ActivityDetailBinding binding;
    private Drinks object;
    private int num=1;
    private ManagmentCart managmentCart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        getIntentExtra();
        setVariable();
        loadSugarOptions();
        loadIceOptions();
         if (object != null ) {
             loadComments(object.getId());
         } else {
             Toast.makeText(this, "Không thể lấy ID của sản phẩm", Toast.LENGTH_SHORT).show();
         }
    }

    private void loadSugarOptions() {
        RadioGroup radioGroup = findViewById(R.id.sugarRadioGroup);
        radioGroup.removeAllViews();

        String[] sugarOptions = {"0%", "30%", "50%", "100%"};
        for (String option : sugarOptions) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTag(option);
            if (option.equals("100%")) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            String selectedSugar = (String) selectedRadioButton.getTag();
            Toast.makeText(this, "Selected Sugar: " + selectedSugar, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadIceOptions() {
        RadioGroup radioGroup = findViewById(R.id.iceRadioGroup);
        radioGroup.removeAllViews();

        String[] iceOptions = {"0%", "30%", "50%", "100%"};
        for (String option : iceOptions) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTag(option);
            if (option.equals("100%")) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            String selectedIce = (String) selectedRadioButton.getTag();
            Toast.makeText(this, "Selected Ice: " + selectedIce, Toast.LENGTH_SHORT).show();
        });
    }

    private String getSelectedSugar() {
        RadioGroup radioGroup = findViewById(R.id.sugarRadioGroup);
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId);
            return (String) selectedRadioButton.getTag();
        }
        return "";
    }

    private String getSelectedIce() {
        RadioGroup radioGroup = findViewById(R.id.iceRadioGroup);
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId);
            return (String) selectedRadioButton.getTag();
        }
        return "";
    }

    private void loadComments(int drinkId) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comment");

        commentRef.orderByChild("DrinkId").equalTo(drinkId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Comment> commentList = new ArrayList<>();
                        List<DataSnapshot> commentSnapshots = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            commentSnapshots.add(data);
                        }

                        if (commentSnapshots.isEmpty()) {
                            updateCommentList(commentList);
                            return;
                        }

                        int[] loadedCount = {0};  // Biến đếm để biết khi nào load xong hết user

                        for (DataSnapshot data : commentSnapshots) {
                            Comment comment = data.getValue(Comment.class);
                            if (comment != null && comment.isActive()) {
                                String userId = comment.getUserId();

                                // Truy vấn theo field "UserId" vì bạn không dùng userId làm key
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

                                userRef.orderByChild("UserId").equalTo(userId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                                                        users user = userSnap.getValue(users.class);
                                                        if (user != null) {
                                                            comment.setUserName(user.getUserName());
                                                            Log.d("Adapter", "Hiển thị comment của: " + user.getUserName());
                                                        }
                                                    }
                                                } else {
                                                    Log.w("Linh", "Không tìm thấy user trong Users cho userId: " + userId);
                                                    comment.setUserName("Ẩn danh");
                                                }

                                                commentList.add(comment);
                                                loadedCount[0]++;
                                                if (loadedCount[0] == commentSnapshots.size()) {
                                                    Log.d("Linh", "Tải xong tất cả comments: " + commentList.size());
                                                    updateCommentList(commentList);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("Linh", "Lỗi khi tải thông tin người dùng: " + error.getMessage());
                                            }
                                        });
                            } else {
                                loadedCount[0]++;
                                if (loadedCount[0] == commentSnapshots.size()) {
                                    updateCommentList(commentList);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailActivity.this, "Lỗi khi tải bình luận", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCommentList(List<Comment> commentList) {
        RecyclerView recyclerView = findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter adapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(adapter);  // Đảm bảo được gán tại đây
    }

    private void setVariable(){
        managmentCart= new ManagmentCart(this);

        binding.backBtn.setOnClickListener(v -> finish());

        Glide.with(DetailActivity.this)
                .load(object.getImagePath())
                .into(binding.pic);

        binding.priceTxt.setText(object.getPrice() + "đ");
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.rateTxt.setText(object.getStar() + "Rating");
        binding.ratingBar.setRating((float) object.getStar());
        binding.totalTxt.setText((num * object.getPrice() + "đ"));

        binding.plusBtn.setOnClickListener(v ->{
            num=num+1;
            binding.numxt.setText(num+" ");
            binding.totalTxt.setText((num* object.getPrice())+" đ");
        });

        binding.minusBtn.setOnClickListener(v -> {
            if(num>1){
                num=num-1;
                binding.numxt.setText(num+"");
                binding.totalTxt.setText((num* object.getPrice())+" đ");
            }
        });

        binding.addBtn.setOnClickListener(v -> {
            String selectedSugar = getSelectedSugar();
            String selectedIce = getSelectedIce();

            object.setSugarOption(selectedSugar);
            object.setIceOption(selectedIce);
            object.setNumberInCart(num);
            managmentCart.insertFood(object);
        });

    }

    private void getIntentExtra(){
        object = (Drinks) getIntent().getSerializableExtra("object");
        if (object == null ) {
            Toast.makeText(this, "Không thể lấy ID của sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }
}