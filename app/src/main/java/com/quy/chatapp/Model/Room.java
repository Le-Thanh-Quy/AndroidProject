package com.quy.chatapp.Model;

public class Room {
    private String roomID;
    private String lastMess;
    private String roomTimeLastMess;
    private String imageRoom;
    private String roomName;
    private String roomType;
    private String phoneNumber;
    private boolean seen;

    public Room() {
    }

    public Room(String roomID, String lastMess, String roomTimeLastMess, String imageRoom, String roomName, String roomType, String phoneNumber, boolean seen) {
        this.roomID = roomID;
        this.lastMess = lastMess;
        this.roomTimeLastMess = roomTimeLastMess;
        this.imageRoom = imageRoom;
        this.roomName = roomName;
        this.roomType = roomType;
        this.phoneNumber = phoneNumber;
        this.seen = seen;
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

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
