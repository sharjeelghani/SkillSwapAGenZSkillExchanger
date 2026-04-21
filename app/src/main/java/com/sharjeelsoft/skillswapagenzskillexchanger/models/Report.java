package com.sharjeelsoft.skillswapagenzskillexchanger.models;

import com.google.firebase.database.Exclude;

public class Report {
    private String reportedUsername;
    private String reportingUsername;
    private String reportCause;
    private String additionalDetails;
    private long timestamp;


    @Exclude
    private int reportCount;

    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)
    }

    public Report(String reportedUsername, String reportingUsername, String reportCause, String additionalDetails, long timestamp) {
        this.reportedUsername = reportedUsername;
        this.reportingUsername = reportingUsername;
        this.reportCause = reportCause;
        this.additionalDetails = additionalDetails;
        this.timestamp = timestamp;
    }

    public String getReportedUsername() {
        return reportedUsername;
    }

    public void setReportedUsername(String reportedUsername) {
        this.reportedUsername = reportedUsername;
    }

    public String getReportingUsername() {
        return reportingUsername;
    }

    public void setReportingUsername(String reportingUsername) {
        this.reportingUsername = reportingUsername;
    }

    public String getReportCause() {
        return reportCause;
    }

    public void setReportCause(String reportCause) {
        this.reportCause = reportCause;
    }

    public String getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public int getReportCount() {
        return reportCount;
    }

    @Exclude
    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }
}
