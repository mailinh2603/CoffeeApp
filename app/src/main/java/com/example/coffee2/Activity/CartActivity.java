package com.example.coffee2.Activity;

import android.content.Intent;
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
import com.example.coffee2.Domain.Drinks;
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
    private double finalTotal = 0.0;

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
        ArrayList<String> sugarOptions = new ArrayList<>();
        ArrayList<String> iceOptions = new ArrayList<>();

        for (Drinks drink : managmentCart.getListCart()) {
            String sugar = drink.getSugarOption();
            if (sugar == null || sugar.isEmpty()) sugar = "100%";
            sugarOptions.add(sugar);

            String ice = drink.getIceOption();
            if (ice == null || ice.isEmpty()) ice = "100%";
            iceOptions.add(ice);
        }

        binding.button2.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("total", finalTotal);
            intent.putStringArrayListExtra("sugarOptions", sugarOptions);
            intent.putStringArrayListExtra("iceOptions", iceOptions);
            startActivity(intent);
        });
    }

    private void initList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.cartView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> {
            calculateCart();
            reloadCouponSpinner();
        });
        binding.cartView.setAdapter(adapter);
    }
    public static String formatCurrencyVND(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
    private void calculateCart() {
        double percenTax = 0.02;
        double delivery = 10000;

        double totalFeeRaw = managmentCart.getTotalFee();
        Log.d("DEBUG_CART", "Total raw fee: " + totalFeeRaw);

        String selectedCouponCode = (String) binding.spinnerCoupons.getSelectedItem();
        double discount = 0.0;

        if (selectedCouponCode != null) {
            Coupon selectedCoupon = findCouponByCode(selectedCouponCode);
            if (selectedCoupon != null && totalFeeRaw >= selectedCoupon.getMinPurchaseAmount()) {
                discount = (totalFeeRaw * selectedCoupon.getDiscountPercentage()) / 100.0;
            }
        }

        discountAmount = Math.round(discount * 100.0) / 100.0;
        tax = Math.round(totalFeeRaw * percenTax * 100.0) / 100.0;
        double total = Math.round((totalFeeRaw + tax + delivery - discountAmount) * 100.0) / 100.0;
        finalTotal = total;
        double itemTotal = Math.round(totalFeeRaw * 100.0) / 100.0;

        // Sử dụng formatCurrencyVND thay vì format US + " đ"
        binding.totalFeeTxt.setText(formatCurrencyVND(itemTotal));
        binding.taxTxt.setText(formatCurrencyVND(tax));
        binding.deliveryTxt.setText(formatCurrencyVND(delivery));
        binding.discountTxt.setText("-" + formatCurrencyVND(discountAmount));
        binding.totalTxt.setText(formatCurrencyVND(total));
    }

    private void loadCoupons() {
        DatabaseReference couponsRef = FirebaseDatabase.getInstance().getReference("Coupon");

        couponsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                availableCoupons.clear();
                couponList.clear();

                double totalAmount = managmentCart.getTotalFee();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Coupon coupon = snapshot.getValue(Coupon.class);
                    if (coupon != null) {
                        if (totalAmount >= coupon.getMinPurchaseAmount()) {
                            Date expirationDate = coupon.getExpirationDateAsDate();
                            if (expirationDate != null && expirationDate.after(new Date())) {
                                availableCoupons.add(coupon);
                                couponList.add(coupon.getCouponCode());
                            } else {
                                Log.d("DEBUG_CART", "Coupon expired: " + coupon.getCouponCode());
                            }
                        }
                    }
                }
                if (couponList.isEmpty()) {
                    Log.d("DEBUG_CART", "No valid coupon available");
                    binding.spinnerCoupons.setVisibility(View.GONE);
                } else {
                    Log.d("DEBUG_CART", "Valid coupons available: " + couponList.size());
                    binding.spinnerCoupons.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CartActivity.this, android.R.layout.simple_spinner_item, couponList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerCoupons.setAdapter(adapter);

                    binding.spinnerCoupons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            Coupon selectedCoupon = availableCoupons.get(position);
                            double discount = 0.0;

                            if (totalAmount >= selectedCoupon.getMinPurchaseAmount()) {
                                discount = (totalAmount * selectedCoupon.getDiscountPercentage()) / 100;
                            }
                            calculateCart();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
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
        loadCoupons();
        binding.spinnerCoupons.setSelection(0);
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }
}