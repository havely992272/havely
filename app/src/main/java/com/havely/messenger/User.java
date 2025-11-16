package com.havely.messenger;

public class User {
    public String id;
    public String username;
    public String displayName;
    
    public User() {}
    
    public User(String id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }
}
