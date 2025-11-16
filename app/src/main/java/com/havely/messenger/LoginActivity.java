package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {

    private EditText usernameInput;
    private Button startButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        // Проверяем авторизацию
        if (prefs.getBoolean("is_logged_in", false)) {
            startMainActivity();
            return;
        }
        
        setContentView(R.layout.activity_login);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                createAnonymousAccount(username);
            }
        });
    }
    
    private void createAnonymousAccount(String username) {
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserToFirestore(user.getUid(), username);
                    }
                } else {
                    Toast.makeText(this, "Ошибка создания аккаунта", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void saveUserToFirestore(String userId, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("createdAt", System.currentTimeMillis());
        user.put("isOnline", true);
        user.put("lastSeen", com.google.firebase.Timestamp.now());
        
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("username", username);
                editor.putString("user_id", userId);
                editor.apply();
                
                startMainActivity();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
