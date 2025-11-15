package com.havely.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class MainActivity extends Activity {

    private EditText usernameInput, messageInput;
    private Button startButton, sendButton;
    private LinearLayout chatContainer, messageInputLayout;
    private String currentUsername = "";
    private FirebaseFirestore db;
    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализируем Firebase
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupClickListeners();
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
                startChat(username);
            }
        });
        
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            } else {
                Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void startChat(String username) {
        currentUsername = username;
        
        // Показываем интерфейс чата
        showChatInterface();
        
        // Загружаем предыдущие сообщения
        loadMessages();
        
        // Слушаем новые сообщения в реальном времени
        listenForMessages();
        
        addSystemMessage("🔒 Вы вошли как: " + username);
        addSystemMessage("💬 Чат готов к использованию!");
    }
    
    private void sendMessage(String message) {
        if (currentUsername.isEmpty()) return;
        
        // Создаем сообщение
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("username", currentUsername);
        messageData.put("message", message);
        messageData.put("timestamp", new Date());
        messageData.put("color", "#9D4EDD"); // Фиолетовый для своих сообщений
        
        // Сохраняем в Firebase
        db.collection("messages")
          .add(messageData)
          .addOnSuccessListener(documentReference -> {
              // Сообщение отправлено
              addMessage(currentUsername, message, "#9D4EDD");
          })
          .addOnFailureListener(e -> {
              Toast.makeText(this, "Ошибка отправки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    }
    
    private void loadMessages() {
        db.collection("messages")
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .limit(50)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                  String username = document.getString("username");
                  String message = document.getString("message");
                  String color = document.getString("color");
                  
                  if (username != null && message != null) {
                      addMessage(username, message, color != null ? color : "#2D004D");
                  }
              }
          });
    }
    
    private void listenForMessages() {
        messageListener = db.collection("messages")
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .addSnapshotListener((queryDocumentSnapshots, e) -> {
              if (e != null) {
                  return;
              }
              
              if (queryDocumentSnapshots != null) {
                  for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                      String username = document.getString("username");
                      String message = document.getString("message");
                      String color = document.getString("color");
                      
                      // Показываем только чужие сообщения (чтобы избежать дублирования)
                      if (username != null && message != null && !username.equals(currentUsername)) {
                          addMessage(username, message, color != null ? color : "#2D004D");
                      }
                  }
              }
          });
    }
    
    private void showChatInterface() {
        usernameInput.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        chatContainer.setVisibility(View.VISIBLE);
        messageInputLayout.setVisibility(View.VISIBLE);
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
            
            // Прокрутка вниз
            chatContainer.post(() -> {
                chatContainer.scrollTo(0, chatContainer.getBottom());
            });
        });
    }
    
    private void addSystemMessage(String message) {
        addMessage("System", message, "#4A0080");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
