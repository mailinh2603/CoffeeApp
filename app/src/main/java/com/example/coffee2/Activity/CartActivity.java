package com.example.coffee2.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Adapter.CartAdapter;
import com.example.coffee2.Domain.Coupon;
import com.example.coffee2.Helper.ChangeNumberItemsListener;
import com.example.coffee2.Helper.ManagmentCart;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityCartBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;
    private double discountAmount = 0.0;
    private List<String> couponList = new ArrayList<>();
    private List<Coupon> availableCoupons = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityCartBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());
       managmentCart = new ManagmentCart(this);
       setVariable();
       calculateCart();
       initList();
       loadCoupons();
    }

    private void initList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
        }
        else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cartView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> {
            calculateCart();         // Cập nhật tổng tiền
            reloadCouponSpinner();   // Làm mới lại danh sách coupon
        });
        binding.cartView.setAdapter(adapter);
    }

    private void calculateCart() {
        double percenTax = 0.02;
        double delivery = 10;

        double totalFeeRaw = managmentCart.getTotalFee(); // ← log chỗ này
        Log.d("DEBUG_CART", "Total raw fee: " + totalFeeRaw);

        tax = Math.round(totalFeeRaw * percenTax * 100.0) / 100.0;
        double total = Math.round((totalFeeRaw + tax + delivery) * 100.0) / 100.0;
        double itemTotal = Math.round(totalFeeRaw * 100.0) / 100.0;

        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        binding.totalFeeTxt.setText(format.format(itemTotal) + " đ");
        binding.taxTxt.setText(format.format(tax) + " đ");
        binding.deliveryTxt.setText(format.format(delivery) + " đ");
        binding.totalTxt.setText(format.format(total) + " đ");
    }

    private void loadCoupons() {
        DatabaseReference couponsRef = FirebaseDatabase.getInstance().getReference("Coupon");

        // Lắng nghe dữ liệu từ Firebase
        couponsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                availableCoupons.clear();
                couponList.clear();

                double totalAmount = managmentCart.getTotalFee(); // Lấy giá trị đơn hàng
                Log.d("DEBUG_CART", "Total order amount: " + totalAmount);

                // Lọc các coupon đủ điều kiện
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Coupon coupon = snapshot.getValue(Coupon.class); // Lấy thông tin coupon

                    if (coupon != null) {
                        Log.d("DEBUG_CART", "Coupon loaded: " + coupon.getCouponCode() + " with MinPurchaseAmount: " + coupon.getMinPurchaseAmount());

                        // Kiểm tra nếu giá trị đơn hàng đủ điều kiện để áp dụng coupon
                        if (totalAmount >= coupon.getMinPurchaseAmount()) {
                            // Kiểm tra ngày hết hạn của coupon (ExpirationDate)
                            Date expirationDate = coupon.getExpirationDateAsDate();

                            // So sánh với ngày hiện tại
                            if (expirationDate != null && expirationDate.after(new Date())) {
                                availableCoupons.add(coupon);
                                couponList.add(coupon.getCouponCode());
                            } else {
                                Log.d("DEBUG_CART", "Coupon expired: " + coupon.getCouponCode());
                            }
                        }
                    }
                }

                // Kiểm tra nếu có coupon hợp lệ
                if (couponList.isEmpty()) {
                    Log.d("DEBUG_CART", "No valid coupon available");
                    binding.spinnerCoupons.setVisibility(View.GONE); // Ẩn Spinner nếu không có coupon hợp lệ
                } else {
                    Log.d("DEBUG_CART", "Valid coupons available: " + couponList.size());
                    binding.spinnerCoupons.setVisibility(View.VISIBLE); // Hiển thị Spinner nếu có coupon hợp lệ

                    // Populate Spinner với các mã coupon
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CartActivity.this, android.R.layout.simple_spinner_item, couponList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerCoupons.setAdapter(adapter);

                    // Set up listener khi người dùng chọn coupon
                    binding.spinnerCoupons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // Áp dụng giảm giá khi người dùng chọn coupon
                            Coupon selectedCoupon = availableCoupons.get(position);
                            double discount = 0.0;

                            if (totalAmount >= selectedCoupon.getMinPurchaseAmount()) {
                                discount = (totalAmount * selectedCoupon.getDiscountPercentage()) / 100;
                            }

                            Log.d("DEBUG_CART", "Discount applied: " + discount);
                            // Cập nhật lại tổng tiền sau khi áp dụng coupon
                            calculateCart();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // Không có coupon nào được chọn
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DEBUG_CART", "loadCoupons:onCancelled", databaseError.toException());
            }
        });
    }

    private Coupon findCouponByCode(String couponCode) {
        for (Coupon coupon : availableCoupons) {
            if (coupon.getCouponCode().equals(couponCode)) {
                return coupon;
            }
        }
        return null;
    }
    private void reloadCouponSpinner() {
        loadCoupons(); // Gọi lại để cập nhật danh sách coupon
        binding.spinnerCoupons.setSelection(0); // Reset lại lựa chọn về đầu tiên
    }

    private void setVariable(){
        binding.backBtn.setOnClickListener(v ->finish());
    }
}