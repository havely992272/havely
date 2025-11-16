package com.havely.messenger;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<User> users;
    private SearchActivity activity;

    public SearchAdapter(List<User> users, SearchActivity activity) {
        this.users = users;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        
        String displayName = user.displayName != null ? user.displayName : user.username;
        holder.displayName.setText(displayName);
        holder.username.setText("@" + user.username);
        
        // Обработчик клика на весь элемент (открывает профиль)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, UserProfileActivity.class);
            intent.putExtra("user_id", user.id);
            intent.putExtra("username", user.username);
            intent.putExtra("display_name", user.displayName);
            activity.startActivity(intent);
        });
        
        // Обработчик клика на кнопку сообщения (открывает чат)
        holder.messageButton.setOnClickListener(v -> {
            // TODO: Открыть чат с пользователем
            activity.showToast("Открыть чат с " + user.username);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView displayName;
        public TextView username;
        public ImageButton messageButton;

        public ViewHolder(View view) {
            super(view);
            displayName = view.findViewById(R.id.userDisplayName);
            username = view.findViewById(R.id.userUsername);
            messageButton = view.findViewById(R.id.messageButton);
        }
    }
}
