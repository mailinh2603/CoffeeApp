package com.example.coffee2.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Adapter.BillAdapter;
import com.example.coffee2.Adapter.OrderDetailsAdapter;
import com.example.coffee2.Domain.Bill;
import com.example.coffee2.Domain.BillDetailItem;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityEditProfileBinding;
import com.example.coffee2.databinding.ActivityOrderTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity {

    private TextView tvBillId, tvOrderDate, tvStatus, tvPayment, tvTotal;
    private OrderDetailsAdapter orderDetailsAdapter;
    private List<BillDetailItem> billDetailList = new ArrayList<>();

    private RecyclerView recyclerViewBills;
    private List<Bill> billList = new ArrayList<>();
    private BillAdapter billAdapter;

    private DatabaseReference billRef;
    private DatabaseReference billDetailRef;
    private DatabaseReference drinkRef;

    private String currentUserId = null;
    private ActivityOrderTrackingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupRecyclerViews();
        initFirebaseReferences();

        fetchCurrentUserIdAndLoadBills();
        binding.backBtn4.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvBillId = findViewById(R.id.tvBillId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvPayment = findViewById(R.id.tvPayment);
        tvTotal = findViewById(R.id.tvTotal);


        recyclerViewBills = findViewById(R.id.recyclerViewBills);
    }

    private void setupRecyclerViews() {
        orderDetailsAdapter = new OrderDetailsAdapter(billDetailList);

        recyclerViewBills.setLayoutManager(new LinearLayoutManager(this));
        billAdapter = new BillAdapter(billList, this::showBillDetails);
        recyclerViewBills.setAdapter(billAdapter);
    }

    private void initFirebaseReferences() {
        billRef = FirebaseDatabase.getInstance().getReference("Bill");
        billDetailRef = FirebaseDatabase.getInstance().getReference("BillDetails");
        drinkRef = FirebaseDatabase.getInstance().getReference("Drinks");
    }

    private void fetchCurrentUserIdAndLoadBills() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email người dùng không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                currentUserId = userSnap.getKey();
                                loadBills(currentUserId);
                                // Xóa dòng dưới
                                // loadOrderInfo(currentUserId);
                                break;
                            }
                        } else {
                            Toast.makeText(OrderTrackingActivity.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("OrderTracking", "Lấy userId thất bại", error.toException());
                        Toast.makeText(OrderTrackingActivity.this, "Lỗi khi lấy userId!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadBills(String userId) {
        billRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        billList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Bill bill = snap.getValue(Bill.class);
                            if (bill != null) {
                                billList.add(bill);
                            }
                        }
                        billAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderTrackingActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderInfo(String userId) {
        billRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(OrderTrackingActivity.this, "Chưa có đơn hàng nào!", Toast.LENGTH_SHORT).show();
                            clearOrderInfo();
                            return;
                        }
                        // Lấy đơn hàng đầu tiên làm mặc định hiển thị
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String billId = snap.child("billId").getValue(String.class);
                            Double total = snap.child("totalPrice").getValue(Double.class);

                            tvBillId.setText("Mã đơn: " + (billId != null ? billId : ""));
                            tvOrderDate.setText(snap.child("orderDate").getValue(String.class));
                            tvStatus.setText(snap.child("orderStatus").getValue(String.class));
                            tvPayment.setText(snap.child("paymentMethod").getValue(String.class));
                            tvTotal.setText("Tổng tiền: " + (total != null ? total : 0) + "₫");

                            if (billId != null) {
                                loadBillDetails(billId);
                            } else {
                                billDetailList.clear();
                                orderDetailsAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderTrackingActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearOrderInfo() {
        tvBillId.setText("Mã đơn: ");
        tvOrderDate.setText("");
        tvStatus.setText("");
        tvPayment.setText("");
        tvTotal.setText("Tổng tiền: 0₫");
        billDetailList.clear();
        orderDetailsAdapter.notifyDataSetChanged();
    }

    private void loadBillDetails(String billId) {
        billDetailList.clear();
        orderDetailsAdapter.notifyDataSetChanged();

        billDetailRef.orderByChild("billId").equalTo(billId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }

                        for (DataSnapshot detailSnap : snapshot.getChildren()) {
                            Long drinkId = detailSnap.child("drinkId").getValue(Long.class);
                            String option = detailSnap.child("option").getValue(String.class);
                            Long quantity = detailSnap.child("quantity").getValue(Long.class);
                            Double unitPrice = detailSnap.child("unitPrice").getValue(Double.class);

                            if (drinkId == null) continue;

                            drinkRef.child(String.valueOf(drinkId))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot drinkSnap) {
                                            String drinkName = drinkSnap.child("Title").getValue(String.class);
                                            String imagePath = drinkSnap.child("ImagePath").getValue(String.class);
                                            billDetailList.add(new BillDetailItem(drinkName, option, quantity, unitPrice, imagePath));
                                            orderDetailsAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showBillDetails(Bill bill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đơn: " + bill.getBillId());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_details, null);
        RecyclerView recyclerViewDialog = dialogView.findViewById(R.id.recyclerViewDialogOrderDetails);
        recyclerViewDialog.setLayoutManager(new LinearLayoutManager(this));

        List<BillDetailItem> dialogBillDetailList = new ArrayList<>();
        OrderDetailsAdapter dialogAdapter = new OrderDetailsAdapter(dialogBillDetailList);
        recyclerViewDialog.setAdapter(dialogAdapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        loadBillDetailsForDialog(bill.getBillId(), dialogBillDetailList, dialogAdapter);
    }

    private void loadBillDetailsForDialog(String billId, List<BillDetailItem> detailList, OrderDetailsAdapter adapter) {
        detailList.clear();
        adapter.notifyDataSetChanged();

        billDetailRef.orderByChild("billId").equalTo(billId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }

                        for (DataSnapshot detailSnap : snapshot.getChildren()) {
                            Long drinkId = detailSnap.child("drinkId").getValue(Long.class);
                            String option = detailSnap.child("option").getValue(String.class);
                            Long quantity = detailSnap.child("quantity").getValue(Long.class);
                            Double unitPrice = detailSnap.child("unitPrice").getValue(Double.class);

                            if (drinkId == null) continue;

                            drinkRef.child(String.valueOf(drinkId))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot drinkSnap) {
                                            String drinkName = drinkSnap.child("Title").getValue(String.class);
                                            String imagePath = drinkSnap.child("ImagePath").getValue(String.class);
                                            detailList.add(new BillDetailItem(drinkName, option, quantity, unitPrice, imagePath));
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
