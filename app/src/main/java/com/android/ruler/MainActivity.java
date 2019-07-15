package com.android.ruler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ruler.widget.RulerView2;
import com.android.ruler.widget.IRulerView;
import com.android.ruler.widget.RulerView;

/**
 * @author Edger Lee
 * @date   2019/7/15
 */
public class MainActivity extends AppCompatActivity {

    private RulerView rulerView;
    private IRulerView rulerView2;
    private TextView currentChannelTv;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentChannelTv = findViewById(R.id.current_channel);
        editText = findViewById(R.id.edit_text);
        rulerView = findViewById(R.id.ruler_view);
        rulerView2 = findViewById(R.id.ruler_view2);

        button = findViewById(R.id.btn_set_channel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editText.getText())) {
                    Toast.makeText(MainActivity.this, "请输入频道值", Toast.LENGTH_SHORT).show();
                    return;
                }
                rulerView.setCurrentChannel(Float.valueOf(editText.getText().toString()));
                rulerView2.setCurrentChannel(Float.valueOf(editText.getText().toString()));
            }
        });

        rulerView.setOnChannelChangedListener(new RulerView.OnChannelChangedListener() {
            @Override
            public void onChannelChanged(float newChannel) {
                currentChannelTv.setText("当前 FM : " + newChannel);
            }
        });

        rulerView2.setChannelChangedListener(new RulerView2.OnChannelChangeListener() {
            @Override
            public void onChannelChanged(float newChannel) {
                currentChannelTv.setText("当前 FM : " + newChannel);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 在此处设置刻度尺的初始化刻度，避免在刻度尺初始化没有完成之前设置，导致设置失败
            rulerView.setCurrentChannel(101.0f);
            rulerView2.setCurrentChannel(101);
        }
    }
}
