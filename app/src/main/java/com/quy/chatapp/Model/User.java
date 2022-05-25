package com.quy.chatapp.Model;

public class User {
    private String userID;
    private String UserName;
    private String userAvatar;

    public User(String userID, String userName, String userAvatar) {
        this.userID = userID;
        UserName = userName;
        this.userAvatar = userAvatar;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}
