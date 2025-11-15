package com.havely.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {

    private EditText messageInput;
    private Button sendButton;
    private TextView onlineStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        onlineStatus = findViewById(R.id.onlineStatus);
        
        String username = getIntent().getStringExtra("username");
        onlineStatus.setText("🟢 " + username);
        
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                // Здесь будет отправка сообщения через WebSocket
                Toast.makeText(this, "Отправлено: " + message, Toast.LENGTH_SHORT).show();
                messageInput.setText("");
            }
        });
    }
}
