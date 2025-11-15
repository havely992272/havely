package com.havely.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
    
    private static final String TAG = "HavelyDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "onCreate: Starting app");
        
        // Инициализация Firebase
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase init error: " + e.getMessage());
            showError("Firebase error: " + e.getMessage());
        }
        
        initializeViews();
        setupClickListeners();
        
        // Проверяем существующую авторизацию
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already authenticated");
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
        
        Log.d(TAG, "Views initialized");
    }
    
    private void setupClickListeners() {
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            Log.d(TAG, "Start button clicked, username: " + username);
            
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                startAnonymousAuth(username);
            }
        });
        
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Log.d(TAG, "Sending message: " + messageText);
                sendMessageToFirestore(messageText);
                messageInput.setText("");
            }
        });
    }
    
    private void startAnonymousAuth(String username) {
        currentUsername = username;
        addMessage("System", "Создаем анонимный аккаунт...", "#4A0080");
        Log.d(TAG, "Starting anonymous auth for: " + username);
        
        if (mAuth == null) {
            Log.e(TAG, "FirebaseAuth is null!");
            showError("Firebase not initialized");
            return;
        }
        
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        currentUserId = user.getUid();
                        Log.d(TAG, "Auth successful, UID: " + currentUserId);
                        saveUserToFirestore(username);
                    } else {
                        Log.e(TAG, "Auth successful but user is null");
                        showError("User is null after auth");
                    }
                } else {
                    Log.e(TAG, "Auth failed: " + task.getException().getMessage());
                    showError("Auth failed: " + task.getException().getMessage());
                }
            });
    }
    
    private void saveUserToFirestore(String username) {
        Log.d(TAG, "Saving user to Firestore: " + username);
        
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("createdAt", new Date());
        user.put("lastSeen", new Date());
        
        db.collection("users").document(currentUserId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User saved to Firestore");
                showChatInterface();
                setupMessagesListener();
                addMessage("System", "✅ Анонимный аккаунт создан!", "#00E676");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving user: " + e.getMessage());
                showError("Error saving user: " + e.getMessage());
            });
    }
    
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            Log.d(TAG, "Loading user data for: " + currentUserId);
            
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUsername = documentSnapshot.getString("username");
                        Log.d(TAG, "User data loaded: " + currentUsername);
                        showChatInterface();
                        setupMessagesListener();
                        addMessage("System", "✅ С возвращением, " + currentUsername + "!", "#00E676");
                    } else {
                        Log.d(TAG, "No user data found, showing login");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                });
        }
    }
    
    private void setupMessagesListener() {
        Log.d(TAG, "Setting up messages listener");
        
        messagesListener = db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Messages listener error: " + error.getMessage());
                    return;
                }
                
                if (querySnapshot != null) {
                    Log.d(TAG, "New messages detected: " + querySnapshot.getDocumentChanges().size());
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
            });
    }
    
    private void sendMessageToFirestore(String messageText) {
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot send message - user ID is empty");
            return;
        }
        
        Log.d(TAG, "Sending message to Firestore: " + messageText);
        
        Map<String, Object> message = new HashMap<>();
        message.put("userId", currentUserId);
        message.put("username", currentUsername);
        message.put("text", messageText);
        message.put("timestamp", new Date());
        
        db.collection("messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Message sent successfully");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending message: " + e.getMessage());
                showError("Error sending: " + e.getMessage());
            });
    }
    
    private void showChatInterface() {
        Log.d(TAG, "Showing chat interface");
        runOnUiThread(() -> {
            usernameInput.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            chatContainer.setVisibility(View.VISIBLE);
            messageInputLayout.setVisibility(View.VISIBLE);
        });
    }
    
    private void addMessage(String sender, String message, String color) {
        Log.d(TAG, "Adding message: " + sender + ": " + message);
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
    
    private void showError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            addMessage("System", "❌ " + error, "#CF6679");
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
