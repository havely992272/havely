package com.havely.messenger;

import android.app.Activity;
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

    private ImageButton backButton, editSaveButton;
    private EditText profileBio, profileUsernameInput;
    private TextView profileUsername, bioCounter;
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
        editSaveButton = findViewById(R.id.editSaveButton);
        profileBio = findViewById(R.id.profileBio);
        profileUsernameInput = findViewById(R.id.profileUsernameInput);
        profileUsername = findViewById(R.id.profileUsername);
        bioCounter = findViewById(R.id.bioCounter);
    }
    
    private void loadProfileData() {
        String username = prefs.getString("username", "Пользователь");
        String bio = prefs.getString("user_bio", "");
        
        profileUsername.setText(username);
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
        
        editSaveButton.setOnClickListener(v -> {
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
        bioCounter.setText(length + "/64");
    }
    
    private void enableEditing() {
        isEditing = true;
        editSaveButton.setImageResource(R.drawable.ic_check);
        
        profileBio.setEnabled(true);
        profileUsernameInput.setEnabled(true);
        
        // Сохраняем оригинальные значения
        originalBio = profileBio.getText().toString();
        originalUsername = profileUsernameInput.getText().toString();
    }
    
    private void disableEditing() {
        isEditing = false;
        editSaveButton.setImageResource(R.drawable.ic_edit);
        
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
        
        if (newBio.length() > 64) {
            Toast.makeText(this, "Описание не может превышать 64 символа", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Сохраняем в SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", newUsername);
        editor.putString("user_bio", newBio);
        editor.apply();
        
        // Обновляем отображение
        profileUsername.setText(newUsername);
        
        disableEditing();
        
        Toast.makeText(this, "Профиль сохранен", Toast.LENGTH_SHORT).show();
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
