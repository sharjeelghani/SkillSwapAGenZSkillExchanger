package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.NotificationAdapter;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.NotificationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private TextView tvNoNotifications;
    private ProgressBar progressBar;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;

    private String currentUsername;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference notificationsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        rvNotifications = view.findViewById(R.id.rv_notifications);
        tvNoNotifications = view.findViewById(R.id.tv_no_notifications);
        progressBar = view.findViewById(R.id.progress_bar);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPrefs = new MySharedprefsClass(requireContext());
        currentUsername = sharedPrefs.getStringValue("username");

        if (currentUsername != null && !currentUsername.equals("new_user")) {
            notificationsRef = FirebaseDatabase.getInstance().getReference("user")
                    .child(currentUsername).child("notifications");
            loadNotifications();
        }

        return view;
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel model = ds.getValue(NotificationModel.class);
                    if (model != null) {
                        notificationList.add(model);
                    }
                }
                Collections.reverse(notificationList); // Show newest first
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        if (notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter = new NotificationAdapter(notificationList, getContext());
            rvNotifications.setAdapter(adapter);
        }
    }
}
