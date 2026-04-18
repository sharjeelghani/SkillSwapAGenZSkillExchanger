package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SkillTestActivity extends AppCompatActivity {

    private static final String TAG = "SkillTestActivity";

    private String API_KEY = "";
    private String GEMINI_URL = "";

    private Handler handler;
    private ProgressBar progressBar;
    private TextView progresstrack, tvQuestion, btnSubmit, tvTitle;
    private RadioGroup rgOptions;
    private RadioButton[] radioButtons = new RadioButton[4];

    private List<Question> allQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentSkillScore = 0;
    private ArrayList<String> selectedSkills;
    private int skillIndex = 0;

    private ArrayList<String> passedSkills = new ArrayList<>();
    private ArrayList<String> teachingSkills;
    private String username;

    private int timeLeft = 30;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_test);

        MySharedprefsClass prefs = new MySharedprefsClass(this);
        String prefsUsername = prefs.getStringValue("username");
        String intentUsername = getIntent().getStringExtra("username");
        username = (intentUsername != null && !intentUsername.isEmpty()) ? intentUsername : prefsUsername;

        selectedSkills = getIntent().getStringArrayListExtra("selectedSkills");
        teachingSkills = getIntent().getStringArrayListExtra("teachingSkills");
        ArrayList<String> prevPassed = getIntent().getStringArrayListExtra("passedSkills");
        if (prevPassed != null) {
            passedSkills.addAll(prevPassed);
        }

        if (selectedSkills == null || selectedSkills.isEmpty()) {
            Toast.makeText(this, "No skills selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tvTitle);
        progressBar = findViewById(R.id.progressBar);
        progresstrack = findViewById(R.id.textView);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        btnSubmit = findViewById(R.id.btnSubmit);

        radioButtons[0] = findViewById(R.id.opt1);
        radioButtons[1] = findViewById(R.id.opt2);
        radioButtons[2] = findViewById(R.id.opt3);
        radioButtons[3] = findViewById(R.id.opt4);

        handler = new Handler(Looper.getMainLooper());

        fetchApiKeyAndStart();

        btnSubmit.setOnClickListener(v -> checkAnswerAndNext());
    }

    private void fetchApiKeyAndStart() {
        FirebaseDatabase.getInstance().getReference("api_keys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    API_KEY = snapshot.getValue(String.class);
                    GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY;
                    loadQuestionsForCurrentSkill();
                } else {
                    Toast.makeText(SkillTestActivity.this, "API Key not found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SkillTestActivity.this, "Failed to fetch API Key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestionsForCurrentSkill() {
        if (skillIndex >= selectedSkills.size()) {
            showReport();
            return;
        }

        String currentSkill = selectedSkills.get(skillIndex);
        tvTitle.setText("Assessment: " + currentSkill);
        currentSkillScore = 0;
        fetchQuestionsFromAI(currentSkill);
    }

    private void fetchQuestionsFromAI(String skill) {
        tvQuestion.setText("Generating questions for " + skill + "...");
        btnSubmit.setEnabled(false);

        OkHttpClient client = new OkHttpClient();
        String prompt = "Generate a JSON array of exactly 5 MCQs about " + skill +
                ". Format: [{\"text\":\"Question?\", \"options\":[\"A\",\"B\",\"C\",\"D\"], \"correctIndex\":0}]. " +
                "Return only the JSON array.";

        try {
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            JSONArray contents = new JSONArray().put(new JSONObject().put("parts", new JSONArray().put(part)));
            JSONObject jsonBody = new JSONObject().put("contents", contents);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> loadFallbackQuestions(skill));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String res = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> loadFallbackQuestions(skill));
                        return;
                    }
                    try {
                        JSONObject jsonResponse = new JSONObject(res);
                        String aiText = jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                        int start = aiText.indexOf("[");
                        int end = aiText.lastIndexOf("]");
                        JSONArray array = new JSONArray(aiText.substring(start, end + 1));

                        allQuestions.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject q = array.getJSONObject(i);
                            JSONArray opts = q.getJSONArray("options");
                            String[] options = new String[4];
                            for (int j = 0; j < 4; j++) options[j] = opts.getString(j);
                            allQuestions.add(new Question(q.getString("text"), options, q.getInt("correctIndex")));
                        }
                        runOnUiThread(() -> {
                            currentQuestionIndex = 0;
                            btnSubmit.setEnabled(true);
                            displayQuestion();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> loadFallbackQuestions(skill));
                    }
                }
            });
        } catch (Exception e) {
            loadFallbackQuestions(skill);
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= allQuestions.size()) {
            double percentage = (double) currentSkillScore / allQuestions.size();
            String currentSkill = selectedSkills.get(skillIndex);
            if (percentage >= 0.8) {
                if (!passedSkills.contains(currentSkill)) passedSkills.add(currentSkill);
                Toast.makeText(this, currentSkill + " Passed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, currentSkill + " Failed! Need 80% to pass.", Toast.LENGTH_SHORT).show();
            }
            skillIndex++;
            loadQuestionsForCurrentSkill();
            return;
        }

        Question q = allQuestions.get(currentQuestionIndex);
        tvQuestion.setText((currentQuestionIndex + 1) + ". " + q.text);
        rgOptions.clearCheck();
        for (int i = 0; i < 4; i++) radioButtons[i].setText(q.options[i]);
        startTimer();
    }

    private void startTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
        timeLeft = 30;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timeLeft--;
                progressBar.setProgress((timeLeft * 100) / 30);
                progresstrack.setText(timeLeft + "s");
                if (timeLeft > 0) handler.postDelayed(this, 1000);
                else checkAnswerAndNext();
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void checkAnswerAndNext() {
        if (allQuestions.isEmpty() || currentQuestionIndex >= allQuestions.size()) return;
        int checkedId = rgOptions.getCheckedRadioButtonId();
        if (checkedId != -1) {
            int index = rgOptions.indexOfChild(findViewById(checkedId));
            if (index == allQuestions.get(currentQuestionIndex).correctIndex) currentSkillScore++;
        }
        currentQuestionIndex++;
        displayQuestion();
    }

    private void loadFallbackQuestions(String skill) {
        allQuestions.clear();
        for (int i = 1; i <= 5; i++) allQuestions.add(new Question("Local Q" + i + " for " + skill, new String[]{"Correct", "Wrong", "Wrong", "Wrong"}, 0));
        currentQuestionIndex = 0;
        btnSubmit.setEnabled(true);
        displayQuestion();
    }

    private void showReport() {
        boolean allTeachingPassed = true;
        if (teachingSkills != null) {
            for (String skill : teachingSkills) {
                if (!passedSkills.contains(skill)) {
                    allTeachingPassed = false;
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder("Results:\n");
        for (String skill : selectedSkills) {
            sb.append(skill).append(": ").append(passedSkills.contains(skill) ? "PASSED" : "FAILED").append("\n");
        }

        // Update database with passed skills
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
        Map<String, Object> updates = new HashMap<>();
        updates.put("passedSkills", passedSkills);
        
        if (allTeachingPassed) {
            updates.put("isSkillsTested", true);
            updates.put("signupStage", "ACCOUNT_PENDING");
        }
        userRef.updateChildren(updates);

        boolean finalAllTeachingPassed = allTeachingPassed;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Assessment Results")
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton("Continue", (d, w) -> {
                    if (finalAllTeachingPassed) {
                        Intent intent = new Intent(this, ProfileUpdateActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, SkillSelectionActivity.class);
                        intent.putStringArrayListExtra("passedSkills", passedSkills);
                        intent.putStringArrayListExtra("teachingSkills", teachingSkills);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                    finish();
                })
                .show();
    }

    private static class Question {
        String text;
        String[] options;
        int correctIndex;
        Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }
}
