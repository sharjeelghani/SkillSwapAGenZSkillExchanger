package com.sharjeelsoft.skillswapagenzskillexchanger.models;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String senderId;
    private long timestamp;
    private String type; // e.g., "match_request"
    private boolean read;

    public NotificationModel() {
    }
    public NotificationModel(String id, String title, String message, String senderId, long timestamp, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
        this.read = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
