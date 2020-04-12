package com.example.weshare;

import com.google.firebase.Timestamp;

public class VideoData {

    private String title;
    private String url;
    private String from;
    private Timestamp timestamp;
    private boolean status;

    public VideoData(String title, String url, String from, Timestamp timestamp, boolean status) {
        this.title = title;
        this.url = url;
        this.from = from;
        this.timestamp = timestamp;
        this.status = status;
    }

    public VideoData() {
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean isStatus() {
        return status;
    }

    public String getFrom() {
        return from;
    }
}
