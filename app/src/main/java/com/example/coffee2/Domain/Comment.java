package com.example.coffee2.Domain;

public class Comment {
    private int CommentId;
    private String CommentDetail;
    private String Cublish;
    private int Rating;
    private boolean Active;
    private String UserId;
    private int DrinkId;
    private String UserName;

    public Comment() {}

    public int getCommentId() {
        return CommentId;
    }

    public void setCommentId(int commentId) {
        CommentId = commentId;
    }

    public String getCommentDetail() {
        return CommentDetail;
    }

    public void setCommentDetail(String commentDetail) {
        CommentDetail = commentDetail;
    }

    public String getCublish() {
        return Cublish;
    }

    public void setCublish(String cublish) {
        Cublish = cublish;
    }

    public int getRating() {
        return Rating;
    }

    public void setRating(int rating) {
        Rating = rating;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public int getDrinkId() {
        return DrinkId;
    }

    public void setDrinkId(int drinkId) {
        DrinkId = drinkId;
    }
}
