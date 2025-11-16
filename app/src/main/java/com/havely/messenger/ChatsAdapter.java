package com.havely.messenger;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Chat> chats;
    private MainActivity activity;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatsAdapter(List<Chat> chats, MainActivity activity) {
        this.chats = chats;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        
        // TODO: Загрузить данные пользователя из participants
        holder.userName.setText("Пользователь");
        holder.lastMessage.setText(chat.lastMessage != null ? chat.lastMessage : "Нет сообщений");
        
        if (chat.lastMessageTime != null) {
            holder.time.setText(timeFormat.format(chat.lastMessageTime.toDate()));
        }
        
        // Бейдж непрочитанных
        if (chat.unreadCount > 0) {
            holder.badge.setText(String.valueOf(chat.unreadCount));
            holder.badge.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            // TODO: Открыть чат с пользователем
            activity.showToast("Открыть чат");
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView lastMessage;
        public TextView time;
        public TextView badge;

        public ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.chatUserName);
            lastMessage = view.findViewById(R.id.chatLastMessage);
            time = view.findViewById(R.id.chatTime);
            badge = view.findViewById(R.id.chatBadge);
        }
    }
}
