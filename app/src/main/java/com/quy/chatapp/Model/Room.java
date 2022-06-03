package com.quy.chatapp.Model;

public class Room {
    private String roomID;
    private String lastMess;
    private String roomTimeLastMess;
    private String imageRoom;
    private String roomName;
    private String roomType;
    private String userId;
    private String iconId;
    private String lastMessId;


    public Room() {
    }

    public Room(String roomID, String lastMess, String roomTimeLastMess, String imageRoom, String roomName, String roomType, String userId, String iconId, String lastMessId) {
        this.roomID = roomID;
        this.lastMess = lastMess;
        this.roomTimeLastMess = roomTimeLastMess;
        this.imageRoom = imageRoom;
        this.roomName = roomName;
        this.roomType = roomType;
        this.userId = userId;
        this.iconId = iconId;
        this.lastMessId = lastMessId;
    }

    public String getLastMessId() {
        return lastMessId;
    }

    public void setLastMessId(String lastMessId) {
        this.lastMessId = lastMessId;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomTimeLastMess() {
        return roomTimeLastMess;
    }

    public void setRoomTimeLastMess(String roomTimeLastMess) {
        this.roomTimeLastMess = roomTimeLastMess;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getLastMess() {
        return lastMess;
    }

    public void setLastMess(String lastMess) {
        this.lastMess = lastMess;
    }

    public String getImageRoom() {
        return imageRoom;
    }

    public void setImageRoom(String imageRoom) {
        this.imageRoom = imageRoom;
    }


    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

}
