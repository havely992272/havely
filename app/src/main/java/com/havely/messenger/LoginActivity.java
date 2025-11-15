package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private Button startButton, themeToggle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private boolean isDarkTheme = false; // По умолчанию светлая тема

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Загружаем настройки темы
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("is_dark_theme", false);
        
        setContentView(R.layout.activity_login);
        applyTheme(); // Применяем тему после установки layout
        
        // Проверяем, если пользователь уже авторизован
        if (prefs.getBoolean("is_logged_in", false)) {
            startMainActivity();
            return;
        }
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        themeToggle = findViewById(R.id.themeToggle);
        
        // Устанавливаем иконку темы (инвертируем)
        themeToggle.setText(isDarkTheme ? "☀️" : "🌙");
        
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                createAnonymousAccount(username);
            }
        });
        
        themeToggle.setOnClickListener(v -> {
            toggleTheme();
        });
    }
    
    private void toggleTheme() {
        // Анимация перехода
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        
        View rootView = findViewById(android.R.id.content);
        rootView.startAnimation(fadeOut);
        
        // Меняем тему
        isDarkTheme = !isDarkTheme;
        
        // Сохраняем настройку
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_dark_theme", isDarkTheme);
        editor.apply();
        
        // Перезагружаем активность для применения темы
        recreate();
        
        rootView.startAnimation(fadeIn);
    }
    
    private void applyTheme() {
        // Простая реализация - основные цвета
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            int backgroundColor = isDarkTheme ? 
                getResources().getColor(R.color.dark_background) : 
                getResources().getColor(R.color.light_background);
            rootView.setBackgroundColor(backgroundColor);
        }
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
        
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                // Сохраняем в SharedPreferences
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
