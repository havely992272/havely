package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends Activity {

    private ImageButton backButton, menuButton, sendButton;
    private EditText messageInput;
    private TextView chatUserName, chatUserStatus;
    private RecyclerView messagesRecyclerView;
    
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private MessagesAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private ListenerRegistration messagesListener;
    
    private String currentUserId;
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");
        
        // Получаем данные из Intent
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("user_id");
        otherUserName = intent.getStringExtra("username");
        otherUserDisplayName = intent.getStringExtra("display_name");
        
        // Создаем ID чата (сортируем ID для уникальности)
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }
        
        initializeViews();
        setupClickListeners();
        setupMessages();
        loadUserStatus();
        markMessagesAsRead();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);
        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        chatUserName = findViewById(R.id.chatUserName);
        chatUserStatus = findViewById(R.id.chatUserStatus);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(messages, currentUserId);
        messagesRecyclerView.setAdapter(adapter);
        
        // Устанавливаем имя пользователя
        String displayName = otherUserDisplayName != null ? otherUserDisplayName : otherUserName;
        chatUserName.setText(displayName);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
        
        menuButton.setOnClickListener(v -> {
            // TODO: Показать меню чата
            showToast("Меню чата");
        });
        
        sendButton.setOnClickListener(v -> {
            sendMessage();
        });
    }
    
    private void setupMessages() {
        // Слушаем сообщения в реальном времени
        messagesListener = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    
                    messages.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        if (message != null) {
                            message.id = doc.getId();
                            messages.add(message);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                });
    }
    
    private void loadUserStatus() {
        // Загружаем статус пользователя
        db.collection("users").document(otherUserId)
                .addSnapshotListener((document, e) -> {
                    if (document != null && document.exists()) {
                        Timestamp lastSeen = document.getTimestamp("lastSeen");
                        boolean isOnline = document.getBoolean("isOnline");
                        
                        if (isOnline) {
                            chatUserStatus.setText("Онлайн");
                        } else if (lastSeen != null) {
                            String time = formatLastSeen(lastSeen.toDate());
                            chatUserStatus.setText("Был(а) в сети " + time);
                        } else {
                            chatUserStatus.setText("Был(а) недавно");
                        }
                    }
                });
    }
    
    private String formatLastSeen(Date date) {
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 1) {
            return "только что";
        } else if (minutes < 60) {
            return minutes + " мин. назад";
        } else if (hours < 24) {
            return hours + " ч. назад";
        } else if (days < 7) {
            return days + " д. назад";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
            return sdf.format(date);
        }
    }
    
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        
        // Очищаем поле ввода
        messageInput.setText("");
        
        // Создаем временное сообщение с статусом "sending"
        String tempMessageId = db.collection("temp").document().getId();
        Message tempMessage = new Message(text, currentUserId, Timestamp.now());
        tempMessage.id = tempMessageId;
        tempMessage.status = "sending";
        messages.add(tempMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Создаем сообщение для Firestore
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);
        messageData.put("senderId", currentUserId);
        messageData.put("timestamp", Timestamp.now());
        messageData.put("type", "text");
        messageData.put("status", "sent"); // Отправлено, но не прочитано
        
        // Добавляем сообщение в Firestore
        db.collection("chats").document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Удаляем временное сообщение
                    messages.removeIf(msg -> msg.id.equals(tempMessageId));
                    
                    // Обновляем с реальным сообщением
                    Message realMessage = new Message(text, currentUserId, Timestamp.now());
                    realMessage.id = documentReference.getId();
                    realMessage.status = "sent";
                    messages.add(realMessage);
                    
                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                    
                    // Обновляем последнее сообщение в чате
                    updateChatInfo(text);
                    
                })
                .addOnFailureListener(e -> {
                    // Обновляем статус на ошибку
                    for (Message msg : messages) {
                        if (msg.id.equals(tempMessageId)) {
                            msg.status = "error";
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    showToast("Ошибка отправки");
                });
    }
    
    private void updateChatInfo(String lastMessage) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("lastMessage", lastMessage);
        chatData.put("lastMessageTime", Timestamp.now());
        chatData.put("participants", List.of(currentUserId, otherUserId));
        chatData.put("unreadCount_" + otherUserId, FieldValue.increment(1));
        
        db.collection("chats").document(chatId)
                .set(chatData);
    }
    
    private void markMessagesAsRead() {
        // Помечаем все сообщения как прочитанные
        db.collection("chats").document(chatId)
                .collection("messages")
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("status", "sent")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().update("status", "read");
                    }
                });
        
        // Сбрасываем счетчик непрочитанных
        db.collection("chats").document(chatId)
                .update("unreadCount_" + currentUserId, 0);
    }
    
    private void scrollToBottom() {
        messagesRecyclerView.postDelayed(() -> {
            if (messages.size() > 0) {
                messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        }, 100);
    }
    
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем статус онлайн
        db.collection("users").document(currentUserId)
                .update("isOnline", true, "lastSeen", Timestamp.now());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Обновляем статус офлайн
        db.collection("users").document(currentUserId)
                .update("isOnline", false, "lastSeen", Timestamp.now());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
    
    // Класс сообщения
    public static class Message {
        public String id;
        public String text;
        public String senderId;
        public Timestamp timestamp;
        public String type;
        public String status; // sending, sent, read, error
        
        public Message() {}
        
        public Message(String text, String senderId, Timestamp timestamp) {
            this.text = text;
            this.senderId = senderId;
            this.timestamp = timestamp;
            this.type = "text";
            this.status = "sending";
        }
    }
}
