package com.example.coffee2.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends BaseActivity {
    ActivityDetailBinding binding;
    private Drinks object;
    private int num=1;
    private ManagmentCart managmentCart;
    private RatingBar userRatingBar;
    private EditText reviewEditText;
    private Button submitReviewBtn;

    private LinearLayout paginationLayout;
    private TextView tvCurrentPage;
    private Button btnPrevious, btnNext;
    private int currentPage = 1;        // Trang hiện tại
    private int commentsPerPage = 5;    // Số bình luận trên mỗi trang
    private int totalComments = 0;      // Tổng số bình luận
    private int totalPages = 0;
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
        comment();

        paginationLayout = findViewById(R.id.paginationLayout);
        tvCurrentPage = findViewById(R.id.tvCurrentPage);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        updatePagination();
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadComments(object.getId());
                updatePagination();
            }
        });

        // Khi nhấn nút "Next"
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadComments(object.getId());
                updatePagination();
            }
        });

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
        Query query = commentRef.orderByChild("DrinkId").equalTo(drinkId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> allComments = new ArrayList<>();

                // Lọc các bình luận đang hoạt động
                for (DataSnapshot data : snapshot.getChildren()) {
                    Comment comment = data.getValue(Comment.class);
                    if (comment != null && comment.isActive()) {
                        allComments.add(comment);
                    }
                }

                // Tính tổng số bình luận và số trang
                totalComments = allComments.size();
                calculateTotalPages();

                // Kiểm tra nếu không có bình luận nào
                if (totalComments == 0) {
                    updateCommentList(new ArrayList<>()); // Cập nhật UI với danh sách rỗng
                    updatePagination();
                    return;
                }

                // Đảm bảo currentPage là hợp lệ
                if (currentPage < 1) {
                    currentPage = 1;
                }

                // Tính chỉ số bắt đầu và kết thúc
                int startIndex = (currentPage - 1) * commentsPerPage;

                // Nếu startIndex vượt quá số bình luận, quay lại trang đầu
                if (startIndex >= totalComments) {
                    currentPage = 1;
                    startIndex = 0;
                }

                int endIndex = Math.min(startIndex + commentsPerPage, totalComments);

                // Nếu startIndex >= endIndex, có thể do currentPage không hợp lệ
                if (startIndex >= endIndex) {
                    updateCommentList(new ArrayList<>());
                    updatePagination();
                    return;
                }

                List<Comment> currentComments = allComments.subList(startIndex, endIndex);
                List<Comment> finalCommentList = new ArrayList<>();
                int[] loadedCount = {0};

                for (Comment comment : currentComments) {
                    String userId = comment.getUserId();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                users user = snapshot.getValue(users.class);
                                if (user != null) {
                                    comment.setUserName(user.getUserName());
                                }
                            } else {
                                comment.setUserName("Ẩn danh");
                            }

                            finalCommentList.add(comment);
                            loadedCount[0]++;

                            if (loadedCount[0] == currentComments.size()) {
                                updateCommentList(finalCommentList);  // Cập nhật danh sách UI
                                updatePagination();                   // Cập nhật phân trang
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Linh", "Lỗi khi tải người dùng: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Lỗi khi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void calculateTotalPages() {
        totalPages = (int) Math.ceil((double) totalComments / commentsPerPage);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
    }





    private void updatePagination() {
        paginationLayout.removeAllViews();  // Xóa các nút cũ

        // Ẩn/hiện nút Previous và Next
        btnPrevious.setVisibility(currentPage > 1 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(currentPage < totalPages ? View.VISIBLE : View.GONE);

        // Tạo nút phân trang
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(this);
            pageButton.setText(String.valueOf(i));
            if (i == currentPage) {
                pageButton.setEnabled(false); // Trang hiện tại không bấm được
            }
            int finalI = i;
            pageButton.setOnClickListener(v -> {
                currentPage = finalI;
                loadComments(object.getId());
            });
            paginationLayout.addView(pageButton);
        }

        // Hiển thị số trang hiện tại
        tvCurrentPage.setText("Trang " + currentPage + " / " + totalPages);
    }


    private void updateCommentList(List<Comment> commentList) {
        RecyclerView recyclerView = findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter adapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(adapter);
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

    private void comment() {
        binding.submitReviewBtn.setOnClickListener(v -> {
            String commentDetail = binding.reviewEditText.getText().toString().trim();
            float rating = binding.userRatingBar.getRating();
            if (TextUtils.isEmpty(commentDetail)) {
                Toast.makeText(DetailActivity.this, "Vui lòng nhập bình luận!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating == 0) {
                Toast.makeText(DetailActivity.this, "Vui lòng chọn đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = (currentUser != null) ? currentUser.getUid() : "anonymous";
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("userName").getValue(String.class);
                        if (userName == null || userName.isEmpty()) {
                            userName = "Người dùng ẩn danh";
                        }
                   /*     Log.d("Comment", "User ID: " + userId);
                        Log.d("Comment", "User Name: " + userName);
                        Log.d("Comment", "Comment Detail: " + commentDetail);
                        Log.d("Comment", "Rating: " + rating); */

                        int drinkId = object.getId();
                        Comment comment = new Comment();
                        comment.setCommentDetail(commentDetail);
                        comment.setRating((int) rating);
                        comment.setUserId(userId);
                        comment.setUserName(userName);
                        comment.setActive(true);
                        comment.setCublish(String.valueOf(System.currentTimeMillis()));
                        comment.setDrinkId(drinkId);

                        submitReviewAndUpdateAverageRating(comment, drinkId);
                    } else {
                        Toast.makeText(DetailActivity.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DetailActivity.this, "Có lỗi khi lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                    //     Log.e("Comment", "Error fetching user info: " + databaseError.getMessage());
                }
            });
        });
    }

    private void submitReviewAndUpdateAverageRating(Comment comment, int drinkId) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comment");
        String commentId = commentRef.push().getKey();
        if (commentId != null) {
            comment.setCommentId(commentId);
            commentRef.child(commentId).setValue(comment)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            binding.userRatingBar.setRating(0);
                            binding.reviewEditText.setText("");
                            updateAverageRating(drinkId);
                            loadComments(object.getId());
                        } else {
                            Toast.makeText(DetailActivity.this, "Có lỗi khi gửi đánh giá!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateAverageRating(int drinkId) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comment");
        commentRef.orderByChild("DrinkId").equalTo(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRating = 0;
                int commentCount = 0;

                // Tính tổng số rating và số lượng comment
                for (DataSnapshot data : snapshot.getChildren()) {
                    Comment comment = data.getValue(Comment.class);
                    if (comment != null && comment.isActive()) {
                        totalRating += comment.getRating();
                        commentCount++;
                    }
                }

                if (commentCount > 0) {
                    // Tính trung bình rating
                    float averageRating = (float) totalRating / commentCount;

                    // Cập nhật giá trị trung bình vào trường 'Star' của Drink
                    DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("Drinks").child(String.valueOf(drinkId));
                    drinkRef.child("Star").setValue(averageRating).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Drink", "Cập nhật trung bình rating thành công cho Drink ID: " + drinkId);

                            // Cập nhật rateTxt với giá trị Star mới
                            updateRateTxt(drinkId);
                        } else {
                            Log.e("Drink", "Cập nhật trung bình rating thất bại cho Drink ID: " + drinkId);
                        }
                    });
                } else {
                    Log.w("Drink", "Không có bình luận nào để tính toán trung bình rating cho Drink ID: " + drinkId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Drink", "Lỗi khi truy vấn bình luận: " + error.getMessage());
            }
        });
    }

    private void updateRateTxt(int drinkId) {
        DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("Drinks").child(String.valueOf(drinkId));
        drinkRef.child("Star").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float star = snapshot.getValue(Float.class);
                    if (star != 0.0f) {
                        // Làm tròn đến 1 chữ số thập phân
                        String roundedStar = String.format("%.1f", star);
                        // Cập nhật rateTxt với giá trị của Star từ Firebase
                        binding.rateTxt.setText(roundedStar + " Rating");
                        Log.d("Drink", "Cập nhật rateTxt với giá trị: " + roundedStar);
                        Log.d("Drink", "Cập nhật rateTxt với giá trị: " + roundedStar);
                        binding.ratingBar.setRating(star);
                    } else {
                        Log.w("Drink", "Không tìm thấy trường Star cho Drink ID: " + drinkId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Drink", "Lỗi khi truy vấn giá trị Star cho Drink ID: " + drinkId);
            }
        });
    }



    private void getIntentExtra(){
        object = (Drinks) getIntent().getSerializableExtra("object");
        if (object == null ) {
            Toast.makeText(this, "Không thể lấy ID của sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }
}