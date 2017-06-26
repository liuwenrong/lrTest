package com.coolyota.logreport;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.log_setting_btn:

                Intent intent = new Intent(this, LogSettingActivity.class);
                startActivity(intent);

                break;

            case R.id.save_to_sdcard_btn:

                break;
        }

    }
}
