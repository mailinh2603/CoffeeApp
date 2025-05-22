package com.example.coffee2.Domain;

public class BillDetails {
    private int BillDetailId;
    private String BillId;
    private int DrinkId;
    private int Quantity;
    private double UnitPrice;
    private String Option;

    public BillDetails() {}

    public int getBillDetailId() {
        return BillDetailId;
    }

    public void setBillDetailId(int billDetailId) {
        BillDetailId = billDetailId;
    }

    public String getBillId() {
        return BillId;
    }

    public void setBillId(String billId) {
        BillId = billId;
    }

    public int getDrinkId() {
        return DrinkId;
    }

    public void setDrinkId(int drinkId) {
        DrinkId = drinkId;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public double getUnitPrice() {
        return UnitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        UnitPrice = unitPrice;
    }

    public String getOption() {
        return Option;
    }

    public void setOption(String option) {
        Option = option;
    }
}
