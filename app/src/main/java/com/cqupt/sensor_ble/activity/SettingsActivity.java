package com.cqupt.sensor_ble.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;

public class SettingsActivity extends Activity {
    public static final String AUTO_CONNECT = "isAutoConnect";
    public static final String FLAG = "flag";
    public static final int TEMPERATURE = 1;
    public static final int HUMIDITY = 2;
    public static final int ILLUMINATION = 3;
    public static final String MIN_TEMPERATURE = "min_temperature";
    public static final String MAX_TEMPERATURE = "max_temperature";
    public static final String MIN_HUMIDITY = "min_humidity";
    public static final String MAX_HUMIDITY = "max_humidity";
    public static final String VALUE_ILLUMINATION = "value_illumination";
    private TextView isAutoConnect;
    private TextView set_temperature;
    private TextView set_humidity;
    private TextView set_illumination;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        TextView autoConnect = (TextView) findViewById(R.id.autoConnect);
        isAutoConnect = (TextView) findViewById(R.id.isAutoConnect);
        TextView temperature = (TextView) findViewById(R.id.temperature);
        set_temperature = (TextView) findViewById(R.id.set_temperature);
        TextView humidity = (TextView) findViewById(R.id.humidity);
        set_humidity = (TextView) findViewById(R.id.set_humidity);
        TextView illumination = (TextView) findViewById(R.id.illumination);
        set_illumination = (TextView) findViewById(R.id.set_illumination);
        autoConnect.setOnClickListener(new OnClickListener());
        temperature.setOnClickListener(new OnClickListener());
        humidity.setOnClickListener(new OnClickListener());
        illumination.setOnClickListener(new OnClickListener());
        try {
            sharedPreferences = this.getSharedPreferences(MainActivity.MY_DATE,
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        int min_T = sharedPreferences.getInt(MIN_TEMPERATURE, 18);
        int max_T = sharedPreferences.getInt(MAX_TEMPERATURE, 26);
        set_temperature.setText(getString(R.string.temperature_value, min_T, max_T));

        int min_H = sharedPreferences.getInt(MIN_HUMIDITY, 30);
        int max_H = sharedPreferences.getInt(MAX_HUMIDITY, 60);
        set_humidity.setText(getString(R.string.humidity_value, min_H, max_H));

        int value_I = sharedPreferences.getInt(VALUE_ILLUMINATION, 10);
        set_illumination.setText(String.valueOf(value_I));

        if (sharedPreferences.getBoolean(ChooseBooleanActivity.IS_AUTO_CONNECT, false)) {
            isAutoConnect.setText(R.string.yes);
        } else {
            isAutoConnect.setText(R.string.no);
        }

    }

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.autoConnect:
                    Intent newIntent = new Intent(SettingsActivity.this, ChooseBooleanActivity.class);
                    Bundle bundle = new Bundle(); //创建Bundle对象
                    bundle.putBoolean(AUTO_CONNECT, true);     // 标示是autoConnect 启动的新Activity
                    newIntent.putExtras(bundle);
                    startActivity(newIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
                case R.id.temperature:
                    Intent intent = new Intent(SettingsActivity.this, SelectActivity.class);
                    intent.putExtra(FLAG, TEMPERATURE);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
                case R.id.humidity:
                    Intent intent2 = new Intent(SettingsActivity.this, SelectActivity.class);
                    intent2.putExtra(FLAG, HUMIDITY);
                    startActivity(intent2);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
                case R.id.illumination:
                    Intent intent3 = new Intent(SettingsActivity.this, SelectActivity.class);
                    intent3.putExtra(FLAG, ILLUMINATION);
                    startActivity(intent3);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
            }
        }
    }

    private void closeSetting() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            closeSetting();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) { //监控/拦截菜单键
            closeSetting();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
