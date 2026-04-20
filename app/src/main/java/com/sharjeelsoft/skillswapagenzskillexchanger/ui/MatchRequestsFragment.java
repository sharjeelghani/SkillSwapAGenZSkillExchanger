package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.MatchRequestAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchRequestsFragment extends Fragment {

    private RecyclerView rvMatchRequests;
    private TextView tvNoRequests;
    private ProgressBar progressBar;
    private MatchRequestAdapter adapter;
    private List<HelperClass> requestUsers = new ArrayList<>();
    private Map<String, Long> requestTimestamps = new HashMap<>();

    private DatabaseReference usersRef;
    private String currentUsername;
    private MySharedprefsClass sharedPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_requests, container, false);

        rvMatchRequests = view.findViewById(R.id.rv_match_requests);
        tvNoRequests = view.findViewById(R.id.tv_no_requests);
        progressBar = view.findViewById(R.id.progress_bar);

        rvMatchRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPrefs = new MySharedprefsClass(requireContext());
        currentUsername = sharedPrefs.getStringValue("username");
        usersRef = FirebaseDatabase.getInstance().getReference("user");

        loadMatchRequests();

        return view;
    }

    private void loadMatchRequests() {
        if (currentUsername == null || currentUsername.equals("new_user")) return;

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference requestsRef = usersRef.child(currentUsername).child("matchRequests").child("received");

        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                requestUsers.clear();
                requestTimestamps.clear();

                if (snapshot.exists()) {
                    long totalRequests = snapshot.getChildrenCount();
                    final int[] loadedCount = {0};

                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                        String senderUsername = requestSnapshot.getKey();
                        Long timestamp = requestSnapshot.getValue(Long.class);
                        
                        if (senderUsername != null) {
                            requestTimestamps.put(senderUsername, timestamp);
                            usersRef.child(senderUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    loadedCount[0]++;
                                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                                    if (user != null) {
                                        requestUsers.add(user);
                                    }

                                    if (loadedCount[0] == totalRequests) {
                                        updateUI();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalRequests) updateUI();
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
        if (requestUsers.isEmpty()) {
            tvNoRequests.setVisibility(View.VISIBLE);
            rvMatchRequests.setVisibility(View.GONE);
        } else {
            tvNoRequests.setVisibility(View.GONE);
            rvMatchRequests.setVisibility(View.VISIBLE);
            adapter = new MatchRequestAdapter(requestUsers, requestTimestamps, getContext(), new MatchRequestAdapter.OnActionClickListener() {
                @Override
                public void onConfirm(HelperClass user) {
                    acceptRequest(user);
                }

                @Override
                public void onDelete(HelperClass user) {
                    deleteRequest(user);
                }
            });
            rvMatchRequests.setAdapter(adapter);
        }
    }

    private void acceptRequest(HelperClass sender) {
        String senderUsername = sender.getUsername();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updates = new HashMap<>();
        // Add to connections
        updates.put("user/" + currentUsername + "/allConnections/" + senderUsername, true);
        updates.put("user/" + senderUsername + "/allConnections/" + currentUsername, true);
        
        // Remove from match requests
        updates.put("user/" + currentUsername + "/matchRequests/received/" + senderUsername, null);
        updates.put("user/" + senderUsername + "/matchRequests/sent/" + currentUsername, null);

        db.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Connected with " + sender.getFullName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRequest(HelperClass sender) {
        String senderUsername = sender.getUsername();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/matchRequests/received/" + senderUsername, null);
        updates.put("user/" + senderUsername + "/matchRequests/sent/" + currentUsername, null);

        db.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Request deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
