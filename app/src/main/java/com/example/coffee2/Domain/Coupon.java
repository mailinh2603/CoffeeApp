package com.example.coffee2.Domain;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Coupon {
    private String CouponCode;
    private int CouponId;
    private double DiscountPercentage;
    private double MinPurchaseAmount;
    private String ExpirationDate;

    public Coupon() {
    }

    public int getCouponId() {
        return CouponId;
    }

    public void setCouponId(int couponId) {
        CouponId = couponId;
    }

    public String getCouponCode() {
        return CouponCode;
    }

    public void setCouponCode(String couponCode) {
        CouponCode = couponCode;
    }

    public double getDiscountPercentage() {
        return DiscountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        DiscountPercentage = discountPercentage;
    }

    public double getMinPurchaseAmount() {
        return MinPurchaseAmount;
    }

    public void setMinPurchaseAmount(double minPurchaseAmount) {
        MinPurchaseAmount = minPurchaseAmount;
    }

    public String getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }
    public Date getExpirationDateAsDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(ExpirationDate);
        } catch (ParseException e) {
            return null;
        }
    }
}
