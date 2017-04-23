package com.rahul.hollywoodhub;

import java.util.Date;

/**
 * Created by root on 4/23/17.
 */

public class ChatModel {
    private String message;
    private String userName;
    private long time;
    private String imageUrl;
    private String uID;

    public ChatModel(String message, String userName, String imageUrl, String uID) {
        this.message = message;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.uID = uID;
        this.time = new Date().getTime();
    }

    public ChatModel() {}

    public String getMessage() {
        return message;
    }

    public String getUserName() {
        return userName;
    }

    public long getTime() {
        return time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getuID() {
        return uID;
    }
}
