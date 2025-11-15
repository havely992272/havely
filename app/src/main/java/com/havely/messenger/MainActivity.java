package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
    private Button menuButton, searchButton;
    private SharedPreferences prefs;
    private boolean isDarkTheme = false; // По умолчанию светлая тема

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Загружаем настройки темы
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("is_dark_theme", false);
        
        setContentView(R.layout.activity_main);
        applyTheme(); // Применяем тему после установки layout
        
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
        
        // Устанавливаем анимацию
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        
        // Показываем меню
        popupWindow.showAtLocation(menuButton, Gravity.START, 0, 0);
        
        // Настраиваем элементы меню
        String username = prefs.getString("username", "Пользователь");
        TextView drawerUsername = drawerView.findViewById(R.id.drawerUsername);
        drawerUsername.setText(username);
        
        Button themeToggle = drawerView.findViewById(R.id.themeToggle);
        themeToggle.setText(isDarkTheme ? "☀️" : "🌙"); // Инвертируем иконку
        
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
        
        // Применяем тему к меню
        applyThemeToView(drawerView);
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
    
    private void applyTheme() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            applyThemeToView(rootView);
        }
    }
    
    private void applyThemeToView(View view) {
        int backgroundColor = isDarkTheme ? 
            getResources().getColor(R.color.dark_background) : 
            getResources().getColor(R.color.light_background);
        
        int surfaceColor = isDarkTheme ? 
            getResources().getColor(R.color.dark_surface) : 
            getResources().getColor(R.color.light_surface);
            
        int textPrimaryColor = isDarkTheme ? 
            getResources().getColor(R.color.dark_text_primary) : 
            getResources().getColor(R.color.light_text_primary);
            
        int textSecondaryColor = isDarkTheme ? 
            getResources().getColor(R.color.dark_text_secondary) : 
            getResources().getColor(R.color.light_text_secondary);
        
        int primaryColor = isDarkTheme ? 
            getResources().getColor(R.color.dark_primary) : 
            getResources().getColor(R.color.light_primary);
        
        // Применяем цвета ко всем элементам
        applyColorsToViewTree(view, backgroundColor, surfaceColor, textPrimaryColor, textSecondaryColor, primaryColor);
    }
    
    private void applyColorsToViewTree(View view, int bgColor, int surfaceColor, int textPrimary, int textSecondary, int primaryColor) {
        if (view instanceof LinearLayout) {
            if (view.getId() == R.id.emptyState || "emptyState".equals(view.getTag())) {
                view.setBackgroundColor(bgColor);
            } else if (view.getId() == android.R.id.content) {
                view.setBackgroundColor(bgColor);
            }
        }
        
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (textView.getId() == R.id.drawerUsername || "username".equals(textView.getTag())) {
                textView.setTextColor(textPrimary);
            } else {
                textView.setTextColor(textPrimary);
            }
        }
        
        if (view instanceof Button) {
            Button button = (Button) view;
            if (button.getId() == R.id.menuButton || button.getId() == R.id.searchButton) {
                button.setTextColor(textPrimary);
                button.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        
        // Рекурсивно применяем к дочерним элементам
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyColorsToViewTree(viewGroup.getChildAt(i), bgColor, surfaceColor, textPrimary, textSecondary, primaryColor);
            }
        }
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
}
