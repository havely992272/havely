package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends Activity {

    private ImageButton backButton, messageButton;
    private TextView profileDisplayName, profileUsername, profileBio;
    
    private FirebaseFirestore db;
    private String userId, username, displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        db = FirebaseFirestore.getInstance();
        
        // Получаем данные из Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        username = intent.getStringExtra("username");
        displayName = intent.getStringExtra("display_name");
        
        initializeViews();
        setupClickListeners();
        loadUserData();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        messageButton = findViewById(R.id.messageButton);
        profileDisplayName = findViewById(R.id.profileDisplayName);
        profileUsername = findViewById(R.id.profileUsername);
        profileBio = findViewById(R.id.profileBio);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
        
        messageButton.setOnClickListener(v -> {
            // Открываем чат с пользователем
            Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            intent.putExtra("display_name", displayName);
            startActivity(intent);
        });
    }
    
    private void loadUserData() {
        // Устанавливаем основные данные
        profileDisplayName.setText(displayName != null ? displayName : username);
        profileUsername.setText("@" + username);
        
        // Загружаем дополнительные данные из Firestore
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String bio = document.getString("user_bio");
                            if (bio != null && !bio.isEmpty()) {
                                profileBio.setText(bio);
                            }
                            
                            // Обновляем displayName если он есть в базе
                            String dbDisplayName = document.getString("display_name");
                            if (dbDisplayName != null && !dbDisplayName.isEmpty()) {
                                profileDisplayName.setText(dbDisplayName);
                            }
                        }
                    }
                });
        }
    }
    
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
