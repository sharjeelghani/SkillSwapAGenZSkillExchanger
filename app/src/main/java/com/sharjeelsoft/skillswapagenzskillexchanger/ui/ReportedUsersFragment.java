package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.models.Report;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.ReportedUsersAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportedUsersFragment extends Fragment implements ReportedUsersAdapter.OnReportActionListener {

    private RecyclerView rvReportedUsers;
    private ReportedUsersAdapter adapter;
    private List<Report> reportList;
    private DatabaseReference reportsRef;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reported_users, container, false);

        rvReportedUsers = view.findViewById(R.id.rv_reported_users);
        rvReportedUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        reportList = new ArrayList<>();
        adapter = new ReportedUsersAdapter(reportList, this);
        rvReportedUsers.setAdapter(adapter);

        reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        fetchReports();

        return view;
    }

    private void fetchReports() {
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> allReports = new ArrayList<>();
                Map<String, Integer> counts = new HashMap<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Report report = dataSnapshot.getValue(Report.class);
                    if (report != null && report.getReportedUsername() != null) {
                        allReports.add(report);
                        String username = report.getReportedUsername();
                        counts.put(username, counts.getOrDefault(username, 0) + 1);
                    }
                }

                for (Report report : allReports) {
                    report.setReportCount(counts.getOrDefault(report.getReportedUsername(), 0));
                }

                reportList.clear();
                reportList.addAll(allReports);
                Collections.reverse(reportList); // Show latest reports first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load reports", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDeleteUser(Report report) {
        // Implement delete user logic here
        Toast.makeText(getContext(), "Delete user: " + report.getReportedUsername(), Toast.LENGTH_SHORT).show();
        // You would typically find the user ID and delete from "users" and "reports" nodes in Firebase
    }

    @Override
    public void onSendWarning(Report report) {
        // Implement send warning logic here
        Toast.makeText(getContext(), "Send warning to: " + report.getReportedUsername(), Toast.LENGTH_SHORT).show();
        // You would typically send a push notification or add a notification to the user's "notifications" node
    }
}
