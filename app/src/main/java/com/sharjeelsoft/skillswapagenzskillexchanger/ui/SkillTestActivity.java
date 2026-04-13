package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SkillTestActivity extends AppCompatActivity {
    private static final String TAG = "SkillTestActivity";
    private static final String API_KEY = "AIzaSyBGVawGiXGZZNc2vHd8F3mwV8Tjl6POXxU";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    private Handler handler;
    private ProgressBar progressBar;
    private TextView progresstrack, tvQuestion, btnSubmit, tvTitle;
    private RadioGroup rgOptions;
    private RadioButton[] radioButtons = new RadioButton[4];
    
    private List<Question> allQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private ArrayList<String> selectedSkills;
    private int skillIndex = 0;
    
    private int timeLeft = 30;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_test);

        selectedSkills = getIntent().getStringArrayListExtra("selectedSkills");
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
        
        loadQuestionsForCurrentSkill();

        btnSubmit.setOnClickListener(v -> checkAnswerAndNext());
    }

    private void loadQuestionsForCurrentSkill() {
        if (skillIndex >= selectedSkills.size()) {
            showReport();
            return;
        }

        String currentSkill = selectedSkills.get(skillIndex);
        tvTitle.setText("Test: " + currentSkill);
        fetchQuestionsFromAI(currentSkill);
    }

    private void fetchQuestionsFromAI(String skill) {
        tvQuestion.setText("Generating questions for " + skill + " using AI...");
        btnSubmit.setEnabled(false);

        OkHttpClient client = new OkHttpClient();
        
        String prompt = "Generate 5 multiple choice questions about " + skill + " in JSON format. " +
                "Each question should have: 'text', 'options' (array of 4 strings), and 'correctIndex' (0-3). " +
                "Return only the raw JSON array.";

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject().put("text", prompt);
            contents.put(new JSONObject().put("parts", new JSONArray().put(part)));
            jsonBody.put("contents", contents);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "AI Request Failed", e);
                    loadFallbackQuestions(skill);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resStr = response.body().string();
                        JSONObject jsonResponse = new JSONObject(resStr);
                        String text = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");
                        
                        text = text.replace("```json", "").replace("```", "").trim();
                        JSONArray questionsArray = new JSONArray(text);
                        
                        allQuestions.clear();
                        for (int i = 0; i < questionsArray.length(); i++) {
                            JSONObject q = questionsArray.getJSONObject(i);
                            JSONArray opts = q.getJSONArray("options");
                            String[] options = new String[4];
                            for(int j=0; j<4; j++) options[j] = opts.getString(j);
                            
                            allQuestions.add(new Question(q.getString("text"), options, q.getInt("correctIndex")));
                        }
                        
                        runOnUiThread(() -> {
                            currentQuestionIndex = 0;
                            btnSubmit.setEnabled(true);
                            displayQuestion();
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing AI response", e);
                        runOnUiThread(() -> loadFallbackQuestions(skill));
                    }
                } else {
                    runOnUiThread(() -> loadFallbackQuestions(skill));
                }
            }
        });
    }

    private void loadFallbackQuestions(String skill) {
        allQuestions.clear();
        allQuestions.add(new Question("What is a primary concept of " + skill + "?", 
                new String[]{"Concept A", "Concept B", "The Fundamentals", "Advanced Theory"}, 2));
        allQuestions.add(new Question("Which tool is commonly used in " + skill + "?", 
                new String[]{"Tool X", "Tool Y", "Standard IDE", "Basic Utility"}, 0));
        
        currentQuestionIndex = 0;
        btnSubmit.setEnabled(true);
        displayQuestion();
        Toast.makeText(this, "AI failed. Using local questions for " + skill, Toast.LENGTH_SHORT).show();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= allQuestions.size()) {
            skillIndex++;
            loadQuestionsForCurrentSkill();
            return;
        }

        Question q = allQuestions.get(currentQuestionIndex);
        tvQuestion.setText((currentQuestionIndex + 1) + ". " + q.text);
        rgOptions.clearCheck();
        for (int i = 0; i < 4; i++) {
            radioButtons[i].setText(q.options[i]);
        }

        startTimer();
    }

    private void startTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
        timeLeft = 30;
        progressBar.setProgress(100);
        
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timeLeft--;
                progressBar.setProgress((timeLeft * 100) / 30);
                progresstrack.setText(timeLeft + "s");
                
                if (timeLeft > 0) {
                    handler.postDelayed(this, 1000);
                } else {
                    checkAnswerAndNext();
                }
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void checkAnswerAndNext() {
        if (allQuestions.isEmpty()) return;

        int checkedId = rgOptions.getCheckedRadioButtonId();
        if (checkedId != -1) {
            View radioButton = rgOptions.findViewById(checkedId);
            int index = rgOptions.indexOfChild(radioButton);
            if (index == allQuestions.get(currentQuestionIndex).correctIndex) {
                score++;
            }
        }

        currentQuestionIndex++;
        displayQuestion();
    }

    private void showReport() {
        String skillList = TextUtils.join(", ", selectedSkills);
        String message = "Skills Tested: " + skillList + 
                        "\nTotal Score: " + score;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Evaluation Report");
        builder.setMessage(message);
        builder.setPositiveButton("Finish", (dialog, which) -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        builder.setCancelable(false);
        builder.show();
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
