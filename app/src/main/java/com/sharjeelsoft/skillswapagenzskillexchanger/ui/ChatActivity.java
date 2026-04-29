package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.FCMV1Helper;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.SessionReminderReceiver;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.ChatAdapter;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.ChatMessage;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.SessionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView, rvSessionRequests;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvTitle, tvSubtitle, tvUnmatchedMsg;
    private ImageView btnBack, imgAvatar, btnMore;
    private View inputBar;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private SessionRequestAdapter sessionRequestAdapter;
    private final List<Map<String, Object>> sessionRequests = new ArrayList<>();

    private DatabaseReference chatRef, connectionRef, dbRef, metadataRef;
    private String currentUsername, currentUserFullName;
    private String targetUsername, targetFullName, targetFcmToken;
    private String chatId;
    private FCMV1Helper fcmv1Helper;
    private ValueEventListener messageListener, connectionListener, metadataListener;
    private long myLastClearedAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        targetUsername = getIntent().getStringExtra("targetUsername");
        targetFullName = getIntent().getStringExtra("targetFullName");

        if (targetUsername == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MySharedprefsClass sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        chatId = getChatId(currentUsername, targetUsername);
        fcmv1Helper = new FCMV1Helper(this);
        dbRef = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.recycler_messages);
        rvSessionRequests = findViewById(R.id.rv_session_requests);
        etMessage    = findViewById(R.id.et_message);
        btnSend      = findViewById(R.id.btn_send);
        tvTitle      = findViewById(R.id.tv_name);
        tvSubtitle   = findViewById(R.id.tv_subtitle);
        btnBack      = findViewById(R.id.btn_back_chat);
        imgAvatar    = findViewById(R.id.img_avatar);
        inputBar     = findViewById(R.id.input_bar);
        tvUnmatchedMsg = findViewById(R.id.tv_unmatched_msg);
        btnMore      = findViewById(R.id.btn_more_chat);

        if (tvTitle != null) tvTitle.setText(targetFullName != null ? targetFullName : targetUsername);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnMore != null) btnMore.setOnClickListener(this::showPopupMenu);

        View.OnClickListener openProfile = v -> {
            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
            intent.putExtra("userName", targetUsername);
            startActivity(intent);
        };
        if (imgAvatar != null) imgAvatar.setOnClickListener(openProfile);
        if (tvTitle != null) tvTitle.setOnClickListener(openProfile);
        if (tvSubtitle != null) tvSubtitle.setOnClickListener(openProfile);

        adapter = new ChatAdapter(messages, currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sessionRequestAdapter = new SessionRequestAdapter(sessionRequests);
        rvSessionRequests.setLayoutManager(new LinearLayoutManager(this));
        rvSessionRequests.setAdapter(sessionRequestAdapter);

        chatRef = dbRef.child("chats").child(chatId);
        metadataRef = dbRef.child("chat_metadata").child(chatId);
        connectionRef = dbRef.child("user").child(currentUsername).child("allConnections").child(targetUsername);

        loadTargetUserInfo();
        loadCurrentUserInfo();
        listenForMetadata();
        listenForConnectionStatus();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Clear chat");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Clear chat")) {
                clearChat();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void clearChat() {
        long now = System.currentTimeMillis();
        metadataRef.child("clearedAt").child(currentUsername).setValue(now).addOnSuccessListener(aVoid -> {
            Toast.makeText(ChatActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
            cleanupDatabase(now);
        });
    }

    private void cleanupDatabase(long myNow) {
        metadataRef.child("clearedAt").child(targetUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long targetClearedAt = snapshot.getValue(Long.class);
                    long minClearedAt = Math.min(myNow, targetClearedAt);
                    
                    chatRef.orderByChild("timestamp").endAt(minClearedAt).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot msgSnapshot) {
                            Map<String, Object> deletes = new HashMap<>();
                            for (DataSnapshot ds : msgSnapshot.getChildren()) {
                                deletes.put(ds.getKey(), null);
                            }
                            if (!deletes.isEmpty()) {
                                chatRef.updateChildren(deletes);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForMetadata() {
        metadataListener = metadataRef.child("clearedAt").child(currentUsername).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myLastClearedAt = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                listenForMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTargetUserInfo() {
        dbRef.child("user").child(targetUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HelperClass user = snapshot.getValue(HelperClass.class);
                        if (user != null) {
                            targetFcmToken = user.getFcmToken();
                            targetFullName = user.getFullName();
                            if (tvTitle != null && targetFullName != null) tvTitle.setText(targetFullName);
                            
                            if (tvSubtitle != null) {
                                tvSubtitle.setText(user.getCurrentJob() != null && !user.getCurrentJob().isEmpty() ? user.getCurrentJob() : "No Job Title");
                                tvSubtitle.setTextColor(getResources().getColor(R.color.black));
                            }
                            int placeholder = (user.getGender() != null && user.getGender().equalsIgnoreCase("Female")) ? R.drawable.avatar : R.drawable.man;
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(ChatActivity.this).load(user.getProfileImageUrl()).placeholder(placeholder).into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(placeholder);
                            }
                            updateUnmatchedText();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadCurrentUserInfo() {
        dbRef.child("user").child(currentUsername)
                .child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserFullName = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForMessages() {
        if (messageListener != null) chatRef.removeEventListener(messageListener);

        messageListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                sessionRequests.clear();
                Map<String, Object> statusUpdates = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.hasChild("isSessionRequest")) {
                        Map<String, Object> request = (Map<String, Object>) child.getValue();
                        if (request != null && !currentUsername.equals(request.get("senderId"))) {
                            // Mark session request as read/delivered when chat is opened
                            if (!Boolean.TRUE.equals(request.get("read"))) statusUpdates.put(child.getKey() + "/read", true);
                            if (!Boolean.TRUE.equals(request.get("delivered"))) statusUpdates.put(child.getKey() + "/delivered", true);

                            if ("PENDING".equals(request.get("status"))) {
                                sessionRequests.add(request);
                            }
                        }
                    } else {
                        ChatMessage msg = child.getValue(ChatMessage.class);
                        if (msg != null && msg.getTimestamp() > myLastClearedAt) {
                            messages.add(msg);
                            if (!msg.getSenderId().equals(currentUsername)) {
                                if (!msg.isDelivered()) statusUpdates.put(child.getKey() + "/delivered", true);
                                if (!msg.isRead()) statusUpdates.put(child.getKey() + "/read", true);
                            }
                        }
                    }
                }
                if (!statusUpdates.isEmpty()) chatRef.updateChildren(statusUpdates);
                
                rvSessionRequests.setVisibility(sessionRequests.isEmpty() ? View.GONE : View.VISIBLE);
                sessionRequestAdapter.notifyDataSetChanged();
                
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) recyclerView.scrollToPosition(messages.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForConnectionStatus() {
        connectionListener = connectionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isConnected = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                inputBar.setVisibility(isConnected ? View.VISIBLE : View.GONE);
                tvUnmatchedMsg.setVisibility(isConnected ? View.GONE : View.VISIBLE);
                updateUnmatchedText();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUnmatchedText() {
        if (tvUnmatchedMsg != null) {
            String name = targetFullName != null ? targetFullName : targetUsername;
            tvUnmatchedMsg.setText("You are unmatched with " + name + " match back to chat again..");
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String id = chatRef.push().getKey();
        if (id == null) return;

        ChatMessage msg = new ChatMessage(id, text, currentUsername, System.currentTimeMillis());
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("chats/" + chatId + "/" + id, msg);
        // Save the conversation in a recent index for both users
        updates.put("user/" + currentUsername + "/recentChats/" + targetUsername, true);
        updates.put("user/" + targetUsername + "/recentChats/" + currentUsername, true);

        dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                fcmv1Helper.sendChatNotification(targetFcmToken, 
                    currentUserFullName != null ? currentUserFullName : currentUsername, 
                    text, "chat_" + currentUsername, id, currentUsername);
            }
        });
        etMessage.setText("");
    }

    private String getChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null && messageListener != null) chatRef.removeEventListener(messageListener);
        if (connectionRef != null && connectionListener != null) connectionRef.removeEventListener(connectionListener);
        if (metadataRef != null && metadataListener != null) metadataRef.removeEventListener(metadataListener);
    }

    private class SessionRequestAdapter extends RecyclerView.Adapter<SessionRequestAdapter.ViewHolder> {
        private List<Map<String, Object>> requests;

        SessionRequestAdapter(List<Map<String, Object>> requests) { this.requests = requests; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_request, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> req = requests.get(position);
            holder.tvTitle.setText("Session: " + req.get("title"));
            holder.tvDate.setText("Date: " + req.get("date"));
            holder.tvTime.setText("Time: " + req.get("time"));

            holder.btnAccept.setOnClickListener(v -> handleRequest(req, "ACCEPTED"));
            holder.btnDecline.setOnClickListener(v -> handleRequest(req, "DECLINED"));
        }

        private void handleRequest(Map<String, Object> req, String status) {
            String reqId = (String) req.get("id");
            String title = (String) req.get("title");
            String date = (String) req.get("date");
            String time = (String) req.get("time");

            Map<String, Object> updates = new HashMap<>();
            updates.put("chats/" + chatId + "/" + reqId + "/status", status);

            if ("ACCEPTED".equals(status)) {
                long scheduledTimestamp = parseDateTime(date, time);
                SessionModel mySession = new SessionModel(reqId, title, date, time, targetUsername, targetFullName, "PENDING", scheduledTimestamp);
                SessionModel targetSession = new SessionModel(reqId, title, date, time, currentUsername, currentUserFullName, "PENDING", scheduledTimestamp);

                updates.put("user/" + currentUsername + "/sessions/" + reqId, mySession);
                updates.put("user/" + targetUsername + "/sessions/" + reqId, targetSession);
                
                // Set reminders
                setReminder(mySession);
            }

            dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                if ("DECLINED".equals(status)) {
                    String senderName = (currentUserFullName != null) ? currentUserFullName : currentUsername;
                    String message = senderName + " has declined your session request ask him why?";
                    if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                        fcmv1Helper.sendChatNotification(targetFcmToken, "Session Request Declined", message, "chat_" + currentUsername, reqId, currentUsername);
                    }
                } else if ("ACCEPTED".equals(status)) {
                    String senderName = (currentUserFullName != null) ? currentUserFullName : currentUsername;
                    String message = senderName + " has accepted your session request!";
                    if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                        fcmv1Helper.sendChatNotification(targetFcmToken, "Session Request Accepted", message, "chat_" + currentUsername, reqId, currentUsername);
                    }
                }
                String msg = status.equals("ACCEPTED") ? "Session Accepted!" : "Session Declined";
                Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        private long parseDateTime(String date, String time) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                return sdf.parse(date + " " + time).getTime();
            } catch (Exception e) {
                return 0;
            }
        }

        private void setReminder(SessionModel session) {
            long reminderTime = session.getScheduledTimestamp() - (30 * 60 * 1000); // 30 minutes before
            if (reminderTime < System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(ChatActivity.this, SessionReminderReceiver.class);
            intent.putExtra("title", session.getTitle());
            intent.putExtra("otherUserFullName", session.getOtherUserFullName());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(ChatActivity.this, session.getId().hashCode(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }

        @Override
        public int getItemCount() { return requests.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvTime, btnAccept, btnDecline;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_request_title);
                tvDate = itemView.findViewById(R.id.tv_request_date);
                tvTime = itemView.findViewById(R.id.tv_request_time);
                btnAccept = itemView.findViewById(R.id.btn_accept);
                btnDecline = itemView.findViewById(R.id.btn_decline);
            }
        }
    }
}
