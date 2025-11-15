package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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
            showDrawerMenu();
        });
        
        searchButton.setOnClickListener(v -> {
            // TODO: Открыть поиск
        });
        
        findViewById(R.id.fabCreateChat).setOnClickListener(v -> {
            // TODO: Создать новый чат
        });
    }
    
    private void showDrawerMenu() {
        View drawerView = getLayoutInflater().inflate(R.layout.drawer_menu, null);
        
        PopupWindow popupWindow = new PopupWindow(
            drawerView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        );
        
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.showAtLocation(menuButton, Gravity.START, 0, 0);
        
        // Настраиваем меню
        String username = prefs.getString("username", "Пользователь");
        TextView drawerUsername = drawerView.findViewById(R.id.drawerUsername);
        drawerUsername.setText(username);
        
        // Обработчики меню
        drawerView.findViewById(R.id.menuNewGroup).setOnClickListener(v -> popupWindow.dismiss());
        drawerView.findViewById(R.id.menuContacts).setOnClickListener(v -> popupWindow.dismiss());
        drawerView.findViewById(R.id.menuSettings).setOnClickListener(v -> popupWindow.dismiss());
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
}
