package com.havely.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements WebSocketClient.MessageListener {

    private EditText usernameInput, messageInput;
    private Button startButton, sendButton;
    private LinearLayout chatContainer;
    private WebSocketClient webSocketClient;
    private String currentUsername = "";
    private static final String TAG = "Havely";

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
        
        // Сначала скрываем элементы чата
        messageInput.setVisibility(android.view.View.GONE);
        sendButton.setVisibility(android.view.View.GONE);
        chatContainer.setVisibility(android.view.View.GONE);
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
        
        // Подключаемся к WebSocket
        webSocketClient = new WebSocketClient();
        webSocketClient.connect(username, this);
        
        // Показываем сообщение о подключении
        addMessage("System", "Подключаемся к Havely сети...", "#4A0080");
    }
    
    private void sendRealMessage(String message) {
        if (webSocketClient != null) {
            webSocketClient.sendMessage(message);
            addMessage(currentUsername, message, "#9D4EDD"); // Свои сообщения фиолетовые
        }
    }
    
    private void showChatInterface() {
        runOnUiThread(() -> {
            usernameInput.setVisibility(android.view.View.GONE);
            startButton.setVisibility(android.view.View.GONE);
            messageInput.setVisibility(android.view.View.VISIBLE);
            sendButton.setVisibility(android.view.View.VISIBLE);
            chatContainer.setVisibility(android.view.View.VISIBLE);
        });
    }
    
    private void addMessage(String sender, String message, String color) {
        runOnUiThread(() -> {
            TextView msgView = new TextView(this);
            msgView.setText(sender + ": " + message);
            msgView.setTextColor(android.graphics.Color.WHITE);
            msgView.setPadding(16, 12, 16, 12);
            msgView.setBackgroundColor(android.graphics.Color.parseColor(color));
            msgView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) msgView.getLayoutParams();
            params.setMargins(16, 8, 16, 8);
            msgView.setLayoutParams(params);
            
            chatContainer.addView(msgView);
            
            // Прокручиваем вниз
            chatContainer.post(() -> {
                chatContainer.smoothScrollTo(0, chatContainer.getBottom());
            });
        });
    }
    
    // WebSocket Listeners
    @Override
    public void onConnected() {
        Log.d(TAG, "WebSocket connected successfully");
        runOnUiThread(() -> {
            showChatInterface();
            addMessage("System", "✅ Подключено к Havely! Можно общаться!", "#00E676");
            addMessage("System", "💬 Отправляйте сообщения - они идут через реальный WebSocket!", "#4A0080");
        });
    }
    
    @Override
    public void onMessageReceived(String message) {
        Log.d(TAG, "Received: " + message);
        runOnUiThread(() -> {
            addMessage("Server", message, "#2D004D"); // Сообщения от сервера тёмные
        });
    }
    
    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            addMessage("System", "❌ Соединение разорвано", "#CF6679");
        });
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
        runOnUiThread(() -> {
            addMessage("System", "💥 Ошибка: " + error, "#CF6679");
            Toast.makeText(this, "Ошибка подключения: " + error, Toast.LENGTH_LONG).show();
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
