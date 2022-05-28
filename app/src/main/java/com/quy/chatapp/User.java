package com.quy.chatapp;

public class User {
    private static User instance;
    public String userAvatar;
    public String userName;
    public String phoneNumber;
    public boolean status;
    public String password;

    public static User getInstance() {
        if(instance == null) {
            instance = new User();
        }
        return  instance;
    }

    public User(String userAvatar, String userName, String phoneNumber, boolean status, String password) {
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.password = password;
    }

    public User() {
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
