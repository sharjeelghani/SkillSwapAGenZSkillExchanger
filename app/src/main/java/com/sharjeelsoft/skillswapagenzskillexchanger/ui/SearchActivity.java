package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.models.SearchUserAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private TextView tvNoUserFound;
    private ProgressBar progressBar;
    
    private DatabaseReference usersRef;
    private List<HelperClass> allUsers;
    private SearchUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initFirebase();
        loadAllUsers();

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

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        allUsers = new ArrayList<>();
        adapter = new SearchUserAdapter(new ArrayList<>(), this);
        rvSearchResults.setAdapter(adapter);
    }

    private void initFirebase() {
        usersRef = FirebaseDatabase.getInstance().getReference("user");
    }

    private void loadAllUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass user = dataSnapshot.getValue(HelperClass.class);
                    if (user != null) {
                        allUsers.add(user);
                    }
                }
                progressBar.setVisibility(View.GONE);
                // Initial filter with empty string to show nothing or all users? 
                // Usually search starts empty.
                filterUsers(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvNoUserFound.setVisibility(View.GONE);
            return;
        }

        List<HelperClass> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (HelperClass user : allUsers) {
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

        updateUI(filteredList);
    }

    private void updateUI(List<HelperClass> filteredList) {
        if (filteredList.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvNoUserFound.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvNoUserFound.setVisibility(View.GONE);
            adapter = new SearchUserAdapter(filteredList, this);
            rvSearchResults.setAdapter(adapter);
        }
    }
}
