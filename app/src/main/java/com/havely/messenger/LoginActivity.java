package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private boolean isDarkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("is_dark_theme", false);
        
        setContentView(R.layout.activity_login);
        applyTheme();
        
        if (prefs.getBoolean("is_logged_in", false)) {
            startMainActivity();
            return;
        }
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        themeToggle = findViewById(R.id.themeToggle);
    }
    
    private void setupClickListeners() {
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                createAnonymousAccount(username);
            }
        });
        
        themeToggle.setOnClickListener(v -> {
            toggleThemeWithAnimation();
        });
    }
    
    private void toggleThemeWithAnimation() {
        // Плавное исчезновение
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(200);
        
        View rootView = findViewById(android.R.id.content);
        rootView.startAnimation(fadeOut);
        
        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}
            
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                // Меняем тему
                isDarkTheme = !isDarkTheme;
                
                // Сохраняем настройку
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_dark_theme", isDarkTheme);
                editor.apply();
                
                // Перезагружаем активность
                recreate();
                
                // Плавное появление
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(200);
                rootView.startAnimation(fadeIn);
            }
            
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });
    }
    
    private void applyTheme() {
        int backgroundColor = isDarkTheme ? 
            getResources().getColor(R.color.black_background) : 
            getResources().getColor(R.color.white_background);
            
        int textPrimaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_primary) : 
            getResources().getColor(R.color.white_text_primary);
            
        int textSecondaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_secondary) : 
            getResources().getColor(R.color.white_text_secondary);
        
        // Применяем к корневому layout
        LinearLayout rootLayout = findViewById(android.R.id.content);
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(backgroundColor);
        }
        
        // Применяем ко всем элементам
        applyThemeToView(getWindow().getDecorView(), backgroundColor, textPrimaryColor, textSecondaryColor);
        
        // Обновляем иконку темы
        themeToggle.setText(isDarkTheme ? "☀️" : "🌙");
    }
    
    private void applyThemeToView(View view, int bgColor, int textPrimary, int textSecondary) {
        if (view instanceof LinearLayout) {
            view.setBackgroundColor(bgColor);
        }
        
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(textPrimary);
        }
        
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setTextColor(textPrimary);
            editText.setHintTextColor(textSecondary);
        }
        
        if (view instanceof Button) {
            Button button = (Button) view;
            button.setTextColor(textPrimary);
        }
        
        // Рекурсивно применяем к дочерним элементам
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyThemeToView(viewGroup.getChildAt(i), bgColor, textPrimary, textSecondary);
            }
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
