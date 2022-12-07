package com.example.smtfan.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smtfan.R;
import com.ficat.easyble.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class ScanDeviceAdapter extends CommonRecyclerViewAdapter<BleDevice> {

    public ScanDeviceAdapter(Context mContext, ArrayList<BleDevice> mDataList, SparseArray<int[]> mResLayoutAndViewIds) {
        super(mContext, mDataList, mResLayoutAndViewIds);
    }

    @Override
    protected void bindDataToItem(CommonRecyclerViewAdapter.MyViewholder mHolder, BleDevice bleDevice, int position) {
        TextView tvName = (TextView) mHolder.mViews.get(R.id.tv_name);
        TextView tvAddress = (TextView) mHolder.mViews.get(R.id.tv_address);
        tvName.setText(bleDevice.name);
        tvAddress.setText(bleDevice.connected ? "connected" : "disconnected");
    }

    @Override
    public int getItemResLayoutType(int position) {
        return R.layout.item_rv_scan_devices;
    }
}
