package com.cqupt.sensor_ble.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;

public class ChooseActivity extends Activity {
    public static final String CHOOSE_RESULT = "result";
    public static final String NO = "no";
    public static final String YES = "yes";
    public static final String IS_AUTO_CONNECT = "isAutoConnect";
    public static final String IS_MANUAL_SET_NOT_AUTO_CONNECT = "isManual";
    private TextView isTrue;
    private TextView isFalse;
    private boolean isAutoConnect;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose);
        TextView title = (TextView) findViewById(R.id.title);
        isTrue = (TextView) findViewById(R.id.isTrue);
        isFalse = (TextView) findViewById(R.id.isFalse);
        title.setOnClickListener(new OnClickListener());
        isTrue.setOnClickListener(new OnClickListener());
        isFalse.setOnClickListener(new OnClickListener());
        try {
            sharedPreferences = this.getSharedPreferences("myDate",
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = this.getIntent();        //获取已有的intent对象
        Bundle bundle = intent.getExtras();    //获取intent里面的bundle对象
        try {
            isAutoConnect = bundle.getBoolean(SettingsActivity.AUTO_CONNECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isAutoConnect) {
            title.setText(getString(R.string.autoConnect));
            isTrue.setText(getString(R.string.yes));
            isFalse.setText(getString(R.string.no));
        }
    }

    public void titleImageButton(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isAutoConnect) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (v == isTrue) {
                    editor.putBoolean(IS_AUTO_CONNECT, true);
                    editor.putBoolean(IS_MANUAL_SET_NOT_AUTO_CONNECT, false);
                    finishAndPutData(YES);
                } else if (v == isFalse) {
                    editor.putBoolean(IS_AUTO_CONNECT, false);
                    editor.putBoolean(IS_MANUAL_SET_NOT_AUTO_CONNECT, true);
                    finishAndPutData(NO);
                }
                editor.apply();
            }
        }
    }

    private void finishAndPutData(String noPsd) {
        Bundle b = new Bundle();
        b.putString(CHOOSE_RESULT, noPsd);
        Intent result = new Intent();
        result.putExtras(b);
        setResult(Activity.RESULT_OK, result);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
