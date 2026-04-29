package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;

public class Report_User_Activity extends AppCompatActivity {

    private RadioGroup reportOptions;
    private TextView additionalLabel;
    private EditText detailsInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        reportOptions = findViewById(R.id.report_options);
        additionalLabel = findViewById(R.id.additional_label);
        detailsInput = findViewById(R.id.details_input);

        reportOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.option_other) {
                    additionalLabel.setVisibility(View.VISIBLE);
                    detailsInput.setVisibility(View.VISIBLE);

                } else {
                    additionalLabel.setVisibility(View.GONE);
                    detailsInput.setVisibility(View.GONE);
                }
            }
        });
    }
}
