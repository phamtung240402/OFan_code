package com.example.smtfan.databaseHelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.ficat.easyble.BleDevice;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class connectedDatabase {
    private SharedPreferences connectedDatabase;
    private Gson gson;

    public connectedDatabase(Context context) {
        connectedDatabase = context.getSharedPreferences("connectedDatabase",Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveDevice(ArrayList<BleDevice> deviceList) {
        SharedPreferences.Editor editor = connectedDatabase.edit();

        editor.putString("ble", gson.toJson(deviceList));
        editor.apply();
    }

    public ArrayList<BleDevice> getBleList() {
        String deviceString = connectedDatabase.getString("ble", null);
        Type BleListType = new TypeToken<ArrayList<BleDevice>>(){}.getType();
        ArrayList<BleDevice> deviceList = gson.fromJson(deviceString,BleListType);

        if(deviceList != null) return deviceList;
        else return new ArrayList<>();

    }
}
