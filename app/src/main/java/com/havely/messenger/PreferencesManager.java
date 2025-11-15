package com.havely.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "HavelyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_BIO = "bio";
    
    private SharedPreferences prefs;
    
    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveUser(String username, String displayName) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply();
    }
    
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
    
    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "");
    }
    
    public String getBio() {
        return prefs.getString(KEY_BIO, "Расскажите о себе");
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public void updateProfile(String displayName, String bio) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_BIO, bio)
            .apply();
    }
    
    public void logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply();
    }
}
