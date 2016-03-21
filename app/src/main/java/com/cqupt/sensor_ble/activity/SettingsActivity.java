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
    private static final int REQUEST_AUTO_CONNECT = 2;
    public static final String AUTO_CONNECT = "isAutoConnect";
    private TextView autoConnect;
    private TextView isAutoConnect;
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
        autoConnect = (TextView) findViewById(R.id.autoConnect);
        isAutoConnect = (TextView) findViewById(R.id.isAutoConnect);
        autoConnect.setOnClickListener(new OnClickListener());
        try {
            sharedPreferences = this.getSharedPreferences("myDate",
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean(ChooseActivity.IS_AUTO_CONNECT, false)) {
            isAutoConnect.setText(R.string.yes);
        } else {
            isAutoConnect.setText(R.string.no);
        }

    }

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
         if (v == autoConnect) {
                Intent newIntent = new Intent(SettingsActivity.this, ChooseActivity.class);
                Bundle bundle = new Bundle(); //创建Bundle对象
                bundle.putBoolean(AUTO_CONNECT, true);     // 标示是autoConnect 启动的新Activity
                newIntent.putExtras(bundle);
                startActivityForResult(newIntent, REQUEST_AUTO_CONNECT);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AUTO_CONNECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String result = data.getStringExtra(ChooseActivity.CHOOSE_RESULT);
                    if ((ChooseActivity.NO).equals(result)) {
                        isAutoConnect.setText(getString(R.string.no));
                    } else if ((ChooseActivity.YES).equals(result)) {
                        isAutoConnect.setText(getString(R.string.yes));
                    }
                }
                break;
            default:
                break;
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
