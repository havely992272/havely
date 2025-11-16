package com.havely.messenger;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class Chat {
    public String id;
    public String lastMessage;
    public @ServerTimestamp Timestamp lastMessageTime;
    public List<String> participants;
    public int unreadCount;
    
    public Chat() {}
}
