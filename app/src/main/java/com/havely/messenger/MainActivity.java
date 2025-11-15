package com.havely.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private EditText usernameInput, messageInput;
    private Button startButton, sendButton;
    private LinearLayout chatContainer, messageInputLayout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;
    private String currentUsername = "";
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupClickListeners();
        
        // Проверяем авторизацию
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }
    
    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatContainer = findViewById(R.id.chatContainer);
        messageInputLayout = findViewById(R.id.messageInputLayout);
    }
    
    private void setupClickListeners() {
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                startAnonymousAuth(username);
            }
        });
        
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessageToFirestore(messageText);
                messageInput.setText("");
            }
        });
    }
    
    private void startAnonymousAuth(String username) {
        currentUsername = username;
        addMessage("System", "Создаем анонимный аккаунт...", "#4A0080");
        
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        currentUserId = user.getUid();
                        saveUserToFirestore(username);
                        showChatInterface();
                        setupMessagesListener();
                        addMessage("System", "✅ Анонимный аккаунт создан!", "#00E676");
                    }
                } else {
                    addMessage("System", "❌ Ошибка: " + task.getException().getMessage(), "#CF6679");
                }
            });
    }
    
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            // Загружаем данные пользователя из Firestore
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUsername = documentSnapshot.getString("username");
                        showChatInterface();
                        setupMessagesListener();
                        addMessage("System", "✅ С возвращением, " + currentUsername + "!", "#00E676");
                    }
                });
        }
    }
    
    private void saveUserToFirestore(String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("createdAt", new Date());
        user.put("lastSeen", new Date());
        
        db.collection("users").document(currentUserId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                // Успешно сохранено
            })
            .addOnFailureListener(e -> {
                addMessage("System", "❌ Ошибка сохранения пользователя", "#CF6679");
            });
    }
    
    private void sendMessageToFirestore(String messageText) {
        if (currentUserId.isEmpty()) return;
        
        Map<String, Object> message = new HashMap<>();
        message.put("userId", currentUserId);
        message.put("username", currentUsername);
        message.put("text", messageText);
        message.put("timestamp", new Date());
        
        db.collection("messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                // Сообщение отправлено
            })
            .addOnFailureListener(e -> {
                addMessage("System", "❌ Ошибка отправки", "#CF6679");
            });
    }
    
    private void setupMessagesListener() {
        messagesListener = db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException error) {
                    if (error != null) {
                        addMessage("System", "❌ Ошибка загрузки сообщений", "#CF6679");
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String username = dc.getDocument().getString("username");
                                String text = dc.getDocument().getString("text");
                                String userId = dc.getDocument().getString("userId");
                                
                                if (username != null && text != null) {
                                    String color = userId.equals(currentUserId) ? "#9D4EDD" : "#4A0080";
                                    addMessage(username, text, color);
                                }
                            }
                        }
                    }
                }
            });
    }
    
    private void showChatInterface() {
        runOnUiThread(() -> {
            usernameInput.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            chatContainer.setVisibility(View.VISIBLE);
            messageInputLayout.setVisibility(View.VISIBLE);
        });
    }
    
    private void addMessage(String sender, String message, String color) {
        runOnUiThread(() -> {
            TextView msgView = new TextView(this);
            msgView.setText(sender + ": " + message);
            msgView.setTextColor(android.graphics.Color.WHITE);
            msgView.setPadding(16, 12, 16, 12);
            msgView.setBackgroundColor(android.graphics.Color.parseColor(color));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            msgView.setLayoutParams(params);
            
            chatContainer.addView(msgView);
            chatContainer.post(() -> chatContainer.scrollTo(0, chatContainer.getBottom()));
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}
