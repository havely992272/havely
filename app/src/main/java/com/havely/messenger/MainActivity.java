package com.havely.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecyclerView chatsRecyclerView;
    private LinearLayout emptyState;
    private ImageButton menuButton, searchButton, fabCreateChat;
    private DrawerLayout drawerLayout;
    private SharedPreferences prefs;
    private TextView drawerUsername;
    
    private FirebaseFirestore db;
    private ChatsAdapter adapter;
    private List<Chat> chats = new ArrayList<>();
    private ListenerRegistration chatsListener;

    private float x1, x2;
    static final int MIN_DISTANCE = 100;
    static final int EDGE_SIZE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupClickListeners();
        setupDrawer();
        setupChats();
    }
    
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        menuButton = findViewById(R.id.menuButton);
        searchButton = findViewById(R.id.searchButton);
        fabCreateChat = findViewById(R.id.fabCreateChat);
        drawerUsername = findViewById(R.id.drawerUsername);
        
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatsAdapter(chats, this);
        chatsRecyclerView.setAdapter(adapter);
        
        updateDrawerUsername();
    }
    
    private void setupChats() {
        String currentUserId = prefs.getString("user_id", "");
        
        // Слушаем чаты пользователя в реальном времени
        chatsListener = db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    
                    chats.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.id = doc.getId();
                            chats.add(chat);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (chats.isEmpty()) {
                        showEmptyState();
                    } else {
                        showChatsList();
                    }
                });
    }
    
    private void updateDrawerUsername() {
        // Используем display_name если есть, иначе username
        String displayName = prefs.getString("display_name", "");
        String username = prefs.getString("username", "Пользователь");
        
        if (!displayName.isEmpty()) {
            drawerUsername.setText(displayName);
        } else {
            drawerUsername.setText(username);
        }
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
            // Открываем экран поиска
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        
        fabCreateChat.setOnClickListener(v -> {
            // TODO: Создать новый чат
        });
        
        // Profile
        findViewById(R.id.menuProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivityForResult(intent, 1);
        });
        
        // Other menu items
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
    
    private void showChatsList() {
        emptyState.setVisibility(View.GONE);
        chatsRecyclerView.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Обновляем имя пользователя после редактирования профиля
            updateDrawerUsername();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем имя при возвращении на экран
        updateDrawerUsername();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }
    
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
