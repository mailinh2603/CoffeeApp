package com.example.coffee2.Domain;

import java.io.Serializable;

public class Drinks implements Serializable {
    private int Id;
    private String Title;
    private String Description;
    private String ImagePath;
    private double Price;
    private double Star;
    private boolean BestDrink;
    private boolean Active;
    private String TimeStamp;

    private int CategoryId;
    private int BeverageId;
    private int PriceId;

    private int numberInCart;
    private String sugarOption;
    private String iceOption;
    private int sugarOptionId;  // Đảm bảo rằng đây là ID đường
    private String iceOptionId;

    public int getSugarOptionId() {
        return sugarOptionId;
    }

    public void setSugarOptionId(int sugarOptionId) {
        this.sugarOptionId = sugarOptionId;
    }

    public String getIceOptionId() {
        return iceOptionId;
    }

    public void setIceOptionId(String iceOptionId) {
        this.iceOptionId = iceOptionId;
    }

    public String getSugarOption() {
        return sugarOption;
    }

    public void setSugarOption(String sugarOption) {
        this.sugarOption = sugarOption;
    }
    public String getIceOption() {
        return iceOption;
    }

    public void setIceOption(String iceOption) {
        this.iceOption = iceOption;
    }
    public Drinks() {}

    @Override
    public String toString() {
        return Title;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public double getStar() {
        return Star;
    }

    public void setStar(double star) {
        Star = star;
    }

    public boolean isBestDrink() {
        return BestDrink;
    }

    public void setBestDrink(boolean bestDrink) {
        BestDrink = bestDrink;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public int getBeverageId() {
        return BeverageId;
    }

    public void setBeverageId(int beverageId) {
        BeverageId = beverageId;
    }

    public int getPriceId() {
        return PriceId;
    }

    public void setPriceId(int priceId) {
        PriceId = priceId;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }
}
