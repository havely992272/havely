package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    private RecyclerView chatsRecyclerView;
    private LinearLayout emptyState;
    private Button menuButton, searchButton, themeToggle;
    private SharedPreferences prefs;
    private boolean isDarkTheme = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("is_dark_theme", true);
        
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
        
        findViewById(R.id.fabCreateChat).setClickListener(v -> {
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
        
        // Устанавливаем анимацию
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        
        // Показываем меню
        popupWindow.showAtLocation(menuButton, Gravity.START, 0, 0);
        
        // Настраиваем элементы меню
        String username = prefs.getString("username", "Пользователь");
        TextView drawerUsername = drawerView.findViewById(R.id.drawerUsername);
        drawerUsername.setText(username);
        
        Button themeToggle = drawerView.findViewById(R.id.themeToggle);
        themeToggle.setText(isDarkTheme ? "🌙" : "☀️");
        
        themeToggle.setOnClickListener(v -> {
            toggleTheme();
            popupWindow.dismiss();
        });
        
        // Обработчики других пунктов меню
        drawerView.findViewById(R.id.menuNewGroup).setOnClickListener(v -> {
            // TODO: Создать новую группу
            popupWindow.dismiss();
        });
        
        drawerView.findViewById(R.id.menuContacts).setOnClickListener(v -> {
            // TODO: Открыть контакты
            popupWindow.dismiss();
        });
        
        drawerView.findViewById(R.id.menuSettings).setOnClickListener(v -> {
            // TODO: Открыть настройки
            popupWindow.dismiss();
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
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
}
