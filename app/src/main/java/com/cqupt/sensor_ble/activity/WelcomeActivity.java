package com.cqupt.sensor_ble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;
import com.cqupt.sensor_ble.utils.CommonTools;

public class WelcomeActivity extends Activity {
    public static final String M2W = "m2w";
    private final int TIME = 2000;
    private TextView animation, tv_bluetooth;
    private Animation myAnimation_Scale;
    private BluetoothAdapter mBtAdapter = null;
    private SharedPreferences sharedPreferences;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 5000;
    private static boolean is_auto_connect;
    private boolean is_auto_connect_success;
    private boolean isStartScan;
    private boolean isBTOpened;
    private boolean isFirstOpen = true;

    @Override
    protected void onResume() {
        super.onResume();

        if (!isFirstOpen) {
            isFirstOpen = false;
            return;
        }
        boolean isOpen;
        if (!mBtAdapter.isEnabled()) {
            isOpen = mBtAdapter.enable();
        } else {
            isOpen = true;
            isBTOpened = true;
        }
        if (isOpen) {
            tv_bluetooth.setText(R.string.bluetooth_open);
            if (is_auto_connect) {
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!is_auto_connect_success) {
                            //noinspection deprecation
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            CommonTools.showShortToast(WelcomeActivity.this, getString(R.string.auto_connect_not_success));
                        }
                    }
                }, SCAN_PERIOD);
                //软件打开前 蓝牙已经打开 立即发起扫描命令
                if (isBTOpened && !isStartScan) {
                    boolean isSuccessfully;
                    //noinspection deprecation
                    isSuccessfully = mBluetoothAdapter.startLeScan(mLeScanCallback);
                    if (isSuccessfully) {
                        isStartScan = true;
                    }
                }
            }
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceAddress = sharedPreferences.getString(DeviceListActivity.DEVICE_ADDRESS, null);
                            if ((device.getAddress()).equals(deviceAddress)) {
                                //停止扫描
                                is_auto_connect_success = true;
                                //noinspection deprecation
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                CommonTools.showShortToast(WelcomeActivity.this, getString(R.string.auto_success));
                                Intent newIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                                Bundle bundle = new Bundle(); //创建Bundle对象
                                bundle.putString(BluetoothDevice.EXTRA_DEVICE, deviceAddress);     //装入数据
                                bundle.putString("rssi", "" + rssi);     //装入数据
                                newIntent.putExtras(bundle);
                                startActivity(newIntent);
                                //淡入淡出的效果
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            }
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_welcome);
        Button connect_bluetooth = (Button) findViewById(R.id.connect_bluetooth);
        Button about_smart_lock = (Button) findViewById(R.id.about_smart_lock);
        animation = (TextView) findViewById(R.id.animation);
        tv_bluetooth = (TextView) findViewById(R.id.tv_bluetooth);
        myAnimation_Scale = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.my_scale_action);
        animation.startAnimation(myAnimation_Scale);
        handler.postDelayed(runnable, TIME); //每隔2s执行
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            CommonTools.showShortToast(this, "Bluetooth is not available");
            finish();
            return;
        }

        connect_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(WelcomeActivity.this, DeviceListActivity.class);
                startActivity(newIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        about_smart_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(WelcomeActivity.this, AboutActivity.class);
                startActivity(newIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        try {
            sharedPreferences = this.getSharedPreferences("myDate",
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        is_auto_connect = sharedPreferences.getBoolean(ChooseActivity.IS_AUTO_CONNECT, false);
        Intent intent = this.getIntent();        //获取已有的intent对象
        Bundle bundle = intent.getExtras();    //获取intent里面的bundle对象
        try {
            is_auto_connect = bundle.getBoolean(M2W);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                handler.postDelayed(this, TIME);
                animation.startAnimation(myAnimation_Scale);
                if (!isStartScan) {
                    boolean isSuccessfully;
                    //noinspection deprecation
                    isSuccessfully = mBluetoothAdapter.startLeScan(mLeScanCallback);
                    if (isSuccessfully) {
                        isStartScan = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
