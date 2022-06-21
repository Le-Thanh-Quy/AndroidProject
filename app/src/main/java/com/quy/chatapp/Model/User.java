package com.quy.chatapp.Model;

import java.io.Serializable;

public class User implements Serializable {
    private static User instance;
    public String userAvatar;
    public String userName;
    public String phoneNumber;
    public String password;
    public String token;

    public static User getInstance() {
        if(instance == null) {
            instance = new User();
        }
        return  instance;
    }

    public User(String userAvatar, String userName, String phoneNumber, String password, String token) {
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.token = token;
    }

    public User() {
    }

    public static void setInstance(User instance) {
        User.instance = instance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
