package com.example.coffee2.Helper.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class QrService {

    // set image width height from client app. Set to 200 for testing
    public static String getBase64ImageForQr(String content, String amount, String bankNumber, int imageWidth, int imageHeight) {
        return QrUtils.getQRCodeImage(content, amount, bankNumber, imageWidth, imageHeight);
    }

    public static Bitmap convertBase64ToBitmap(String content, String amount, String bankNumber, int imageWidth, int imageHeight) {

        String base64String = getBase64ImageForQr(content, amount, bankNumber, imageWidth, imageHeight);
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}