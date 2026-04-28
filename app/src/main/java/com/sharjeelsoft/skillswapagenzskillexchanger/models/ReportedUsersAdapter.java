package com.sharjeelsoft.skillswapagenzskillexchanger.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportedUsersAdapter extends RecyclerView.Adapter<ReportedUsersAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private OnReportActionListener actionListener;

    public interface OnReportActionListener {
        void onDeleteUser(Report report);
        void onSendWarning(Report report);
    }
    public ReportedUsersAdapter(List<Report> reportList, OnReportActionListener actionListener) {
        this.reportList = reportList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reported_user, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.tvReportedUsername.setText(report.getReportedUsername());
        holder.tvReportingUsername.setText(report.getReportingUsername());
        holder.tvReportCause.setText(report.getReportCause());
        holder.tvDetails.setText(report.getAdditionalDetails());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(report.getTimestamp()));
        holder.tvTimestamp.setText(dateString);

        if (report.getAdditionalDetails() == null || report.getAdditionalDetails().isEmpty()) {
            holder.tvDetails.setVisibility(View.GONE);
        } else {
            holder.tvDetails.setVisibility(View.VISIBLE);
        }

        if (report.getReportCount() > 0) {
            holder.tvReportCount.setVisibility(View.VISIBLE);
            holder.tvReportCount.setText(String.valueOf(report.getReportCount()));
        } else {
            holder.tvReportCount.setVisibility(View.GONE);
        }

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteUser(report);
            }
        });

        holder.btnSendWarning.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onSendWarning(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportedUsername, tvReportingUsername, tvReportCause, tvDetails, tvTimestamp, tvReportCount;
        ImageButton btnDeleteUser, btnSendWarning;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportedUsername = itemView.findViewById(R.id.tv_reported_username);
            tvReportingUsername = itemView.findViewById(R.id.tv_reporting_username);
            tvReportCause = itemView.findViewById(R.id.tv_report_cause);
            tvDetails = itemView.findViewById(R.id.tv_details);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvReportCount = itemView.findViewById(R.id.tv_report_count);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
            btnSendWarning = itemView.findViewById(R.id.btn_send_warning);
        }
    }
}
