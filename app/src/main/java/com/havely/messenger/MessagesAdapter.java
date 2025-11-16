package com.havely.messenger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;
    
    private List<ChatActivity.Message> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessagesAdapter(List<ChatActivity.Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatActivity.Message message = messages.get(position);
        return message.senderId.equals(currentUserId) ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_OUTGOING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_out, parent, false);
            return new OutgoingMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_in, parent, false);
            return new IncomingMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatActivity.Message message = messages.get(position);
        String time = timeFormat.format(message.timestamp.toDate());
        
        if (holder.getItemViewType() == TYPE_OUTGOING) {
            ((OutgoingMessageViewHolder) holder).bind(message, time);
        } else {
            ((IncomingMessageViewHolder) holder).bind(message, time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static class OutgoingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;
        ProgressBar messageProgress;
        ImageView messageStatus;
        ImageView messageStatusRead;

        OutgoingMessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.messageText);
            messageTime = view.findViewById(R.id.messageTime);
            messageProgress = view.findViewById(R.id.messageProgress);
            messageStatus = view.findViewById(R.id.messageStatus);
            messageStatusRead = view.findViewById(R.id.messageStatusRead);
        }

        void bind(ChatActivity.Message message, String time) {
            messageText.setText(message.text);
            messageTime.setText(time);
            
            // Управление статусами сообщения
            if (message.status == null || message.status.equals("sending")) {
                messageProgress.setVisibility(View.VISIBLE);
                messageStatus.setVisibility(View.GONE);
                messageStatusRead.setVisibility(View.GONE);
            } else if (message.status.equals("sent")) {
                messageProgress.setVisibility(View.GONE);
                messageStatus.setVisibility(View.VISIBLE);
                messageStatusRead.setVisibility(View.GONE);
            } else if (message.status.equals("read")) {
                messageProgress.setVisibility(View.GONE);
                messageStatus.setVisibility(View.GONE);
                messageStatusRead.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class IncomingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;

        IncomingMessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.messageText);
            messageTime = view.findViewById(R.id.messageTime);
        }

        void bind(ChatActivity.Message message, String time) {
            messageText.setText(message.text);
            messageTime.setText(time);
        }
    }
}
