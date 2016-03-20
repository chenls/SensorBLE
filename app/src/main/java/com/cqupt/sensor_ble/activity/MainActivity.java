package com.cqupt.sensor_ble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;
import com.cqupt.sensor_ble.utils.CommonTools;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends Activity {
    private static final int REQUEST_CHANGE_NAME = 2;
    private int TIME = 2000;
    private TextView step, distance, calorie, tv_bluetooth_name, tv_battery, tv_rssi;
    private Button connect, setting;
    private BluetoothDevice mDevice = null;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    public static final String TAG = "SmartLock";
    private String deviceAddress;
    private static final int UART_PROFILE_CONNECTED = 20;
    private String rssi;
    private boolean isManualDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);
        Intent intent = this.getIntent();        //获取已有的intent对象
        Bundle bundle = intent.getExtras();    //获取intent里面的bundle对象
        deviceAddress = bundle.getString(BluetoothDevice.EXTRA_DEVICE);
        rssi = bundle.getString("rssi");
        tv_rssi = ((TextView) findViewById(R.id.tv_rssi));
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        service_init();
        tv_bluetooth_name = (TextView) findViewById(R.id.tv_bluetooth_name);
        tv_bluetooth_name.setText(getString(R.string.bluetooth_name) + mDevice.getName());
        tv_battery = (TextView) findViewById(R.id.tv_battery);
        tv_battery.setText(getString(R.string.battery_value) + "100%");//初始为100%
        step = (TextView) findViewById(R.id.step);
        distance = (TextView) findViewById(R.id.distance);
        calorie = (TextView) findViewById(R.id.calorie);
        connect = (Button) findViewById(R.id.connect);
        setting = (Button) findViewById(R.id.setting);
        connect.setOnClickListener(new OnClickListener());
        setting.setOnClickListener(new OnClickListener());
        handler.postDelayed(runnable, TIME); //每隔2s执行

    }

    public class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == connect) {
                if (connect.getText().equals(getString(R.string.connect))) {
                    CommonTools.showShortToast(MainActivity.this, getString(R.string.tryReconnet));
                    mService.connect(deviceAddress);
                } else {
                    if (mDevice != null) {
                        mService.disconnect();
                    }
                    skipWelcomeActivity();
                }
            } else if (v == setting) {
                openSetting();
            }
        }
    }

    private void skipWelcomeActivity() {
        isManualDisconnect = true;
        Intent newIntent = new Intent(MainActivity.this, WelcomeActivity.class);
        Bundle bundle = new Bundle(); //创建Bundle对象
        bundle.putBoolean(WelcomeActivity.M2W, false);     //装入数据
        newIntent.putExtras(bundle);
        startActivity(newIntent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    /**
     * 发送字符串数据
     */
    private void sendData(String message) {
        byte[] value;
        try {
            value = message.getBytes("UTF-8");
            Log.i(TAG, "发送数据为：" + Arrays.toString(value));
            mService.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 广播接收器
     */
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /**
             * 连接成功
             */
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                connect.setText(R.string.disconnect);
                tv_bluetooth_name.setText(getString(R.string.bluetooth_name) + mDevice.getName());
                mState = UART_PROFILE_CONNECTED;
                tv_rssi.setText(getString(R.string.rssi) + rssi + "dBm");
            }
            /**
             * 连接断开
             */
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                if (isManualDisconnect) {
                    return;
                }
                connect.setText(R.string.connect);
                Log.i(TAG, "连接断开");
                tv_bluetooth_name.setText(R.string.no_bt);
                tv_battery.setText(getString(R.string.battery));
//                step.setBackgroundResource(R.drawable.gray_circle_shape);
//                step.setText(R.string.bt_disconnect);
                tv_rssi.setText(R.string.rssi_null);
                mService.connect(deviceAddress);
                mState = UART_PROFILE_DISCONNECTED;
            }
            /**
             * 获取数据
             */
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final String stepValue = intent.getStringExtra(UartService.EXTRA_DATA);
                if (!TextUtils.isEmpty(stepValue)) {
//                    接受到CHAR4的数据
                    step.setText("步数：" + stepValue);
                    distance.setText("距离：" + stepValue);
                    calorie.setText("卡路里：" + 2);
                }
                //获取RSSI
                final String rssiStatus = intent.getStringExtra(UartService.RSSI_STATUS);
                if (!TextUtils.isEmpty(rssiStatus)) {
                    if (rssiStatus.equals("0")) {
                        rssi = intent.getStringExtra(UartService.RSSI);
                        tv_rssi.setText(getString(R.string.rssi) + rssi + "dBm");
                    }
                }
            }
            /**
             * 获取电量
             */
            if (action.equals(UartService.EXTRAS_DEVICE_BATTERY)) {
                final String txValue = intent.getStringExtra(UartService.EXTRA_DATA);
                tv_battery.setText(getString(R.string.battery_value) + txValue + "%");
            }
            /**
             * 发现服务后 发起获取通知数据的请求
             */
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
                //获取电量
                mService.readCharacteristic(UartService.Battery_Service_UUID, UartService.Battery_Level_UUID);
            }
            /**
             * 接受设备不支持UART的广播
             */
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                CommonTools.showShortToast(MainActivity.this, getString(R.string.bt_initialize_fail));
                mService.disconnect();
            }
        }
    };

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                handler.postDelayed(this, TIME);
                //每两秒读取一次Rssi
                mService.myReadRemoteRssi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void openSetting() {
        Intent newIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(newIntent, REQUEST_CHANGE_NAME);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mState == UART_PROFILE_CONNECTED) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                CommonTools.showShortToast(MainActivity.this, getString(R.string.Sign_out));
            } else {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) { //监控/拦截菜单键
            openSetting();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 服务中间人
     */
//UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            } else {
                //服务开启后 连接蓝牙
                mService.connect(deviceAddress);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };


    /**
     * 开启服务
     * 注册广播
     */
    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * 广播过滤器
     *
     * @return static
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.EXTRAS_DEVICE_BATTERY);
        return intentFilter;
    }

    /**
     * 当破坏Activity时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Main_onDestroy()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
    }
}