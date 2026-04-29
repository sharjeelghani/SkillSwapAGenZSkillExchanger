package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView rvChatList;
    private ProgressBar progressBar;
    private TextView tvNoChats;
    private List<ChatPreviewModel> chatPreviews = new ArrayList<>();
    private ChatListAdapter adapter;

    private DatabaseReference usersRef, chatsRef, metadataRef;
    private String currentUsername;
    private MySharedprefsClass sharedPrefs;
    private Map<String, ValueEventListener> chatListeners = new HashMap<>();
    private Map<String, ValueEventListener> metaListeners = new HashMap<>();
    private Set<String> matchedUsers = new HashSet<>();
    private Set<String> historyUsers = new HashSet<>();
    private Map<String, Long> userClearedAtMap = new HashMap<>();
    private Map<String, DataSnapshot> lastChatSnapshots = new HashMap<>();

    private Handler timeUpdateHandler = new Handler();
    private Runnable timeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            timeUpdateHandler.postDelayed(this, 60000); // Update every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChatList = findViewById(R.id.rv_chat_list);
        progressBar = findViewById(R.id.progress_bar);
        tvNoChats = findViewById(R.id.tv_no_chats);
        ImageView btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatListAdapter(chatPreviews);
        rvChatList.setAdapter(adapter);

        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        usersRef = FirebaseDatabase.getInstance().getReference("user");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        metadataRef = FirebaseDatabase.getInstance().getReference("chat_metadata");

        loadInitialData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        timeUpdateHandler.postDelayed(timeUpdateRunnable, 60000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
    }

    private void loadInitialData() {
        progressBar.setVisibility(View.VISIBLE);
        
        usersRef.child(currentUsername).child("allConnections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUsers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    matchedUsers.add(ds.getKey());
                }
                refreshChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        usersRef.child(currentUsername).child("recentChats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    historyUsers.add(ds.getKey());
                }
                refreshChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String chatId = ds.getKey();
                    if (chatId != null && chatId.contains(currentUsername)) {
                        String otherUser = chatId.replace(currentUsername, "").replace("_", "");
                        if (!otherUser.isEmpty()) {
                            historyUsers.add(otherUser);
                        }
                    }
                }
                refreshChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void refreshChatList() {
        Set<String> allUsersToShow = new HashSet<>(matchedUsers);
        allUsersToShow.addAll(historyUsers);

        if (allUsersToShow.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            updateNoChatsVisibility();
        }

        for (String otherUser : allUsersToShow) {
            listenToChatMetadata(otherUser);
            listenToChat(otherUser);
        }
    }

    private void listenToChatMetadata(String otherUser) {
        String chatId = getChatId(currentUsername, otherUser);
        if (metaListeners.containsKey(chatId)) return;

        ValueEventListener metaListener = metadataRef.child(chatId).child("clearedAt").child(currentUsername)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long clearedAt = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                userClearedAtMap.put(otherUser, clearedAt);
                
                if (lastChatSnapshots.containsKey(otherUser)) {
                    processChatData(otherUser, lastChatSnapshots.get(otherUser));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        metaListeners.put(chatId, metaListener);
    }

    private void listenToChat(String otherUser) {
        String chatId = getChatId(currentUsername, otherUser);
        if (chatListeners.containsKey(chatId)) return;

        ValueEventListener listener = chatsRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastChatSnapshots.put(otherUser, snapshot);
                processChatData(otherUser, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        chatListeners.put(chatId, listener);
    }

    private void processChatData(String otherUser, DataSnapshot chatSnapshot) {
        String chatId = getChatId(currentUsername, otherUser);
        boolean hasHistory = chatSnapshot.exists();
        boolean isMatched = matchedUsers.contains(otherUser);
        
        if (!isMatched && !hasHistory) {
            removePreview(otherUser);
            return;
        }

        long clearedAt = userClearedAtMap.containsKey(otherUser) ? userClearedAtMap.get(otherUser) : 0;
        
        String lastMsgText = "You can now chat with each other!";
        long lastTime = 0;
        int unreadCount = 0;
        boolean isLastMsgFromMe = false;
        boolean isRead = false;
        boolean isDelivered = false;
        boolean hasVisibleMessages = false;

        Map<String, Object> statusUpdates = new HashMap<>();

        for (DataSnapshot ds : chatSnapshot.getChildren()) {
            if (ds.hasChild("isSessionRequest")) {
                // Handle Session Request Preview
                Long timestamp = ds.child("timestamp").getValue(Long.class);
                if (timestamp != null && timestamp > clearedAt) {
                    hasVisibleMessages = true;
                    lastTime = timestamp;
                    String senderId = ds.child("senderId").getValue(String.class);
                    String status = ds.child("status").getValue(String.class);
                    boolean read = ds.hasChild("read") && Boolean.TRUE.equals(ds.child("read").getValue(Boolean.class));
                    
                    if (currentUsername.equals(senderId)) {
                        lastMsgText = "You sent a Session Request";
                        isLastMsgFromMe = true;
                        isRead = read;
                    } else {
                        isLastMsgFromMe = false;
                        if ("PENDING".equals(status)) {
                            lastMsgText = "You have a Session Request!";
                        } else {
                            lastMsgText = "Session Request " + status.toLowerCase();
                        }
                        if (!read) unreadCount++;
                        
                        if (ds.hasChild("delivered") && !Boolean.TRUE.equals(ds.child("delivered").getValue(Boolean.class))) {
                            // statusUpdates.put(ds.getKey() + "/delivered", true); // handled in background by FCM service
                        }
                    }
                }
            } else {
                // Handle normal ChatMessage
                ChatMessage msg = ds.getValue(ChatMessage.class);
                if (msg != null && msg.getTimestamp() > clearedAt) {
                    hasVisibleMessages = true;
                    lastTime = msg.getTimestamp();
                    if (msg.getSenderId().equals(currentUsername)) {
                        lastMsgText = "You: " + msg.getText();
                        isLastMsgFromMe = true;
                        isRead = msg.isRead();
                        isDelivered = msg.isDelivered();
                    } else {
                        lastMsgText = msg.getText();
                        isLastMsgFromMe = false;
                        if (!msg.isRead()) unreadCount++;
                        if (!msg.isDelivered()) {
                            statusUpdates.put(ds.getKey() + "/delivered", true);
                        }
                    }
                }
            }
        }

        if (!hasVisibleMessages) {
            lastMsgText = "You can now chat with each other!";
            lastTime = 0;
            isLastMsgFromMe = false;
        }

        if (!statusUpdates.isEmpty()) {
            chatsRef.child(chatId).updateChildren(statusUpdates);
        }

        final long finalLastTime = lastTime;
        final String finalLastMsg = lastMsgText;
        final int finalUnread = unreadCount;
        final boolean finalIsFromMe = isLastMsgFromMe;
        final boolean finalIsRead = isRead;
        final boolean finalIsDelivered = isDelivered;

        usersRef.child(otherUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                HelperClass user = userSnapshot.getValue(HelperClass.class);
                if (user != null) {
                    updateOrAddPreview(user, finalLastMsg, finalLastTime, finalUnread, finalIsFromMe, finalIsRead, finalIsDelivered);
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateOrAddPreview(HelperClass user, String lastMsg, long time, int unread, boolean isFromMe, boolean isRead, boolean isDelivered) {
        int index = -1;
        for (int i = 0; i < chatPreviews.size(); i++) {
            if (chatPreviews.get(i).user.getUsername().equals(user.getUsername())) {
                index = i;
                break;
            }
        }

        ChatPreviewModel model = new ChatPreviewModel(user, lastMsg, time, unread, isFromMe, isRead, isDelivered);
        if (index != -1) {
            chatPreviews.set(index, model);
        } else {
            chatPreviews.add(model);
        }

        Collections.sort(chatPreviews, (c1, c2) -> Long.compare(c2.lastMessageTime, c1.lastMessageTime));
        adapter.notifyDataSetChanged();
        updateNoChatsVisibility();
    }
    
    private void removePreview(String username) {
        int index = -1;
        for (int i = 0; i < chatPreviews.size(); i++) {
            if (chatPreviews.get(i).user.getUsername().equals(username)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            chatPreviews.remove(index);
            adapter.notifyDataSetChanged();
            updateNoChatsVisibility();
        }
    }
    
    private void updateNoChatsVisibility() {
        if (chatPreviews.isEmpty()) {
            tvNoChats.setVisibility(View.VISIBLE);
        } else {
            tvNoChats.setVisibility(View.GONE);
        }
    }

    private String getChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, ValueEventListener> entry : chatListeners.entrySet()) {
            chatsRef.child(entry.getKey()).removeEventListener(entry.getValue());
        }
        for (Map.Entry<String, ValueEventListener> entry : metaListeners.entrySet()) {
            metadataRef.child(entry.getKey()).child("clearedAt").child(currentUsername).removeEventListener(entry.getValue());
        }
    }

    private String getFormattedTime(long timestamp) {
        if (timestamp <= 0) return "";
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) {
            return "just now";
        } else if (diff < 3600000) {
            long mins = diff / 60000;
            return mins == 1 ? "1 minute ago" : mins + " minutes ago";
        } else if (diff < 86400000) {
            long hours = diff / 3600000;
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (diff < 604800000) {
            long days = diff / 86400000;
            return days == 1 ? "1 day ago" : days + " days ago";
        } else {
            return DateUtils.getRelativeTimeSpanString(timestamp).toString();
        }
    }

    private static class ChatPreviewModel {
        HelperClass user;
        String lastMessage;
        long lastMessageTime;
        int unreadCount;
        boolean isLastMsgFromMe;
        boolean isRead;
        boolean isDelivered;

        ChatPreviewModel(HelperClass user, String lastMessage, long lastMessageTime, int unreadCount, boolean isFromMe, boolean isRead, boolean isDelivered) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
            this.unreadCount = unreadCount;
            this.isLastMsgFromMe = isFromMe;
            this.isRead = isRead;
            this.isDelivered = isDelivered;
        }
    }

    private class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
        private List<ChatPreviewModel> previews;

        ChatListAdapter(List<ChatPreviewModel> previews) {
            this.previews = previews;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatPreviewModel model = previews.get(position);
            HelperClass user = model.user;

            holder.tvName.setText(user.getFullName());
            holder.tvLastMessage.setText(model.lastMessage);
            
            holder.tvTime.setText(getFormattedTime(model.lastMessageTime));

            if (model.unreadCount > 0) {
                holder.tvUnreadCount.setVisibility(View.VISIBLE);
                holder.tvUnreadCount.setText(String.valueOf(model.unreadCount));
                holder.tvTime.setTextColor(getResources().getColor(R.color.teal_light));
                holder.imgStatus.setVisibility(View.GONE);
            } else {
                holder.tvUnreadCount.setVisibility(View.GONE);
                holder.tvTime.setTextColor(getResources().getColor(R.color.gray_text));
                
                if (model.isLastMsgFromMe) {
                    holder.imgStatus.setVisibility(View.VISIBLE);
                    if (model.isRead) {
                        holder.imgStatus.setImageResource(R.drawable.double_check_read_light);
                    } else if (model.isDelivered) {
                        holder.imgStatus.setImageResource(R.drawable.double_check_delivered_light);
                    } else {
                        holder.imgStatus.setImageResource(R.drawable.check_notdelivered_light);
                    }
                } else {
                    holder.imgStatus.setVisibility(View.GONE);
                }
            }

            int placeholder = (user.getGender() != null && user.getGender().equalsIgnoreCase("Female")) ? R.drawable.avatar : R.drawable.man;
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(user.getProfileImageUrl()).placeholder(placeholder).into(holder.imgProfile);
            } else {
                holder.imgProfile.setImageResource(placeholder);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("targetUsername", user.getUsername());
                intent.putExtra("targetFullName", user.getFullName());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return previews.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProfile, imgStatus;
            TextView tvName, tvLastMessage, tvTime, tvUnreadCount;
            ViewHolder(View itemView) {
                super(itemView);
                imgProfile = itemView.findViewById(R.id.img_profile);
                tvName = itemView.findViewById(R.id.tv_name);
                tvLastMessage = itemView.findViewById(R.id.tv_last_message);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
                imgStatus = itemView.findViewById(R.id.img_status);
            }
        }
    }
}
