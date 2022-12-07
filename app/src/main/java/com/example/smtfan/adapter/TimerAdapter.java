package com.example.smtfan.adapter;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smtfan.Model.Timer;
import com.example.smtfan.R;
import com.example.smtfan.TimerActivity;
import com.example.smtfan.utils.ByteUtils;
import com.example.smtfan.utils.OnClickListener;
import com.example.smtfan.utils.OnLongClickListener;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleWriteCallback;

import java.util.ArrayList;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {
    private String UUID_TIMER_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    private String UUID_TIMER_CHARACTERISTICS = "00002a14-0000-1000-8000-00805f9b34fb";
    private String HEX_TIMER_CANCEL = "00";
    private String HEX_TIMER_15 = "01";
    private String HEX_TIMER_30 = "02";
    private String HEX_TIMER_60 = "03";
    private String HEX_TIMER_120 = "04";
    private String HEX_TIMER_240 = "05";
    private boolean timerRunning = false;

    private ArrayList<Timer> timerList;
    private BleDevice mDevice;
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;


    public TimerAdapter(ArrayList<Timer> timerList,BleDevice mDevice, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        this.timerList = timerList;
        this.mDevice = mDevice;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_timer, parent, false);
        return new TimerViewHolder(view, onClickListener, onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Timer timer = timerList.get(position);
        if(timer == null) {
            return;
        }
        holder.txtHour.setText(String.format("%02d",timer.getHour()));
        if(timer.getHour() > 1) {
            holder.texthour.setText("hours");
        } else {
            holder.texthour.setText("hour");
        }
        holder.txtMinute.setText(String.format("%02d",timer.getMinute()));
        if(timer.getMinute() > 1) {
            holder.textminute.setText("minutes");
        } else {
            holder.textminute.setText("minute");
        }
        holder.btnAddAlarm.setChecked(timer.isChecked());
        holder.btnAddAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onClickListener.OnChecked(position, b);
                if (mDevice == null) {
                    return;
                } else {
                    if (BleManager.getInstance().isConnected(mDevice.address)) {
                        if (b) {
                            int time = timer.getHour() * 60 + timer.getMinute();
                            if (time == 15) {
                                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_15), writeCallBack);
                            } else if (time == 30) {
                                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_30), writeCallBack);
                            } else if (time == 60) {
                                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_60), writeCallBack);
                            } else if (time == 120) {
                                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_120), writeCallBack);
                            }
                            else {
                                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_240), writeCallBack);
                            }
//
                            Logger.e("done");
                        } else {
                            Logger.e("cancelled");
                            BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(HEX_TIMER_CANCEL), writeCallBack);
                        }
                    }
                    else {
                        return;
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(timerList != null) return timerList.size();
        return 0;
    }

    class TimerViewHolder extends RecyclerView.ViewHolder {
        private TextView txtHour, txtMinute, texthour, textminute;

        private Switch btnAddAlarm;

        public TimerViewHolder(@NonNull View itemView, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
            super(itemView);
            txtHour = itemView.findViewById(R.id.txtHour);
            txtMinute = itemView.findViewById(R.id.txtMinute);
            btnAddAlarm = itemView.findViewById(R.id.btnAddAlarm);
            texthour = itemView.findViewById(R.id.texthour);
            textminute = itemView.findViewById(R.id.textminute);
            itemView.setOnClickListener(v -> onClickListener.OnItemClick(getAdapterPosition()));
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onLongClickListener.OnItemLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }

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



    }
