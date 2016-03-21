package com.cqupt.sensor_ble.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;

import java.lang.reflect.Field;

public class SelectActivity extends Activity implements NumberPicker.OnValueChangeListener {
    private String MIN;
    private String MAX;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select);
        try {
            sharedPreferences = this.getSharedPreferences(MainActivity.MY_DATE,
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NumberPicker low_numberPicker = (NumberPicker) findViewById(R.id.low_numberPicker);
        //noinspection deprecation
        setNumberPickerTextColor(low_numberPicker, getResources().getColor(R.color.white));
        low_numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //加上这句话就可以禁止
        low_numberPicker.setOnValueChangedListener(this);

        NumberPicker high_numberPicker = (NumberPicker) findViewById(R.id.high_numberPicker);
        //noinspection deprecation
        setNumberPickerTextColor(high_numberPicker, getResources().getColor(R.color.white));
        high_numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //加上这句话就可以禁止
        high_numberPicker.setOnValueChangedListener(this);
        TextView title = (TextView) findViewById(R.id.title);
        Intent intent = getIntent();
        switch (intent.getIntExtra(SettingsActivity.FLAG, 0)) {
            case SettingsActivity.TEMPERATURE:
                title.setText(getString(R.string.title_temperature));
                low_numberPicker.setMinValue(0);
                low_numberPicker.setMaxValue(21);
                high_numberPicker.setMinValue(22);
                high_numberPicker.setMaxValue(60);
                MIN = SettingsActivity.MIN_TEMPERATURE;
                MAX = SettingsActivity.MAX_TEMPERATURE;
                int min_T = sharedPreferences.getInt(SettingsActivity.MIN_TEMPERATURE, 18);
                int max_T = sharedPreferences.getInt(SettingsActivity.MAX_TEMPERATURE, 26);
                low_numberPicker.setValue(min_T);
                high_numberPicker.setValue(max_T);
                break;
            case SettingsActivity.HUMIDITY:
                title.setText(getString(R.string.title_humidity));
                MIN = SettingsActivity.MIN_HUMIDITY;
                MAX = SettingsActivity.MAX_HUMIDITY;
                low_numberPicker.setMinValue(0);
                low_numberPicker.setMaxValue(50);
                high_numberPicker.setMinValue(51);
                high_numberPicker.setMaxValue(90);
                int min_H = sharedPreferences.getInt(SettingsActivity.MIN_HUMIDITY, 30);
                int max_H = sharedPreferences.getInt(SettingsActivity.MAX_HUMIDITY, 60);
                low_numberPicker.setValue(min_H);
                high_numberPicker.setValue(max_H);
                break;
            case SettingsActivity.ILLUMINATION:
                title.setText(getString(R.string.title_illumination));
                MIN = SettingsActivity.VALUE_ILLUMINATION;
                low_numberPicker.setMinValue(0);
                low_numberPicker.setMaxValue(127);
                int value_I = sharedPreferences.getInt(SettingsActivity.VALUE_ILLUMINATION, 20);
                low_numberPicker.setValue(value_I);
                findViewById(R.id.tv_max).setVisibility(View.GONE);
                findViewById(R.id.high_numberPicker).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.tv_min)).setText(R.string.threshold_value);
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (picker.getId()) {
            case R.id.low_numberPicker:
                editor.putInt(MIN, newVal);
                break;
            case R.id.high_numberPicker:
                editor.putInt(MAX, newVal);
                break;
        }
        editor.apply();
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        boolean result = false;
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    result = true;
                } catch (NoSuchFieldException | IllegalAccessException
                        | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public void titleImageButton(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
