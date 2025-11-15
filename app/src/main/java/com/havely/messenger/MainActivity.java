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

public class MainActivity extends Activity implements WebSocketClient.MessageListener {

    private EditText usernameInput, messageInput;
    private Button startButton, sendButton;
    private LinearLayout chatContainer, messageInputLayout;
    private WebSocketClient webSocketClient;
    private String currentUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
                startRealChat(username);
            }
        });
        
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendRealMessage(message);
                messageInput.setText("");
            }
        });
    }
    
    private void startRealChat(String username) {
        currentUsername = username;
        webSocketClient = new WebSocketClient();
        webSocketClient.connect(username, this);
        addMessage("System", "Подключаемся к Havely...", "#4A0080");
    }
    
    private void sendRealMessage(String message) {
        if (webSocketClient != null) {
            webSocketClient.sendMessage(message);
            addMessage(currentUsername, message, "#9D4EDD");
        } else {
            Toast.makeText(this, "Нет подключения к серверу", Toast.LENGTH_SHORT).show();
        }
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
            
            // Простая прокрутка вниз (без smoothScrollTo)
            chatContainer.post(() -> {
                chatContainer.scrollTo(0, chatContainer.getBottom());
            });
        });
    }
    
    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            showChatInterface();
            addMessage("System", "✅ Подключено к Havely серверу!", "#00E676");
            addMessage("System", "💬 Теперь можно отправлять сообщения!", "#4A0080");
            Toast.makeText(this, "Подключено к серверу!", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> {
            addMessage("Server", message, "#2D004D");
        });
    }
    
    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            addMessage("System", "❌ Соединение с сервером разорвано", "#CF6679");
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            addMessage("System", "💥 Ошибка подключения: " + error, "#CF6679");
            Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }
}
