package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    private RecyclerView chatsRecyclerView;
    private LinearLayout emptyState;
    private Button menuButton, searchButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        initializeViews();
        setupClickListeners();
        showEmptyState();
    }
    
    private void initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        menuButton = findViewById(R.id.menuButton);
        searchButton = findViewById(R.id.searchButton);
        
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupClickListeners() {
        menuButton.setOnClickListener(v -> {
            // TODO: Открыть меню
        });
        
        searchButton.setOnClickListener(v -> {
            // TODO: Открыть поиск
        });
        
        findViewById(R.id.fabCreateChat).setOnClickListener(v -> {
            // TODO: Создать новый чат
        });
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
}
