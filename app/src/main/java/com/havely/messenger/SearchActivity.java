package com.havely.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {

    private ImageButton backButton;
    private EditText searchInput;
    private RecyclerView searchResultsRecyclerView;
    private LinearLayout emptyState, noResultsState;
    
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private List<User> searchResults = new ArrayList<>();
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("havely_prefs", MODE_PRIVATE);
        
        initializeViews();
        setupClickListeners();
        setupSearchListener();
        setupAdapter();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchInput = findViewById(R.id.searchInput);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        noResultsState = findViewById(R.id.noResultsState);
        
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupAdapter() {
        adapter = new SearchAdapter(searchResults, this);
        searchResultsRecyclerView.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 1) {
                    searchUsers(query);
                } else {
                    showEmptyState();
                }
            }
        });
    }
    
    private void searchUsers(String username) {
        // Ищем пользователей по точному совпадению юзернейма
        db.collection("users")
            .whereEqualTo("username", username.toLowerCase())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    searchResults.clear();
                    String currentUserId = prefs.getString("user_id", "");
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Пропускаем текущего пользователя
                        if (!document.getId().equals(currentUserId)) {
                            User user = new User(
                                document.getId(),
                                document.getString("username"),
                                document.getString("display_name")
                            );
                            searchResults.add(user);
                        }
                    }
                    
                    if (searchResults.isEmpty()) {
                        showNoResults();
                    } else {
                        showResults();
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchActivity.this, "Ошибка поиска", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        noResultsState.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.GONE);
    }
    
    private void showNoResults() {
        emptyState.setVisibility(View.GONE);
        noResultsState.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
    }
    
    private void showResults() {
        emptyState.setVisibility(View.GONE);
        noResultsState.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
    }
    
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
