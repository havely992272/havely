package com.havely.messenger;

import java.util.Date;

public class Message {
    private String id;
    private String userId;
    private String username;
    private String text;
    private Date timestamp;
    
    public Message() {}
    
    public Message(String userId, String username, String text) {
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = new Date();
    }
    
    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
