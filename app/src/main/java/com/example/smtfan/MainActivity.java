package com.example.smtfan;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smtfan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleReadCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{
    public static final String UUID_SERVICE_BUTTON_OPTIONS = "000000ee-0000-1000-8000-00805f9b34fb";
    public static final String UUID_Characteristic_BUTTON_OPTIONS = "0000ee01-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE_TEMPERATURE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_TEMPERARTURE = "00002a19-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE_SEEKBAR = "0000180c-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_SEEKBAR = "00002a16-0000-1000-8000-00805f9b34fb";
    public static final String HEX_WRITE_AUTO = "00";
    public static final String HEX_WRITE_ONOFF = "00";
    public static final String HEX_WRITE_SEEKBAR_MODE_1 = "00";
    public static final String HEX_WRITE_SEEKBAR_MODE_2 = "01";
    public static final String HEX_WRITE_SEEKBAR_MODE_3 = "02";
    public static final String HEX_WRITE_SEEKBAR_MODE_4 = "03";
    public static final String HEX_WRITE_SEEKBAR_MODE_5 = "04";
    public static final String HEX_WRITE_SEEKBAR_MODE_6 = "05";
    public static final String HEX_WRITE_SEEKBAR_MODE_7 = "06";
    public static final String HEX_WRITE_SEEKBAR_MODE_8 = "07";
    public static final String HEX_WRITE_SEEKBAR_MODE_9 = "08";
    public static final String HEX_WRITE_SEEKBAR_MODE_10 = "09";
    private Handler handler = new Handler();
    protected static final long TIME_DELAYED = 15000;
    private int count_manual_mode = 0;
    BleDevice device;
    Toolbar toolBar;
    MaterialTextView tempOutput;
    AppCompatButton btnManual, btnAuto, btnOnOff;
    RelativeLayout speedChange;
    SeekBar sbChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }
    private void initView() {
        toolBar = findViewById(R.id.toolBar);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");

        speedChange = findViewById(R.id.speedChange);
        btnOnOff = findViewById(R.id.btnOnOff);
        btnManual = findViewById(R.id.btnManual);
        btnAuto = findViewById(R.id.btnAuto);
        tempOutput = findViewById(R.id.tempOutput);
        sbChange = findViewById(R.id.sbChange);

        speedChange.setVisibility(View.INVISIBLE);

        ButtonOption();
        if(device != null) {
            sbChange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(i > 0 && i <=10) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_1),writeCallBack);
                    } else if (i > 10 && i <=20) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_2),writeCallBack);
                    }
                    else if (i > 20 && i <=30) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_3),writeCallBack);
                    }
                    else if (i > 30 && i <=40) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_4),writeCallBack);
                    }
                    else if (i > 40 && i <=50) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_5),writeCallBack);
                    }
                    else if (i > 50 && i <=60) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_6),writeCallBack);
                    }
                    else if (i > 60 && i <= 70) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_7),writeCallBack);
                    }
                    else if (i > 70 && i <= 80) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_8),writeCallBack);
                    }
                    else if (i > 80 && i <= 90) {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_9),writeCallBack);
                    }
                    else {
                        BleManager.getInstance().write(device,UUID_SERVICE_SEEKBAR,UUID_CHARACTERISTIC_SEEKBAR,ByteUtils.hexStr2Bytes(HEX_WRITE_SEEKBAR_MODE_10),writeCallBack);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnConnect:
                Intent toScanActivity = new Intent(MainActivity.this, ScanActivity.class);
                toScanActivity.putExtra(ScanActivity.KEY_DEVICE_INFO,device);
                startActivity(toScanActivity);
                break;
            case R.id.btnTimer:
                Intent toTimerActivity =  new Intent(MainActivity.this, TimerActivity.class);
                toTimerActivity.putExtra(ScanActivity.KEY_DEVICE_INFO,device);
                startActivity(toTimerActivity);
                break;
        }
        return true;
    }

    private void initData() {
        device = getIntent().getParcelableExtra(ScanActivity.KEY_DEVICE_INFO);
        if(device == null) {
            Logger.e("disconnected");
            return;
        }
        else {
            if(BleManager.getInstance().isConnected(device.address)) {
                addDeviceInfoDataAndUpdate();
                Logger.e("connected");
                handler.post(updateTempRunnable);
            }
            else {
                return;
            }
        }
    }

    private void addDeviceInfoDataAndUpdate() {
        if(device == null) return;
        Map<ServiceInfo, List<CharacteristicInfo>> deviceInfo = BleManager.getInstance().getDeviceServices(device.address);
        if(deviceInfo == null) return;
    }

    private boolean checkConnect() {
        if(device == null) return false;
        return true;
    }

    private void ButtonOption() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    speedChange.setVisibility(View.INVISIBLE);
                    if (checkConnect()) {
                        BleManager.getInstance().write(device, UUID_SERVICE_BUTTON_OPTIONS, UUID_Characteristic_BUTTON_OPTIONS,
                                ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF), writeCallBack);
                    } else {
                        Logger.e("Connect to your devivce");
                    }
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedChange.setVisibility(View.VISIBLE);

                speedChange.setVisibility(View.VISIBLE);
                count_manual_mode++;
                if (checkConnect()) {
                    do {
                        if (count_manual_mode == 1) {
                            sbChange.setProgress(35);
                            break;
                        } else if (count_manual_mode == 2) {
                            sbChange.setProgress(70);
                            break;
                        } else {
                            sbChange.setProgress(100);
                            break;
                        }
                    }
                    while (count_manual_mode <= 4);
                    if (count_manual_mode == 3) count_manual_mode = 0;
                } else {
                    Logger.e("Connect to your devivce");
                    do {
                        if (count_manual_mode == 1) {
                            sbChange.setProgress(35);
                            break;
                        } else if (count_manual_mode == 2) {
                            sbChange.setProgress(70);
                            break;
                        } else {
                            sbChange.setProgress(100);
                            break;
                        }
                    }
                    while (count_manual_mode <= 4);
                    if (count_manual_mode == 3) count_manual_mode = 0;
                }
            }
        });

        btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    speedChange.setVisibility(View.INVISIBLE);
                    if (checkConnect()) {
                        BleManager.getInstance().write(device, UUID_SERVICE_TEMPERATURE, UUID_CHARACTERISTIC_TEMPERARTURE,ByteUtils.hexStr2Bytes(HEX_WRITE_AUTO) ,writeCallBack);
                    } else {
                        Logger.e("Connect to your devivce");
                    }
            }
        });


    }

    Runnable updateTempRunnable = new Runnable() {
        @Override
        public void run() {
            BleManager.getInstance().read(device, UUID_SERVICE_TEMPERATURE, UUID_CHARACTERISTIC_TEMPERARTURE, readCallback);
            handler.postDelayed(this,TIME_DELAYED);
        }
    };

    private BleWriteCallback writeCallBack = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] data, BleDevice device) {
            Logger.e("write success:" + ByteUtils.bytes2HexStr(data));
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("write fail:" + info);
        }
    };

    private BleReadCallback readCallback = new BleReadCallback() {
        @Override
        public void onReadSuccess(byte[] data, BleDevice device) {
            Logger.e("read success:" + ByteUtils.bytes2HexStr(data));
            try{
                tempOutput.setText(String.valueOf(Integer.parseInt(ByteUtils.bytes2HexStr(data),16)));
            }catch(Exception ex){
                Logger.e(ex.getMessage());
            }
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e(info);
        }
    };
}