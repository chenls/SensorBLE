package com.cqupt.sensor_ble.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cqupt.sensor_ble.R;
import com.cqupt.sensor_ble.utils.CommonTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceListActivity extends Activity {
    public static final String DEVICE_ADDRESS = "deviceAddress";
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences sharedPreferences;
    private TextView mEmptyList;
    private static final String TAG = "myLog";

    private List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private Handler mHandler;
    private boolean mScanning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.device_list);
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showMessage(getString(R.string.ble_not_supported));
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            showMessage(getString(R.string.ble_not_supported));
            finish();
            return;
        }
        populateList();
        mEmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mScanning) {
                    scanLeDevice(true);
                    mEmptyList.setText(R.string.scanning);
                } else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
        try {
            sharedPreferences = this.getSharedPreferences(MainActivity.MY_DATE,
                    Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //noinspection deprecation
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    cancelButton.setText(R.string.scan);
                    mEmptyList.setText(R.string.no_device);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            //noinspection deprecation
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            cancelButton.setText(R.string.cancel);
        } else {
            mScanning = false;
            //noinspection deprecation
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addDevice(device, rssi);
                                }
                            });
                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }


        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);


            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        //noinspection deprecation
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //noinspection deprecation
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    /**
     * 选择设备后返回。
     */
    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //noinspection deprecation
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ADDRESS, deviceList.get(position).getAddress());

            if (!sharedPreferences.getBoolean(ChooseBooleanActivity.IS_MANUAL_SET_NOT_AUTO_CONNECT, false)) {
                editor.putBoolean(ChooseBooleanActivity.IS_AUTO_CONNECT, true);
            }

            editor.apply();

            Intent newIntent = new Intent(DeviceListActivity.this, MainActivity.class);
            Bundle bundle = new Bundle(); //创建Bundle对象
            bundle.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());     //装入数据
            int rssi = devRssiValues.get(deviceList.get(position).getAddress());
            bundle.putString("rssi", "" + rssi);     //装入数据
            newIntent.putExtras(bundle);
            startActivity(newIntent);
            //淡入淡出的效果
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    class DeviceAdapter extends BaseAdapter {
        final List<BluetoothDevice> devices;
        final LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tv_add = ((TextView) vg.findViewById(R.id.address));
            final TextView tv_name = ((TextView) vg.findViewById(R.id.name));
            final TextView tv_paired = (TextView) vg.findViewById(R.id.paired);
            final TextView tv_rssi = (TextView) vg.findViewById(R.id.rssi);

            tv_rssi.setVisibility(View.VISIBLE);
            byte rssiValue = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssiValue != 0) {
                tv_rssi.setText(getString(R.string.Rssi, rssiValue));
            }

            tv_name.setText(device.getName());
            tv_add.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tv_name.setTextColor(Color.WHITE);
                tv_add.setTextColor(Color.WHITE);
                tv_paired.setTextColor(Color.GRAY);
                tv_paired.setVisibility(View.VISIBLE);
                tv_paired.setText(R.string.paired);
                tv_rssi.setVisibility(View.VISIBLE);
                tv_rssi.setTextColor(Color.WHITE);

            } else {
                tv_name.setTextColor(Color.WHITE);
                tv_add.setTextColor(Color.WHITE);
                tv_paired.setVisibility(View.GONE);
                tv_rssi.setVisibility(View.VISIBLE);
                tv_rssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }

    private void showMessage(String msg) {
        CommonTools.showShortToast(this, msg);
    }
}
