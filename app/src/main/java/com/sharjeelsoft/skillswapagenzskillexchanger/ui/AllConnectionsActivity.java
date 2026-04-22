package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.util.ArrayList;
import java.util.List;

public class AllConnectionsActivity extends AppCompatActivity {

    private RecyclerView rvConnections;
    private TextView tvNoConnections;
    private ProgressBar progressBar;
    private ConnectionAdapter adapter;
    private List<HelperClass> connectionList = new ArrayList<>();
    
    private DatabaseReference usersRef;
    private String currentUsername;
    private MySharedprefsClass sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_connections);

        rvConnections = findViewById(R.id.rv_connections);
        tvNoConnections = findViewById(R.id.tv_no_connections);
        progressBar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        rvConnections.setLayoutManager(new GridLayoutManager(this, 2));
        
        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        usersRef = FirebaseDatabase.getInstance().getReference("user");

        loadConnections();
    }

    private void loadConnections() {
        if (currentUsername == null || currentUsername.equals("new_user")) return;

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference connectionsRef = usersRef.child(currentUsername).child("allConnections");

        connectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectionList.clear();
                if (snapshot.exists()) {
                    long total = snapshot.getChildrenCount();
                    final int[] loaded = {0};

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String username = ds.getKey();
                        if (username != null) {
                            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    loaded[0]++;
                                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                                    if (user != null) {
                                        connectionList.add(user);
                                    }
                                    if (loaded[0] == total) {
                                        updateUI();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loaded[0]++;
                                    if (loaded[0] == total) updateUI();
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
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllConnectionsActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(AllConnectionsActivity.this, ViewProfileActivity.class);
                intent.putExtra("userName", user.getUsername());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProfile;
            TextView tvName, btnView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProfile = itemView.findViewById(R.id.img_profile);
                tvName = itemView.findViewById(R.id.tv_name);
                btnView = itemView.findViewById(R.id.btn_view_profile);
            }
        }
    }
}
