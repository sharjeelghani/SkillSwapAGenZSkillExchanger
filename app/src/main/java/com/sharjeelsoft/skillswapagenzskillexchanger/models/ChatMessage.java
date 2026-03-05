package com.sharjeelsoft.skillswapagenzskillexchanger.models;

public class ChatMessage {
    private String id;
    private String text;
    private String senderId;
    private long timestamp;

    public ChatMessage() { } // required for Firebase

    public ChatMessage(String id, String text, String senderId, long timestamp) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
}

