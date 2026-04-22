package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.models.SearchUserAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private TextView tvNoUserFound;
    private ProgressBar progressBar;
    private ImageView btnBack;
    
    private DatabaseReference usersRef;
    private List<HelperClass> matchedUsersList = new ArrayList<>();
    private List<String> myLearningInterests = new ArrayList<>();
    private List<String> myTeachingSkills = new ArrayList<>();
    private String currentUsername;
    private MySharedprefsClass sharedPrefs;
    private SearchUserAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");

        initViews();
        initFirebase();
        loadMatchingUsers();

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvNoUserFound = findViewById(R.id.tvNoUserFound);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btn_back_search);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initFirebase() {
        usersRef = FirebaseDatabase.getInstance().getReference("user");
    }

    private void loadMatchingUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUsersList.clear();
                myLearningInterests.clear();
                myTeachingSkills.clear();

                DataSnapshot me = snapshot.child(currentUsername);
                if (me.exists()) {
                    HelperClass uMe = me.getValue(HelperClass.class);
                    if (uMe != null) {
                        if (uMe.getLearningInterests() != null) {
                            myLearningInterests.addAll(uMe.getLearningInterests());
                        }
                        if (uMe.getTeachingSkills() != null) {
                            myTeachingSkills.addAll(uMe.getTeachingSkills());
                        }
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass user = dataSnapshot.getValue(HelperClass.class);
                    if (user != null && user.getUsername() != null && !user.getUsername().equals(currentUsername)) {
                        
                        // --- Mutual Skill-based Filtering (Matching Logic) ---

                        boolean otherTeachesWhatILearn = false;
                        if (user.getTeachingSkills() != null) {
                            for (String skill : user.getTeachingSkills()) {
                                if (myLearningInterests.contains(skill)) {
                                    otherTeachesWhatILearn = true;
                                    break;
                                }
                            }
                        }

                        boolean iTeachWhatOtherLearns = false;
                        if (user.getLearningInterests() != null) {
                            for (String interest : user.getLearningInterests()) {
                                if (myTeachingSkills.contains(interest)) {
                                    iTeachWhatOtherLearns = true;
                                    break;
                                }
                            }
                        }

                        if (otherTeachesWhatILearn && iTeachWhatOtherLearns) {
                            matchedUsersList.add(user);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                filterUsers(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterUsers(String query) {
        List<HelperClass> filteredList = new ArrayList<>();
        
        if (query.isEmpty()) {
            // Don't show users directly, wait for search
            updateUI(new ArrayList<>());
            return;
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (HelperClass user : matchedUsersList) {
                boolean matchesName = user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerCaseQuery);
                boolean matchesTeaching = false;
                boolean matchesLearning = false;

                if (user.getTeachingSkills() != null) {
                    for (String skill : user.getTeachingSkills()) {
                        if (skill.toLowerCase().contains(lowerCaseQuery)) {
                            matchesTeaching = true;
                            break;
                        }
                    }
                }

                if (user.getLearningInterests() != null) {
                    for (String skill : user.getLearningInterests()) {
                        if (skill.toLowerCase().contains(lowerCaseQuery)) {
                            matchesLearning = true;
                            break;
                        }
                    }
                }

                if (matchesName || matchesTeaching || matchesLearning) {
                    filteredList.add(user);
                }
            }
        }

        updateUI(filteredList);
    }

    private void updateUI(List<HelperClass> filteredList) {
        if (filteredList.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            // Only show "No user found" if the user actually typed something
            if (!etSearch.getText().toString().isEmpty()) {
                tvNoUserFound.setVisibility(View.VISIBLE);
            } else {
                tvNoUserFound.setVisibility(View.GONE);
            }
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvNoUserFound.setVisibility(View.GONE);
            
            // Custom Adapter with click listener to open ViewProfileActivity
            adapter = new SearchUserAdapter(filteredList, this) {
                @Override
                public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                    super.onBindViewHolder(holder, position);
                    HelperClass user = filteredList.get(position);
                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(SearchActivity.this, ViewProfileActivity.class);
                        intent.putExtra("userName", user.getUsername());
                        startActivity(intent);
                    });
                }
            };
            rvSearchResults.setAdapter(adapter);
        }
    }
}
