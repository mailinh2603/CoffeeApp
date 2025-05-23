package com.example.coffee2.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.coffee2.Domain.Bill;
import com.example.coffee2.Domain.BillDetails;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.Helper.ManagmentCart;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CheckoutActivity extends BaseActivity {
    private static final double QUAN_YEN_HA_LAT = 21.1052458;
    private static final double QUAN_YEN_HA_LON = 105.7945167;
    private ActivityCheckoutBinding binding;
    private ManagmentCart managmentCart;
    private DatabaseReference dbRef;
    private Spinner spinner;
    private String userIdTuSinh;
    Spinner spinnerDistrict, spinnerWard;
    ArrayList<String> districtNames = new ArrayList<>();
    ArrayList<String> wardNames = new ArrayList<>();
    EditText roadEditText;
    JSONArray districtsArray;
    private static final int ZP_APP_ID = 2553;           // fix chỗ này
    private static final String ZP_CALLBACK = "coffee2://zalopay";
    private ArrayList<String> sugarOptions;
    private ArrayList<String> iceOptions;
    private double total;
    public static String formatCurrencyVND(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        managmentCart = new ManagmentCart(this);

        Intent intent = getIntent();
        sugarOptions = intent.getStringArrayListExtra("sugarOptions");
        iceOptions = intent.getStringArrayListExtra("iceOptions");

        if (sugarOptions != null && iceOptions != null) {
            for (int i = 0; i < sugarOptions.size(); i++) {
                String sugar = sugarOptions.get(i);
                String ice = iceOptions.get(i);
                Log.d("Checkout", "Sản phẩm " + i + ": Đường = " + sugar + ", Đá = " + ice);
            }
        } else {
            Log.d("Checkout", "Không nhận được dữ liệu đường/đá");
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        String[] paymentOptions = {"Tiền mặt", "Chuyển khoản", "ZaloPay"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roadEditText = findViewById(R.id.roadEditText);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);

        loadDistrictWardFromJson();
        binding.paymentMethodSpinner.setAdapter(adapter);
        total = getIntent().getDoubleExtra("total", 0.0);
        binding.totalTxt.setText(formatCurrencyVND(total));

        binding.checkoutBtn.setOnClickListener(v -> validateAndPlaceOrder());
        vn.zalopay.sdk.ZaloPaySDK.init(ZP_APP_ID, vn.zalopay.sdk.Environment.SANDBOX);
        binding.backBtn2.setOnClickListener(v -> finish());
    }

    private void validateAndPlaceOrder() {
        String road = roadEditText.getText().toString().trim();
        String district = spinnerDistrict.getSelectedItem().toString().trim();
        String ward = spinnerWard.getSelectedItem().toString().trim();

        if (road.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        String fullAddress = road + ", " + ward + ", " + district + ", Hà Nội";
        fetchCoordinatesFromAddress(fullAddress);
    }

    private void fetchCoordinatesFromAddress(String address) {
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + Uri.encode(address) + "&key=87bba0f95532486cbfaa9176ec2f3610";
        Log.d("Geocode", "URL: " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Geocode", "Request failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi khi lấy tọa độ", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("Geocode", "Response: " + responseBody);
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray results = json.getJSONArray("results");

                        if (results.length() == 0) {
                            runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");

                        double userLat = geometry.getDouble("lat");
                        double userLon = geometry.getDouble("lng");

                        Log.d("Geocode", "Latitude: " + userLat + ", Longitude: " + userLon);

                        if (isWithinDistance(userLat, userLon, QUAN_YEN_HA_LAT, QUAN_YEN_HA_LON, 15)) {
                            runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Địa chỉ hợp lệ và trong phạm vi 15km", Toast.LENGTH_SHORT).show());

                            placeOrder();
                        } else {
                            runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Địa chỉ quá xa (> 15km)", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e("Geocode", "Error parsing JSON: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("Geocode", "Response unsuccessful: " + response.code());  // Log unsuccessful response status
                    runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi khi lấy tọa độ", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private boolean isWithinDistance(double userLat, double userLon, double targetLat, double targetLon, double maxDistance) {
        double distance = calculateDistance(userLat, userLon, targetLat, targetLon);
        return distance <= maxDistance;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private void placeOrder() {
        String emailDangNhap = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(emailDangNhap)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                Log.d("UserData", "User data: " + userSnapshot.getValue().toString());
                                userIdTuSinh = userSnapshot.child("userId").getValue(String.class);
                                Log.d("UserID", "1 User ID tự sinh: " + userIdTuSinh);

                                // Sau khi lấy được userIdTuSinh, mới thực hiện tạo đơn hàng:
                                createBillAndProcessOrder(userIdTuSinh);
                                break;  // Nếu chỉ có 1 user thì thoát loop
                            }
                        } else {
                            Log.d("UserID", "Không tìm thấy user với email: " + emailDangNhap);
                            Toast.makeText(CheckoutActivity.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserID", "Lấy userId thất bại", error.toException());
                        Toast.makeText(CheckoutActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void createBillAndProcessOrder(String userIdTuSinh) {
        String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
      //  double total = managmentCart.getTotalFee();
        String selectedPayment = binding.paymentMethodSpinner.getSelectedItem().toString();

        String billIdKey = "HD" + System.currentTimeMillis();
        String fullAddress = roadEditText.getText().toString().trim()
                + ", " + spinnerWard.getSelectedItem().toString().trim()
                + ", " + spinnerDistrict.getSelectedItem().toString().trim()
                + ", Hà Nội";

        Bill bill = new Bill();
        bill.setBillId(billIdKey);
        bill.setUserId(userIdTuSinh);
        bill.setOrderDate(orderDate);
        bill.setOrderStatus("Đang xử lý");
        bill.setPaymentMethod(selectedPayment);
        bill.setTotalPrice(total);
        bill.setAddress(fullAddress);

        DatabaseReference billRef = dbRef.child("Bill").child(billIdKey);

        billRef.setValue(bill).addOnSuccessListener(unused -> {
            List<Drinks> cartList = managmentCart.getListCart();

            for (int i = 0; i < cartList.size(); i++) {
                Drinks item = cartList.get(i);

                BillDetails details = new BillDetails();
                details.setBillId(billIdKey);
                details.setDrinkId(item.getId());
                details.setQuantity(item.getNumberInCart());
                details.setUnitPrice(item.getPrice());

                String sugar = (sugarOptions != null && i < sugarOptions.size()) ? sugarOptions.get(i) : "100%";
                String ice = (iceOptions != null && i < iceOptions.size()) ? iceOptions.get(i) : "100%";
                String option = "Đường: " + sugar + ", Đá: " + ice;
                details.setOption(option);

                dbRef.child("BillDetails").push().setValue(details);
            }

            managmentCart.clearCart();

            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

            if (selectedPayment.equals("Tiền mặt")) {
                Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if (selectedPayment.equals("Chuyển khoản")) {
                Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                intent.putExtra("billId", billIdKey);
                intent.putExtra("total", total);
                startActivity(intent);
                finish();
            } else if (selectedPayment.equals("ZaloPay")) {
                new Thread(() -> {
                    try {
                        String amountStr = String.valueOf((int) total);
                        org.json.JSONObject res = new com.example.coffee2.ZaloPay.CreateOrder()
                                .createOrder(amountStr);
                        if ("1".equals(res.getString("return_code"))) {
                            String zpTransToken = res.getString("zp_trans_token");
                            runOnUiThread(() -> payWithZalo(zpTransToken, billIdKey));
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Tạo đơn ZaloPay thất bại", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(this, "Lỗi ZaloPay", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi khi đặt hàng", Toast.LENGTH_SHORT).show()
        );
    }

    private void payWithZalo(String token, String billId) {
        vn.zalopay.sdk.ZaloPaySDK.getInstance()
                .payOrder(this, token, ZP_CALLBACK, new vn.zalopay.sdk.listeners.PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transId, String transToken, String appTransID) {
                        Toast.makeText(CheckoutActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        gotoHome();
                    }
                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        Toast.makeText(CheckoutActivity.this, "Bạn đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPaymentError(vn.zalopay.sdk.ZaloPayError error, String zpTransToken, String appTransID) {
                        Toast.makeText(CheckoutActivity.this, "Lỗi thanh toán: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void gotoHome() {
        Intent i = new Intent(CheckoutActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void loadDistrictWardFromJson() {
        try {
            InputStream is = getAssets().open("data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray provinces = new JSONArray(json);
            JSONObject hanoi = null;

            for (int i = 0; i < provinces.length(); i++) {
                JSONObject obj = provinces.getJSONObject(i);
                if (obj.getString("Name").equals("Thành phố Hà Nội")) {
                    hanoi = obj;
                    break;
                }
            }

            if (hanoi != null) {
                districtsArray = hanoi.getJSONArray("Districts");
                districtNames.clear();

                for (int i = 0; i < districtsArray.length(); i++) {
                    JSONObject district = districtsArray.getJSONObject(i);
                    districtNames.add(district.getString("Name"));
                }

                ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districtNames);
                districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDistrict.setAdapter(districtAdapter);

                // Sự kiện chọn quận
                spinnerDistrict.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        try {
                            JSONObject selectedDistrict = districtsArray.getJSONObject(position);
                            JSONArray wardsArray = selectedDistrict.getJSONArray("Wards");

                            wardNames.clear();
                            for (int i = 0; i < wardsArray.length(); i++) {
                                wardNames.add(wardsArray.getJSONObject(i).getString("Name"));
                            }

                            ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, wardNames);
                            wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerWard.setAdapter(wardAdapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}