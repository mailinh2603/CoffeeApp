package com.example.coffee2.Domain;

public class users {
    private String UserId;
    private String UserName;
    private String Email;
    private String Address;
    private String BirthDate;
    private String PhoneNumber;

    public users() {

    }

    public String getId() {
        return UserId;
    }

    public void setId(String id) {
        this.UserId = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getBirthDate() {
        return BirthDate;
    }

    public void setBirthDate(String birthDate) {
        BirthDate = birthDate;
    }
}
