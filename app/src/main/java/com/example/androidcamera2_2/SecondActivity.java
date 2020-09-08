package com.example.androidcamera2_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int TIME = 0;
        int NUM = 1;
        setContentView(R.layout.activity_second);

        //创建intent来接收数据，根据flag来确定是设置时间还是拍摄数量
        Intent intent = getIntent();
        int space_time = intent.getIntExtra("space_time",100);
        int capture_num = intent.getIntExtra("capture_num",100);
        final int flag = intent.getIntExtra("flag",2);

        EditText editText = findViewById(R.id.time);
        Button button = findViewById(R.id.button);

        //设置显示
        if(flag == TIME)
        {
            editText.setText(String.valueOf(space_time));
            setTitle("设置拍照间隔(ms)");
        }
        else if(flag == NUM)
        {
            editText.setText(String.valueOf(capture_num));
            setTitle("设置拍照数量(张)");
        }
        //当点击确定时
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = findViewById(R.id.time);
                try {
                    //所有的都传回去

                    //输入框的数据转int
                    int result =Integer.parseInt(editText.getText().toString());
                    Log.w("result",String.valueOf(result));
                    //开始传数据，全部传了，但接收方根据flag自行判断
                    Intent intent = new Intent();
                    intent.putExtra("space_time", result);
                    intent.putExtra("capture_num", result);
                    intent.putExtra("flag", flag);
                    //resultCode = RESULT_OK
                    setResult(RESULT_OK, intent);
                    Log.w("pause","pause");
                    //setTitle("lilei");
                    //finish();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}