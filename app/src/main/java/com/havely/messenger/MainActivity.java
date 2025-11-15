package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    private RecyclerView chatsRecyclerView;
    private LinearLayout emptyState;
    private ImageButton menuButton, searchButton, fabCreateChat;
    private DrawerLayout drawerLayout;
    private SharedPreferences prefs;

    private float x1, x2;
    static final int MIN_DISTANCE = 100;
    static final int EDGE_SIZE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        initializeViews();
        setupClickListeners();
        setupDrawer();
        showEmptyState();
    }
    
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        menuButton = findViewById(R.id.menuButton);
        searchButton = findViewById(R.id.searchButton);
        fabCreateChat = findViewById(R.id.fabCreateChat);
        
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String username = prefs.getString("username", "Пользователь");
        TextView drawerUsername = findViewById(R.id.drawerUsername);
        drawerUsername.setText(username);
    }
    
    private void setupClickListeners() {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawer(Gravity.START);
            } else {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        
        searchButton.setOnClickListener(v -> {
            // TODO: Открыть поиск
        });
        
        fabCreateChat.setOnClickListener(v -> {
            // TODO: Создать новый чат
        });
        
        // Обработчики меню
        findViewById(R.id.menuNewGroup).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            // TODO: Создать новую группу
        });
        
        findViewById(R.id.menuSecretChats).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            // TODO: Открыть секретные чаты
        });
        
        findViewById(R.id.menuSettings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            // TODO: Открыть настройки
        });
        
        findViewById(R.id.menuContacts).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            // TODO: Открыть контакты
        });
    }
    
    private void setupDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        
        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        
                        if (x1 < EDGE_SIZE && Math.abs(deltaX) > MIN_DISTANCE && deltaX > 0) {
                            if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                                drawerLayout.openDrawer(Gravity.START);
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }
}
