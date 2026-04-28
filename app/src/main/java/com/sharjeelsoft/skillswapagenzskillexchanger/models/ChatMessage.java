package com.sharjeelsoft.skillswapagenzskillexchanger.models;

public class ChatMessage {
    private String id;
    private String text;
    private String senderId;
    private long timestamp;
    private boolean read;
    private boolean delivered;

    public ChatMessage() { } // required for Firebase
    public ChatMessage(String id, String text, String senderId, long timestamp) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.read = false;
        this.delivered = false;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
}
