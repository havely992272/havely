package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText usernameInput;
    private Button startButton;
    private TextView subtitle;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        // Проверяем есть ли сохранённый пользователь
        String savedUser = prefs.getString("username", "");
        if (!savedUser.isEmpty()) {
            startMainActivity(savedUser);
            return;
        }
        
        initializeViews();
        setupAnimations();
        setupClickListeners();
    }
    
    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        subtitle = findViewById(R.id.subtitle);
        
        // Красивые тени и эффекты
        usernameInput.setTranslationZ(16f);
        startButton.setTranslationZ(20f);
    }
    
    private void setupAnimations() {
        // Анимация появления
        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        
        subtitle.startAnimation(fadeIn);
        
        // Пульсирующая анимация для подзаголовка
        AlphaAnimation pulse = new AlphaAnimation(0.7f, 1f);
        pulse.setDuration(1500);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        
        new Handler().postDelayed(() -> subtitle.startAnimation(pulse), 1200);
    }
    
    private void setupClickListeners() {
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                shakeView(usernameInput);
                Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            } else if (username.length() < 2) {
                shakeView(usernameInput);
                Toast.makeText(this, "Имя слишком короткое", Toast.LENGTH_SHORT).show();
            } else {
                // Анимация нажатия
                startButton.animate()
                    .scaleX(0.95f).scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        startButton.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> processLogin(username))
                            .start();
                    })
                    .start();
            }
        });
    }
    
    private void processLogin(String username) {
        // Сохраняем пользователя
        prefs.edit().putString("username", username).apply();
        
        // Анимация загрузки
        startButton.setText("🎉");
        startButton.animate()
            .rotation(360)
            .setDuration(800)
            .withEndAction(() -> {
                startButton.setText("✅");
                new Handler().postDelayed(() -> startMainActivity(username), 500);
            })
            .start();
    }
    
    private void startMainActivity(String username) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    private void shakeView(View view) {
        view.animate()
            .translationXBy(20f)
            .setDuration(50)
            .withEndAction(() -> view.animate()
                .translationXBy(-40f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                    .translationXBy(20f)
                    .setDuration(50)
                    .start())
                .start())
            .start();
    }
}
