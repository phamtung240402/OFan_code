package com.example.smtfan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleConnectCallback;

import java.util.List;
import java.util.Map;

public class OperateActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String KEY_DEVICE_INFO = "keyDeviceInfo";

    private BleDevice device;
    private TextView tv_device_name,tv_device_address,tv_connection_state,tv_connect,tv_disconnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);
        initData();
        initView();
    }

    private void initData()
    {
        device = getIntent().getParcelableExtra(KEY_DEVICE_INFO);
    }

    private void initView()
    {
        tv_device_name = findViewById(R.id.tv_device_name);
        tv_device_address = findViewById(R.id.tv_device_address);
        tv_connection_state = findViewById(R.id.tv_connection_state);
        tv_connect = findViewById(R.id.tv_connect);
        tv_disconnect = findViewById(R.id.tv_disconnect);
        tv_connect.setOnClickListener(this);
        tv_disconnect.setOnClickListener(this);
        tv_device_name.setText(device.name);
        tv_device_address.setText(device.address);
        updateConnectionStateUi(BleManager.getInstance().isConnected(device.address));
    }

    private void sendBleDevice(){
        Intent toMainActivity = new Intent(OperateActivity.this,MainActivity.class);
        toMainActivity.putExtra(KEY_DEVICE_INFO,device);
        startActivity(toMainActivity);
    }

    private void updateConnectionStateUi(boolean connected) {
        String state;
        if(device.connected){
            state = "Connected";
        }
        else if(device.connecting)
        {
            state = "Connecting";
        }
        else {
            state = "Disconnected";
        }
        tv_connection_state.setText(state);
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_connect) {
            BleManager.getInstance().connect(device.address, bleConnectCallback);
            return;
        }
        switch (view.getId()) {
            case R.id.tv_disconnect:
                BleManager.getInstance().disconnect(device.address);
        }
    }

    private BleConnectCallback bleConnectCallback = new BleConnectCallback() {
        @Override
        public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
            Logger.e("start connecting" + startConnectSuccess + " info = " + info);
            OperateActivity.this.device = device;
            updateConnectionStateUi(false);
            if(!startConnectSuccess) {
                Toast.makeText(OperateActivity.this, "start connecting fail: " + info, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnected(BleDevice device) {
            updateConnectionStateUi(true);
            sendBleDevice();
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
            Logger.e("disconnected");
            updateConnectionStateUi(false);
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("connect fail : " + info);
            updateConnectionStateUi(false);
        }
    };
}