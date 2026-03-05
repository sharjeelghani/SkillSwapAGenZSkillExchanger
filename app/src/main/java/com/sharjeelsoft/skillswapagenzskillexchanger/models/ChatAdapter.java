package com.sharjeelsoft.skillswapagenzskillexchanger.models;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;

    private final List<ChatMessage> messages;
    private final String currentUserId;

    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_OUTGOING) {
            View v = inflater.inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String time = DateFormat.format("h:mm a", msg.getTimestamp()).toString();

        if (holder instanceof OutgoingHolder) {
            ((OutgoingHolder) holder).message.setText(msg.getText());
            ((OutgoingHolder) holder).time.setText(time);
        } else {
            ((IncomingHolder) holder).message.setText(msg.getText());
            ((IncomingHolder) holder).time.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class IncomingHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        IncomingHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.tv_message);
            time    = itemView.findViewById(R.id.tv_time);
        }
    }

    static class OutgoingHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        OutgoingHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.tv_message);
            time    = itemView.findViewById(R.id.tv_time);
        }
    }
}
