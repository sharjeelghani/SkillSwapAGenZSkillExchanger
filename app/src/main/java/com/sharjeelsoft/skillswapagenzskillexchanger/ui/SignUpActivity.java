package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.app.DatePickerDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    EditText username, fullName, email, password, contact, dateofbirth;
    TextView loginRedirectText, signupButton;
    Spinner countrySpinner;
    ImageView ivEye;
    boolean isPasswordVisible = false;
    MySharedprefsClass signupPrefsClassLog;
    Context context;
    FirebaseDatabase database;
    DatabaseReference references;

    private String generatedOtp;
    private final String MAILTRAP_TOKEN = "5b87d41e19497497d94fac63e9d4cfa9";
    private final OkHttpClient client = new OkHttpClient();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        context = SignUpActivity.this;
        signupPrefsClassLog = new MySharedprefsClass(context);
        username = findViewById(R.id.et_user_name);
        fullName = findViewById(R.id.et_full_name);
        email = findViewById(R.id.et_email);
        countrySpinner = findViewById(R.id.spinner_country);
        password = findViewById(R.id.et_password);
        ivEye = findViewById(R.id.iv_eye);
        contact = findViewById(R.id.et_contact);
        dateofbirth = findViewById(R.id.et_dob);
        signupButton = findViewById(R.id.btn_sign_up);
        loginRedirectText = findViewById(R.id.btn_login_sign);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);

        dateofbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                        (view, year1, month1, dayOfMonth) -> {
                            String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                            dateofbirth.setText(date);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        ivEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivEye.setImageResource(R.drawable.ic_eye);
                } else {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivEye.setImageResource(R.drawable.ic_eye_off);
                }
                isPasswordVisible = !isPasswordVisible;
                password.setSelection(password.getText().length());
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = username.getText().toString().trim();
                String FullName = fullName.getText().toString().trim();
                String Email = email.getText().toString().trim();
                String Password = password.getText().toString().trim();
                String Contact = contact.getText().toString().trim();
                String Dateofbirth = dateofbirth.getText().toString().trim();
                String country = countrySpinner.getSelectedItem().toString();

                if (!validateData(userName, FullName, Email, Password, Contact, Dateofbirth, country)) {
                    return;
                }

                checkUserAndProceed(userName, FullName, Email, Password, Contact, Dateofbirth, country);
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkUserAndProceed(String userName, String FullName, String Email, String Password, String Contact, String Dateofbirth, String country) {
        database = FirebaseDatabase.getInstance();
        references = database.getReference("user");

        references.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username.setError("Username must be unique");
                    username.requestFocus();
                } else {
                    Query checkEmailQuery = references.orderByChild("email").equalTo(Email);
                    checkEmailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                email.setError("Email already exists");
                                email.requestFocus();
                            } else {
                                sendOtpEmail(Email, userName, FullName, Password, Contact, Dateofbirth, country);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SignUpActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtpEmail(String userEmail, String userName, String fullNameText, String pass, String cont, String dob, String country) {
        generatedOtp = String.format("%06d", new Random().nextInt(999999));

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("from", new JSONObject().put("email", "hello@denmire.store").put("name", "Skill Swap Team"));
            jsonBody.put("to", new JSONArray().put(new JSONObject().put("email", userEmail)));
            jsonBody.put("subject", "Skill Swap Verification Code");
            jsonBody.put("text", "Your verification code is: " + generatedOtp);
            jsonBody.put("category", "OTP Verification");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);
        Request request = new Request.Builder()
                .url("https://send.api.mailtrap.io/api/send")
                .post(body)
                .addHeader("Authorization", "Bearer " + MAILTRAP_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> showOtpDialog(userEmail, userName, fullNameText, pass, cont, dob, country));
                } else {
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Error sending email: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showOtpDialog(String userEmail, String userName, String fullNameText, String pass, String cont, String dob, String country) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verification, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText[] otpBoxes = {
                view.findViewById(R.id.et_otp1),
                view.findViewById(R.id.et_otp2),
                view.findViewById(R.id.et_otp3),
                view.findViewById(R.id.et_otp4),
                view.findViewById(R.id.et_otp5),
                view.findViewById(R.id.et_otp6)
        };

        setupOtpTextWatcher(otpBoxes);

        TextView tvTimer = view.findViewById(R.id.tv_timer);
        Button btnResend = view.findViewById(R.id.btn_resend);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format(Locale.getDefault(), "00:%02d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                btnResend.setVisibility(View.VISIBLE);
            }
        }.start();

        btnResend.setOnClickListener(v -> {
            dialog.dismiss();
            sendOtpEmail(userEmail, userName, fullNameText, pass, cont, dob, country);
        });

        btnConfirm.setOnClickListener(v -> {
            StringBuilder enteredOtp = new StringBuilder();
            for (EditText box : otpBoxes) {
                enteredOtp.append(box.getText().toString());
            }

            if (enteredOtp.toString().equals(generatedOtp)) {
                dialog.dismiss();
                registerUser(userName, fullNameText, userEmail, pass, cont, dob, country);
            } else {
                Toast.makeText(SignUpActivity.this, "Code is invalid", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void setupOtpTextWatcher(EditText[] otpBoxes) {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0 && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void registerUser(String userName, String FullName, String Email, String Password, String Contact, String Dateofbirth, String country) {
        HelperClass helperClass = new HelperClass(userName, FullName, Email, Password, Contact, Dateofbirth, country);
        references.child(userName).setValue(helperClass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "You have signed up successfully", Toast.LENGTH_SHORT).show();
                signupPrefsClassLog.saveStringValue("isLogin", "signed_up");
                signupPrefsClassLog.saveStringValue("username", userName); // FIXED: Added saving username
                
                // Navigate to CNIC Verification
                Intent intent = new Intent(SignUpActivity.this, ActivityCNICVarification.class);
                intent.putExtra("username", userName);
                intent.putExtra("fullName", FullName);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validateData(String userName, String FullName, String Email, String Password, String Contact, String Dateofbirth, String country) {
        if (userName.isEmpty()) {
            username.setError("User name is required");
            username.requestFocus();
            return false;
        }

        if (userName.contains(" ")) {
            username.setError("Username cannot contain spaces");
            username.requestFocus();
            return false;
        }

        if (!Pattern.compile("^[a-z0-9_]*$").matcher(userName).matches()) {
            username.setError("Username can only contain small letters, numbers and underscore (_)");
            username.requestFocus();
            return false;
        }

        if (FullName.isEmpty()) {
            fullName.setError("Full name is required");
            fullName.requestFocus();
            return false;
        }
        if (Email.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email.setError("Invalid email format");
            email.requestFocus();
            return false;
        }
        if (Contact.isEmpty()) {
            contact.setError("Contact is required");
            contact.requestFocus();
            return false;
        }
        if (Dateofbirth.isEmpty()) {
            dateofbirth.setError("Date of birth is required");
            dateofbirth.requestFocus();
            return false;
        }
        if (Password.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }

        if (Password.length() < 8) {
            password.setError("Password must be at least 8 characters long");
            password.requestFocus();
            return false;
        }
        if (!Pattern.compile("[A-Z]").matcher(Password).find()) {
            password.setError("Password must contain at least one capital letter");
            password.requestFocus();
            return false;
        }
        if (!Pattern.compile("^[a-zA-Z0-9_]*$").matcher(Password).matches()) {
            password.setError("Password cannot contain symbols except underscore (_)");
            password.requestFocus();
            return false;
        }

        if (country.equals("Select Country")) {
            Toast.makeText(this, "Please select your country", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
