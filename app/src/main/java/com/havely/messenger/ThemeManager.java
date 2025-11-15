package com.havely.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;

public class ThemeManager {
    
    public static void applyTheme(Activity activity, boolean isDarkTheme) {
        Context context = activity.getApplicationContext();
        Resources resources = context.getResources();
        
        int backgroundColor = isDarkTheme ? 
            resources.getColor(R.color.black_background) : 
            resources.getColor(R.color.white_background);
            
        int surfaceColor = isDarkTheme ? 
            resources.getColor(R.color.black_surface) : 
            resources.getColor(R.color.white_surface);
            
        int textPrimaryColor = isDarkTheme ? 
            resources.getColor(R.color.black_text_primary) : 
            resources.getColor(R.color.white_text_primary);
            
        int textSecondaryColor = isDarkTheme ? 
            resources.getColor(R.color.black_text_secondary) : 
            resources.getColor(R.color.white_text_secondary);
        
        int primaryColor = isDarkTheme ? 
            resources.getColor(R.color.black_primary) : 
            resources.getColor(R.color.white_primary);
        
        // Обновляем цвета в ресурсах
        updateColorResource(activity, R.color.app_background, backgroundColor);
        updateColorResource(activity, R.color.app_surface, surfaceColor);
        updateColorResource(activity, R.color.app_primary, primaryColor);
        updateColorResource(activity, R.color.app_text_primary, textPrimaryColor);
        updateColorResource(activity, R.color.app_text_secondary, textSecondaryColor);
    }
    
    private static void updateColorResource(Activity activity, int colorResId, int newColor) {
        try {
            // Этот метод сложно реализовать без пересоздания активности
            // В реальном приложении нужно использовать разные темы в styles.xml
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
