package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText usernameInput;
    private Button startButton;
    private static final String TAG = "Havely";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        
        startButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите никнейм", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Log.d(TAG, "Starting ChatActivity for user: " + username);
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ChatActivity", e);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
