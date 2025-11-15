package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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
    private boolean isDarkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("is_dark_theme", false);
        
        setContentView(R.layout.activity_main);
        applyTheme();
        
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
        
        Button themeToggle = drawerView.findViewById(R.id.themeToggle);
        themeToggle.setText(isDarkTheme ? "☀️" : "🌙");
        
        themeToggle.setOnClickListener(v -> {
            toggleThemeWithAnimation();
            popupWindow.dismiss();
        });
        
        // Применяем тему к меню
        applyThemeToView(drawerView);
        
        // Обработчики меню
        drawerView.findViewById(R.id.menuNewGroup).setOnClickListener(v -> popupWindow.dismiss());
        drawerView.findViewById(R.id.menuContacts).setOnClickListener(v -> popupWindow.dismiss());
        drawerView.findViewById(R.id.menuSettings).setOnClickListener(v -> popupWindow.dismiss());
    }
    
    private void toggleThemeWithAnimation() {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(200);
        
        View rootView = findViewById(android.R.id.content);
        rootView.startAnimation(fadeOut);
        
        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}
            
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                isDarkTheme = !isDarkTheme;
                
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_dark_theme", isDarkTheme);
                editor.apply();
                
                recreate();
                
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(200);
                rootView.startAnimation(fadeIn);
            }
            
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });
    }
    
    private void applyTheme() {
        int backgroundColor = isDarkTheme ? 
            getResources().getColor(R.color.black_background) : 
            getResources().getColor(R.color.white_background);
            
        int surfaceColor = isDarkTheme ? 
            getResources().getColor(R.color.black_surface) : 
            getResources().getColor(R.color.white_surface);
            
        int textPrimaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_primary) : 
            getResources().getColor(R.color.white_text_primary);
            
        int textSecondaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_secondary) : 
            getResources().getColor(R.color.white_text_secondary);
        
        // Применяем тему
        applyThemeToView(getWindow().getDecorView(), backgroundColor, surfaceColor, textPrimaryColor, textSecondaryColor);
    }
    
    private void applyThemeToView(View view) {
        int backgroundColor = isDarkTheme ? 
            getResources().getColor(R.color.black_background) : 
            getResources().getColor(R.color.white_background);
            
        int surfaceColor = isDarkTheme ? 
            getResources().getColor(R.color.black_surface) : 
            getResources().getColor(R.color.white_surface);
            
        int textPrimaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_primary) : 
            getResources().getColor(R.color.white_text_primary);
            
        int textSecondaryColor = isDarkTheme ? 
            getResources().getColor(R.color.black_text_secondary) : 
            getResources().getColor(R.color.white_text_secondary);
        
        applyThemeToView(view, backgroundColor, surfaceColor, textPrimaryColor, textSecondaryColor);
    }
    
    private void applyThemeToView(View view, int bgColor, int surfaceColor, int textPrimary, int textSecondary) {
        if (view instanceof LinearLayout) {
            if (view.getId() == R.id.emptyState) {
                view.setBackgroundColor(bgColor);
            } else {
                view.setBackgroundColor(surfaceColor);
            }
        }
        
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(textPrimary);
        }
        
        if (view instanceof Button) {
            Button button = (Button) view;
            button.setTextColor(textPrimary);
        }
        
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyThemeToView(viewGroup.getChildAt(i), bgColor, surfaceColor, textPrimary, textSecondary);
            }
        }
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        chatsRecyclerView.setVisibility(View.GONE);
    }
}
