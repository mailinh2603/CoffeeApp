package com.example.coffee2.Domain;

public class BillDetailItem {
    public String name, option, imagePath;
    public long quantity;
    public double price;

    public BillDetailItem(String name, String option, long quantity, double price, String imagePath) {
        this.name = name;
        this.option = option;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = imagePath;
    }
}
