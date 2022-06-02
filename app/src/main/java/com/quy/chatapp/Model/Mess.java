package com.quy.chatapp.Model;

public class Mess {
    private String message;
    private String time;
    private String type;
    private String userId;

    public Mess() {
    }

    public Mess(String message, String time, String type, String userId) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
