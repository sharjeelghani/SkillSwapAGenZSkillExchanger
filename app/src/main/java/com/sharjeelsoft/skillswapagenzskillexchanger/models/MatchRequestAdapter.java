package com.sharjeelsoft.skillswapagenzskillexchanger.models;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ViewProfileActivity;

import java.util.List;
import java.util.Map;

public class MatchRequestAdapter extends RecyclerView.Adapter<MatchRequestAdapter.ViewHolder> {

    private List<HelperClass> userList;
    private Map<String, Long> timestamps;
    private Context context;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onConfirm(HelperClass user);
        void onDelete(HelperClass user);
    }
    public MatchRequestAdapter(List<HelperClass> userList, Map<String, Long> timestamps, Context context, OnActionClickListener listener) {
        this.userList = userList;
        this.timestamps = timestamps;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClass user = userList.get(position);
        holder.tvName.setText(user.getFullName());

        Long timestamp = timestamps.get(user.getUsername());
        if (timestamp != null) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("NA");
        }

        int placeholder = (user.getGender() != null && user.getGender().equalsIgnoreCase("Female")) ? R.drawable.avatar : R.drawable.man;
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context).load(user.getProfileImageUrl()).placeholder(placeholder).into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(placeholder);
        }

        holder.btnConfirm.setOnClickListener(v -> listener.onConfirm(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));

        holder.ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userName", user.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvTime, btnConfirm, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_user_profile);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_request_time);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
