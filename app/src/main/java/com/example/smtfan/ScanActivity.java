package com.example.smtfan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.smtfan.Model.Timer;
import com.example.smtfan.adapter.CommonRecyclerViewAdapter;
import com.example.smtfan.adapter.ScanDeviceAdapter;
import com.example.smtfan.databaseHelper.BleDatabase;
import com.example.smtfan.databaseHelper.connectedDatabase;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.scan.BleScanCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "EasyBle";
    public static final String KEY_DEVICE_INFO = "keyDeviceInfo";
    String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN};
    int requestCodePermission ;
    private BleDatabase bleDatabase;
    private com.example.smtfan.databaseHelper.connectedDatabase connectedDatabase;
    private RecyclerView rv, rv_connectedDevices;
    private BleManager manager;
    private ArrayList<BleDevice> deviceList = new ArrayList<>();
    private ArrayList<BleDevice> cDeviceList = new ArrayList<>();
    private ScanDeviceAdapter adapter;
    private ScanDeviceAdapter connectedAdapter;
    private ImageButton btn_scan;
    private BleDevice mDevice;
    private Toolbar toolBar;
    private ImageButton btnBack;
    private RelativeLayout rl_connectedDevices;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        bleDatabase = new BleDatabase(this);
        connectedDatabase = new connectedDatabase(this);
        initView();
        initBleManager();
        showDeviceByRv();
        showConnectedDeviceByRv();
    }

    private void initView(){
        toolBar = findViewById(R.id.toolBar);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        rv = findViewById(R.id.rv);
        rv_connectedDevices = findViewById(R.id.rv_connectedDevices);
        rl_connectedDevices = findViewById(R.id.rl_connectedDevices);
        deviceList = bleDatabase.getBleList();
        cDeviceList = connectedDatabase.getBleList();
        if(cDeviceList.size() != 0) {
            rl_connectedDevices.setVisibility(View.VISIBLE);
        }
        mDevice = getIntent().getParcelableExtra(ScanActivity.KEY_DEVICE_INFO);
        btn_scan = findViewById(R.id.btn_scan);
        progressBar = findViewById(R.id.pb);
        btnBack = findViewById(R.id.btn_back);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!manager.isScanning()) {
                    if (!BleManager.isBluetoothOn()) {
                        BleManager.toggleBluetooth(true);
                    }
                    if(!hasPermissions(ScanActivity.this,permissions)){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        {
                            requestPermissions(permissions,requestCodePermission);
                        }
                    }
                    BleManager.getInstance().disconnectAll();
                    mDevice = null;
                    startScan();
                }
            }
        });

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent toMainActivity = new Intent(ScanActivity.this,MainActivity.class);
                        toMainActivity.putExtra(OperateActivity.KEY_DEVICE_INFO,mDevice);
                        startActivity(toMainActivity);
                    }
            });

        }

    private void initBleManager() {
        if(!BleManager.supportBle(this)) {
            return;
        }
        BleManager.toggleBluetooth(true);

        BleManager.ScanOptions scanOptions = BleManager.ScanOptions
                .newInstance()
                .scanPeriod(5000)
                .scanDeviceName(null);

        BleManager.ConnectOptions connectOptions = BleManager.ConnectOptions
                .newInstance()
                .connectTimeout(12000);

        manager = BleManager
                .getInstance()
                .setScanOptions(scanOptions)
                .setConnectionOptions(connectOptions)
                .setLog(true,"oam")
                .init(this.getApplication());
    }

    private void showDeviceByRv() {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> res = new SparseArray<>();
        res.put(R.layout.item_rv_scan_devices,new int[]{R.id.tv_name,R.id.tv_address});
        adapter = new ScanDeviceAdapter(this, deviceList, res);
        adapter.setOnItemClickListener(new CommonRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                manager.stopScan();
                BleDevice device = deviceList.get(position);
                BleManager.getInstance().connect(device.address, bleConnectCallback);
            }
        });

        adapter.setOnItemLongClickListener(new CommonRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                BleDevice device = deviceList.get(position);
                if(BleManager.getInstance().isConnected(device.address)) {
                    BleManager.getInstance().disconnect(device.address);
                    Toast.makeText(ScanActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    mDevice = null;
                }
                else {
                    Toast.makeText(ScanActivity.this, "cant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rv.setAdapter(adapter);
    }

    private void showConnectedDeviceByRv() {
        rv_connectedDevices.setLayoutManager(new LinearLayoutManager(this));
        rv_connectedDevices.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> resCD = new SparseArray<>();
        resCD.put(R.layout.item_rv_scan_devices,new int[]{R.id.tv_name,R.id.tv_address});
        connectedAdapter = new ScanDeviceAdapter(this, cDeviceList, resCD);

        connectedAdapter.setOnItemLongClickListener(new CommonRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                BleDevice device = cDeviceList.get(position);
                if(BleManager.getInstance().isConnected(device.address)) {
                    BleManager.getInstance().disconnect(device.address);
                    cDeviceList.clear();
                    if(cDeviceList.size() == 0){
                        rl_connectedDevices.setVisibility(View.GONE);
                    }
                    connectedAdapter.notifyDataSetChanged();
                    connectedDatabase.saveDevice(cDeviceList);
                    mDevice = null;
                }
                else {
                    Toast.makeText(ScanActivity.this, "cant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rv_connectedDevices.setAdapter(connectedAdapter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            default:
                break;
        }
        }

    private boolean hasPermissions(Context context,String[] permissions){
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == requestCodePermission
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
        }
    }

    private void startScan() {
        manager.startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
                for (BleDevice d : deviceList) {
                    if (device.address.equals(d.address)) {
                        return;
                    }
                }
                deviceList.add(device);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                Log.e(TAG, "start scan = " + startScanSuccess + "   info: " + info);
                if (startScanSuccess) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    cDeviceList.clear();
                    connectedAdapter.notifyDataSetChanged();
                    connectedDatabase.saveDevice(cDeviceList);
                    rl_connectedDevices.setVisibility(View.GONE);
                    deviceList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "scan finish");
                bleDatabase.saveDevice(deviceList);
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isGpsOn() {
        LocationManager locationManager
                = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        deviceList.clear();
        adapter.notifyDataSetChanged();
        bleDatabase.saveDevice(deviceList);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.destroy();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void disconnectBle() {
        BleManager.getInstance().disconnectAll();
        cDeviceList.clear();
        connectedAdapter.notifyDataSetChanged();
        connectedDatabase.saveDevice(cDeviceList);
    }

    private BleConnectCallback bleConnectCallback = new BleConnectCallback() {
        @Override
        public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
            Logger.e("start connecting" + startConnectSuccess + " info = " + info);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            if(!startConnectSuccess) {
                Logger.e(info);
            }
        }

        @Override
        public void onConnected(BleDevice device) {
            ScanActivity.this.mDevice = device;
            rl_connectedDevices.setVisibility(View.VISIBLE);
            cDeviceList.add(device);
            connectedAdapter.notifyDataSetChanged();
            connectedDatabase.saveDevice(cDeviceList);
            showPB();
            sendBleDevice();
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
            Logger.e("disconnected");
            showPB();
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("connect fail : " + info);
            showPB();
        }
    };

    private void showPB() {
        progressBar.setProgress(100);
        progressBar.setVisibility(View.GONE);
    }

    private void sendBleDevice(){
        Intent toMainActivity = new Intent(ScanActivity.this,MainActivity.class);
        toMainActivity.putExtra(OperateActivity.KEY_DEVICE_INFO,mDevice);
        startActivity(toMainActivity);
    }



}