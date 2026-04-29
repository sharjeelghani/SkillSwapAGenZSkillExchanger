package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionsFragment extends Fragment {

    private RecyclerView rvConnections;
    private TextView tvNoConnections;
    private ProgressBar progressBar;
    private ConnectionAdapter adapter;
    private List<HelperClass> connectionList = new ArrayList<>();

    private DatabaseReference usersRef, dbRef;
    private String currentUsername, currentUserFullName;
    private MySharedprefsClass sharedPrefs;
    private FCMV1Helper fcmv1Helper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connections, container, false);

        rvConnections = view.findViewById(R.id.rv_connections);
        tvNoConnections = view.findViewById(R.id.tv_no_connections);
        progressBar = view.findViewById(R.id.progress_bar);

        rvConnections.setLayoutManager(new GridLayoutManager(getContext(), 2));

        sharedPrefs = new MySharedprefsClass(requireContext());
        currentUsername = sharedPrefs.getStringValue("username");
        dbRef = FirebaseDatabase.getInstance().getReference();
        usersRef = dbRef.child("user");
        fcmv1Helper = new FCMV1Helper(requireContext());

        loadCurrentUserDetails();
        loadConnections();

        return view;
    }

    private void loadCurrentUserDetails() {
        usersRef.child(currentUsername).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserFullName = snapshot.getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadConnections() {
        if (currentUsername == null || currentUsername.equals("new_user")) return;

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference connectionsRef = usersRef.child(currentUsername).child("allConnections");

        connectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                connectionList.clear();
                if (snapshot.exists()) {
                    long total = snapshot.getChildrenCount();
                    final int[] loadedCount = {0};

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String username = ds.getKey();
                        if (username != null) {
                            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    loadedCount[0]++;
                                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                                    if (user != null) {
                                        connectionList.add(user);
                                    }
                                    if (loadedCount[0] == total) {
                                        updateUI();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) updateUI();
                                }
                            });
                        }
                    }
                } else {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        if (connectionList.isEmpty()) {
            tvNoConnections.setVisibility(View.VISIBLE);
            rvConnections.setVisibility(View.GONE);
        } else {
            tvNoConnections.setVisibility(View.GONE);
            rvConnections.setVisibility(View.VISIBLE);
            adapter = new ConnectionAdapter(connectionList);
            rvConnections.setAdapter(adapter);
        }
    }

    private class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder> {
        private List<HelperClass> users;

        public ConnectionAdapter(List<HelperClass> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connection_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HelperClass user = users.get(position);
            holder.tvName.setText(user.getFullName());

            int placeholder = (user.getGender() != null && user.getGender().equalsIgnoreCase("Female")) ? R.drawable.avatar : R.drawable.man;

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(placeholder)
                        .into(holder.imgProfile);
            } else {
                holder.imgProfile.setImageResource(placeholder);
            }

            holder.btnView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ViewProfileActivity.class);
                intent.putExtra("userName", user.getUsername());
                startActivity(intent);
            });

            holder.btnSchedule.setOnClickListener(v -> showSchedulePopup(user));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProfile;
            TextView tvName, btnView, btnSchedule;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProfile = itemView.findViewById(R.id.img_profile);
                tvName = itemView.findViewById(R.id.tv_name);
                btnView = itemView.findViewById(R.id.btn_view_profile);
                btnSchedule = itemView.findViewById(R.id.btn_schedule_session);
            }
        }
    }

    private void showSchedulePopup(HelperClass targetUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_schedule_session, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_session_title);
        TextView tvDate = dialogView.findViewById(R.id.tv_select_date);
        TextView tvTime = dialogView.findViewById(R.id.tv_select_time);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnSchedule = dialogView.findViewById(R.id.btn_schedule);

        final Calendar calendar = Calendar.getInstance();
        final int[] selectedYear = {calendar.get(Calendar.YEAR)};
        final int[] selectedMonth = {calendar.get(Calendar.MONTH)};
        final int[] selectedDay = {calendar.get(Calendar.DAY_OF_MONTH)};
        final int[] selectedHour = {-1};
        final int[] selectedMinute = {-1};

        tvDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                selectedYear[0] = year;
                selectedMonth[0] = month;
                selectedDay[0] = dayOfMonth;
                tvDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, selectedYear[0], selectedMonth[0], selectedDay[0]);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        tvTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                Calendar now = Calendar.getInstance();
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(selectedYear[0], selectedMonth[0], selectedDay[0], hourOfDay, minute);

                if (selectedYear[0] == now.get(Calendar.YEAR) &&
                        selectedMonth[0] == now.get(Calendar.MONTH) &&
                        selectedDay[0] == now.get(Calendar.DAY_OF_MONTH)) {

                    Calendar minTime = Calendar.getInstance();
                    minTime.add(Calendar.HOUR_OF_DAY, 1);

                    if (selectedTime.before(minTime)) {
                        Toast.makeText(requireContext(), "Sessions must be scheduled at least 1 hour in advance", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                selectedHour[0] = hourOfDay;
                selectedMinute[0] = minute;
                String format = (hourOfDay >= 12) ? "PM" : "AM";
                int hour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                tvTime.setText(String.format("%02d:%02d %s", hour, minute, format));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSchedule.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String date = tvDate.getText().toString();
            String time = tvTime.getText().toString();

            if (title.isEmpty() || date.equals("Select Date") || time.equals("Select Time")) {
                Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            sendSessionRequest(targetUser, title, date, time);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendSessionRequest(HelperClass targetUser, String title, String date, String time) {
        String requestId = dbRef.child("chats").child(getChatId(currentUsername, targetUser.getUsername())).push().getKey();
        if (requestId == null) return;

        Map<String, Object> sessionRequest = new HashMap<>();
        sessionRequest.put("id", requestId);
        sessionRequest.put("title", title);
        sessionRequest.put("date", date);
        sessionRequest.put("time", time);
        sessionRequest.put("senderId", currentUsername);
        sessionRequest.put("status", "PENDING");
        sessionRequest.put("timestamp", System.currentTimeMillis());
        sessionRequest.put("isSessionRequest", true);

        String chatId = getChatId(currentUsername, targetUser.getUsername());
        dbRef.child("chats").child(chatId).child(requestId).setValue(sessionRequest).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Session Request Sent!", Toast.LENGTH_SHORT).show();

            // Send FCM notification
            String fcmToken = targetUser.getFcmToken();
            if (fcmToken != null && !fcmToken.isEmpty()) {
                String senderName = (currentUserFullName != null) ? currentUserFullName : currentUsername;
                String message = "You have a Session Request with '" + senderName + "' at '" + time + "', '" + date + "' please respond ASAP!";
                fcmv1Helper.sendChatNotification(fcmToken, "New Session Request", message, "chat_" + currentUsername, requestId, currentUsername);
            }
        });
    }

    private String getChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }
}
