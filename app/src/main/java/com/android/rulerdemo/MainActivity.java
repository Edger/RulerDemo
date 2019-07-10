package com.android.rulerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RulerView rulerView;
    private TextView textView;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rulerView = findViewById(R.id.ruler_view);
        textView = findViewById(R.id.current_fm_channel);
        editText = findViewById(R.id.edit_text);
        button = findViewById(R.id.goto_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rulerView.setFmChannel(Double.valueOf(editText.getText().toString()));
            }
        });

        rulerView.setFmChannel(95.5);
        rulerView.setOnMoveActionListener(new RulerView.OnMoveActionListener() {
            @Override
            public void onMove(double x) {
                textView.setText("当前 FM : " + x);
            }
        });
    }
}
