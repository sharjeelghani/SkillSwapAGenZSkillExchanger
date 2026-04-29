package com.sharjeelsoft.skillswapagenzskillexchanger.models;

public class SessionModel {
    private String id;
    private String title;
    private String date;
    private String time;
    private String otherUserId;
    private String otherUserFullName;
    private String status; // PENDING, COMPLETED, MISSED
    private long scheduledTimestamp;


    public SessionModel() {
    }

    public SessionModel(String id, String title, String date, String time, String otherUserId, String otherUserFullName, String status, long scheduledTimestamp) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.otherUserId = otherUserId;
        this.otherUserFullName = otherUserFullName;
        this.status = status;
        this.scheduledTimestamp = scheduledTimestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getOtherUserId() { return otherUserId; }
    public String getOtherUserFullName() { return otherUserFullName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getScheduledTimestamp() { return scheduledTimestamp; }
}
