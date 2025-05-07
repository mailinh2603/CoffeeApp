package com.example.coffee2.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;



import com.example.coffee2.Helper.qr.QrService;
import com.example.coffee2.databinding.ActivityPaymentBinding;

public class PaymentActivity extends BaseActivity {

    private ActivityPaymentBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Content là tiếng việt k dấu, k ký tự đặc biệt, trong trường hợp này là mã đơn hàng
        // amount là số tiền cần thanh toán
        // bankNumber là mã ngân hàng, tuỳ thuộc vào từng bank mà có bank bin khác nhau, tham khảo createConsumeAccountInfo trong file QrUtils.java
        // imageWidth và imageHeight là kích thước của ảnh QR code

        String content = "abcxyz";
        String amount = "100000";
        String bankNumber = "3200999999";
        int imageWidth = 300;
        int imageHeight = 300;

        Bitmap bitmap = QrService.convertBase64ToBitmap(content, amount, bankNumber, imageWidth, imageHeight);


        binding.imgQr.setImageBitmap(bitmap);



    }
}