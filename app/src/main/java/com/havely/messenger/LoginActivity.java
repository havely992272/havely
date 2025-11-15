package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    
    private static final String TAG = "HavelyDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity onCreate");
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        if (prefs.getBoolean("is_logged_in", false)) {
            Log.d(TAG, "Уже авторизован, переход в MainActivity");
            startMainActivity();
            return;
        }
        
        setContentView(R.layout.activity_login);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase инициализирован");
        
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            Log.d(TAG, "Нажата кнопка, username: " + username);
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                startButton.setEnabled(false);
                createAnonymousAccount(username);
            }
        });
    }
    
    private void createAnonymousAccount(String username) {
        Log.d(TAG, "Начало анонимной аутентификации");
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Анонимная аутентификация успешна");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "User ID: " + user.getUid());
                        saveUserToFirestore(user.getUid(), username);
                    } else {
                        Log.e(TAG, "Ошибка: user is null после аутентификации");
                        startButton.setEnabled(true);
                    }
                } else {
                    Log.e(TAG, "Ошибка аутентификации: " + task.getException());
                    startButton.setEnabled(true);
                }
            });
    }
    
    private void saveUserToFirestore(String userId, String username) {
        Log.d(TAG, "Сохранение пользователя в Firestore: " + username);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("createdAt", System.currentTimeMillis());
        
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Пользователь сохранен в Firestore");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("username", username);
                editor.putString("user_id", userId);
                editor.apply();
                Log.d(TAG, "Данные сохранены в SharedPreferences");
                
                startMainActivity();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Ошибка Firestore: " + e.getMessage());
                startButton.setEnabled(true);
            });
    }
    
    private void startMainActivity() {
        Log.d(TAG, "Запуск MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
