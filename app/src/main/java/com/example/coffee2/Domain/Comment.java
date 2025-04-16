package com.example.coffee2.Domain;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
@IgnoreExtraProperties
public class Comment {
    private String CommentId;
    private String CommentDetail;
    private String Cublish;
    private int Rating;
    private boolean Active;
    private String UserId;
    private int DrinkId;
    private String UserName;

    public Comment() {}

    @PropertyName("CommentId")
    public String getCommentId() {
        return CommentId;
    }
    @PropertyName("CommentId")
    public void setCommentId(String commentId) {
        CommentId = commentId;
    }
    @PropertyName("CommentDetail")
    public String getCommentDetail() {
        return CommentDetail;
    }
    @PropertyName("CommentDetail")
    public void setCommentDetail(String commentDetail) {
        CommentDetail = commentDetail;
    }
    @PropertyName("Cublish")
    public String getCublish() {
        return Cublish;
    }
    @PropertyName("Cublish")
    public void setCublish(String cublish) {
        Cublish = cublish;
    }
    @PropertyName("Rating")
    public int getRating() {
        return Rating;
    }
    @PropertyName("Rating")
    public void setRating(int rating) {
        Rating = rating;
    }
    @PropertyName("Active")
    public boolean isActive() {
        return Active;
    }
    @PropertyName("Active")
    public void setActive(boolean active) {
        Active = active;
    }
    @PropertyName("UserId")
    public String getUserId() {
        return UserId;
    }
    @PropertyName("UserId")
    public void setUserId(String userId) {
        UserId = userId;
    }
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }
    @PropertyName("DrinkId")
    public int getDrinkId() {
        return DrinkId;
    }
    @PropertyName("DrinkId")
    public void setDrinkId(int drinkId) {
        DrinkId = drinkId;
    }
}
