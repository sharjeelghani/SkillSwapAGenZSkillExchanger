package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.models.SessionModel;

import java.util.ArrayList;
import java.util.List;

public class SessionsFragment extends Fragment {

    private RecyclerView rvSessions;
    private TextView tvNoSessions;
    private List<SessionModel> sessionsList = new ArrayList<>();
    private SessionAdapter adapter;
    private DatabaseReference sessionsRef;
    private String currentUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);
        rvSessions = view.findViewById(R.id.rv_sessions);
        tvNoSessions = view.findViewById(R.id.tv_no_sessions);

        MySharedprefsClass sharedPrefs = new MySharedprefsClass(requireContext());
        currentUsername = sharedPrefs.getStringValue("username");
        sessionsRef = FirebaseDatabase.getInstance().getReference("user").child(currentUsername).child("sessions");

        rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SessionAdapter(sessionsList);
        rvSessions.setAdapter(adapter);

        loadSessions();

        return view;
    }

    private void loadSessions() {
        sessionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                sessionsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    SessionModel session = ds.getValue(SessionModel.class);
                    if (session != null) {
                        sessionsList.add(session);
                    }
                }
                
                if (sessionsList.isEmpty()) {
                    tvNoSessions.setVisibility(View.VISIBLE);
                    rvSessions.setVisibility(View.GONE);
                } else {
                    tvNoSessions.setVisibility(View.GONE);
                    rvSessions.setVisibility(View.VISIBLE);
                    // Sort sessions: PENDING first, then by timestamp
                    sessionsList.sort((s1, s2) -> {
                        if (s1.getStatus().equals(s2.getStatus())) {
                            return Long.compare(s1.getScheduledTimestamp(), s2.getScheduledTimestamp());
                        }
                        return s1.getStatus().equals("PENDING") ? -1 : 1;
                    });
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private static class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
        private final List<SessionModel> sessions;
        SessionAdapter(List<SessionModel> sessions) { this.sessions = sessions; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_detail, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SessionModel session = sessions.get(position);
            holder.tvTitle.setText(session.getTitle());
            holder.tvOtherUser.setText("With: " + session.getOtherUserFullName());
            holder.tvDate.setText("Date: " + session.getDate());
            holder.tvTime.setText("Time: " + session.getTime());
            holder.tvStatus.setText(session.getStatus());

            // Set status background color based on status
            if ("PENDING".equals(session.getStatus())) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pending);
            } else if ("COMPLETED".equals(session.getStatus())) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_approve);
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_reject);
            }
        }

        @Override
        public int getItemCount() { return sessions.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvOtherUser, tvDate, tvTime, tvStatus;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_session_title);
                tvOtherUser = itemView.findViewById(R.id.tv_other_user);
                tvDate = itemView.findViewById(R.id.tv_session_date);
                tvTime = itemView.findViewById(R.id.tv_session_time);
                tvStatus = itemView.findViewById(R.id.tv_session_status);
            }
        }
    }
}
