package com.quy.chatapp.Model;

public class Messenger {
    private String userID;
    private String messContent;
    private String messTime;
    private String messType;

    public Messenger(String userID, String messContent, String messTime, String messType) {
        this.userID = userID;
        this.messContent = messContent;
        this.messTime = messTime;
        this.messType = messType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessContent() {
        return messContent;
    }

    public void setMessContent(String messContent) {
        this.messContent = messContent;
    }

    public String getMessTime() {
        return messTime;
    }

    public void setMessTime(String messTime) {
        this.messTime = messTime;
    }

    public String getMessType() {
        return messType;
    }

    public void setMessType(String messType) {
        this.messType = messType;
    }
}
