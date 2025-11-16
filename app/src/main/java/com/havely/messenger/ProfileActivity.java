package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends Activity {

    private ImageButton backButton, editButton;
    private EditText profileBio, profileUsernameInput;
    private TextView bioCounter;
    private SharedPreferences prefs;
    
    private boolean isEditing = false;
    private String originalBio = "";
    private String originalUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        initializeViews();
        loadProfileData();
        setupClickListeners();
        setupTextWatchers();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        profileBio = findViewById(R.id.profileBio);
        profileUsernameInput = findViewById(R.id.profileUsernameInput);
        bioCounter = findViewById(R.id.bioCounter);
    }
    
    private void loadProfileData() {
        String username = prefs.getString("username", "Пользователь");
        String bio = prefs.getString("user_bio", "");
        
        profileUsernameInput.setText(username);
        profileBio.setText(bio);
        
        originalBio = bio;
        originalUsername = username;
        
        updateBioCounter(bio.length());
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
        
        editButton.setOnClickListener(v -> {
            if (!isEditing) {
                // Включаем режим редактирования
                enableEditing();
            } else {
                // Сохраняем изменения
                saveProfile();
            }
        });
    }
    
    private void setupTextWatchers() {
        profileBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateBioCounter(s.length());
            }
        });
    }
    
    private void updateBioCounter(int length) {
        bioCounter.setText(length + "/70");
    }
    
    private void enableEditing() {
        isEditing = true;
        editButton.setImageResource(R.drawable.ic_check);
        
        profileBio.setEnabled(true);
        profileUsernameInput.setEnabled(true);
        
        // Сохраняем оригинальные значения
        originalBio = profileBio.getText().toString();
        originalUsername = profileUsernameInput.getText().toString();
        
        // Фокусируемся на имени
        profileUsernameInput.requestFocus();
    }
    
    private void disableEditing() {
        isEditing = false;
        editButton.setImageResource(R.drawable.ic_edit);
        
        profileBio.setEnabled(false);
        profileUsernameInput.setEnabled(false);
    }
    
    private void saveProfile() {
        String newBio = profileBio.getText().toString().trim();
        String newUsername = profileUsernameInput.getText().toString().trim();
        
        // Валидация
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Имя пользователя не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (newBio.length() > 70) {
            Toast.makeText(this, "Описание не может превышать 70 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Сохраняем в SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", newUsername);
        editor.putString("user_bio", newBio);
        editor.apply();
        
        disableEditing();
        
        Toast.makeText(this, "Профиль сохранен", Toast.LENGTH_SHORT).show();
        
        // Обновляем MainActivity через Intent
        Intent resultIntent = new Intent();
        resultIntent.putExtra("username", newUsername);
        setResult(RESULT_OK, resultIntent);
    }
    
    @Override
    public void onBackPressed() {
        if (isEditing) {
            // Отменяем редактирование
            profileBio.setText(originalBio);
            profileUsernameInput.setText(originalUsername);
            disableEditing();
            updateBioCounter(originalBio.length());
        } else {
            super.onBackPressed();
        }
    }
}
