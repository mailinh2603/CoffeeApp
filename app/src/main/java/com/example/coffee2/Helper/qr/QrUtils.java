package com.example.coffee2.Helper.qr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class QrUtils {

    private static final String DATA_VERSION = "000201";
    private static final String INIT_METHOD = "010212";
    private static final String TRANSACTION_CURRENCY = "5303704";
    private static final String COUNTRY_CODE = "5802VN";

    public static String getAdditionInfo(String str) {
        String baseInfo = "08" + convertLength(str.length()) + str;
        return "62" + convertLength(baseInfo.length()) + baseInfo;
    }

    public static String getAmount(String amount) {
        return "54" + convertLength(amount.length()) + amount;
    }

    public static String getQRCodeImage(String content, String amount, String bankNumber, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            String text = QrUtils.createStringForBank(content, amount, bankNumber);
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            Log.d("QrUtils", "StartText |" + base64 + "| endText");
            return base64;
        } catch (Exception e) {
            Log.d("QrUtils", "error: " + e.getMessage());
            return null;
        }
    }


    public static String createStringForBank(String content, String amount, String bankNumber) {
        Log.d("createStringForBank","StartConsumeAccountInfo |" + createConsumeAccountInfo(bankNumber) + "| endConsumeAccountInfo");
        Log.d("createStringForBank","StartTRANSACTION_AMOUNT |" + getAmount(amount) + "| endTRANSACTION_AMOUNT");
        Log.d("createStringForBank","StartADDITIONAL_INFO |" + getAdditionInfo(content) + "| endADDITIONAL_INFO");
        String base = DATA_VERSION +
                INIT_METHOD +
                createConsumeAccountInfo(bankNumber) +
                TRANSACTION_CURRENCY +
                getAmount(amount) +
                COUNTRY_CODE +
                getAdditionInfo(content) + "6304";

        String crc = CrcUtils.calcCRC(base);
        Log.d("StartCRC","StartCRC |" + crc + "| endCRC");
        return base + crc;

    }

    /**
     * | Ngân hàng               | Mã BIN |
     * | ----------------------- | ------ |
     * | MB (Ngân hàng Quân Đội) | 970422 |
     * | Vietcombank             | 970436 |
     * | BIDV                    | 970418 |
     * | Techcombank             | 970407 |
     * | VietinBank              | 970415 |
     */
    public static String createConsumeAccountInfo(String bankNumber) {
        final String GUID = "0010A000000727";
        final String SERVICE_CODE = "0208QRIBFTTA";
        String bankNumberInfo = "01" + bankNumber.length() + bankNumber;
        Log.d("StartBankNumberInfo | {} | endBankNumberInfo", bankNumberInfo);
        String bankBin = "970407";
        final String BANK = "0006" + bankBin + bankNumberInfo;
        String BankInfo = "01" + convertLength(BANK.length()) + BANK;
        Log.d("StartBankInfo |{}| endBankInfo", BankInfo);
        String s = GUID + BankInfo + SERVICE_CODE;
        return "38" + convertLength(s.length()) + s;
    }

    private static String convertLength(int length) {
        return length < 10 ? "0" + length : String.valueOf(length);
    }
}