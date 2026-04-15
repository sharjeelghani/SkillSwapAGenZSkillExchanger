package com.sharjeelsoft.skillswapagenzskillexchanger.models;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ChatActivity;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {

    private List<HelperClass> userList;
    private Context context;

    public SearchUserAdapter(List<HelperClass> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClass user = userList.get(position);
        holder.tvName.setText(user.getFullName());
        
        StringBuilder skills = new StringBuilder();
        if (user.getTeachingSkills() != null && !user.getTeachingSkills().isEmpty()) {
            skills.append("Teaches: ").append(TextUtils.join(", ", user.getTeachingSkills()));
        }
        if (user.getLearningInterests() != null && !user.getLearningInterests().isEmpty()) {
            if (skills.length() > 0) skills.append("\n");
            skills.append("Wants to learn: ").append(TextUtils.join(", ", user.getLearningInterests()));
        }
        
        if (skills.length() == 0) {
            holder.tvMessage.setText(user.getUsername());
        } else {
            holder.tvMessage.setText(skills.toString());
        }

        holder.tvTime.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", user.getUsername());
            intent.putExtra("receiverName", user.getFullName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
