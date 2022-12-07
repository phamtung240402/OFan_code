package com.example.smtfan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.smtfan.Model.Timer;
import com.example.smtfan.adapter.TimerAdapter;
import com.example.smtfan.databaseHelper.DatabaseHelper;
import com.example.smtfan.utils.ByteUtils;
import com.example.smtfan.utils.OnClickListener;
import com.example.smtfan.utils.OnLongClickListener;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleWriteCallback;

import java.util.ArrayList;

public class TimerActivity extends AppCompatActivity {
    private RecyclerView rv_timer;
    private BleDevice mDevice;
    private boolean timerRunning;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private int currentRunning;
    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @Override
        public void OnItemLongClick(int position) {
            deletedItem = position;
            btnAddTimer.setVisibility(View.GONE);
            rl_delete.setVisibility(View.VISIBLE);
        }
    };
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void OnItemClick(int position) {

        }

        @Override
        public void OnChecked(int position, boolean checked) {
                if(mDevice == null) return;
                Timer timer = mTimerList.get(position);
                timer.setChecked(checked);
                for (int i = 0; i < mTimerList.size(); i++) {
                    if (timer.isChecked() == true && i != position) {
                        mTimerList.get(i).setChecked(false);
                        timerAdapter.notifyItemChanged(i);
                    }
                }
                databaseHelper.saveTimer(mTimerList);
                if(checked) {
                    if(timerRunning) {
                        pauseTimer();
                    }
                    else {
                        mTimeLeftInMillis = timer.getHour() * 60 * 60 * 1000 + timer.getMinute() * 60 * 1000;
                        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
                        currentRunning = position;
                        startTimer(position);
                    }
                }
                else {
                    pauseTimer();
                }
        }
    };
    private ArrayList<Timer> mTimerList;
    private TimerAdapter timerAdapter;
    private ImageButton btnBack, btnBackTimer;
    private Toolbar toolBar;
    private DatabaseHelper databaseHelper;
    private AppCompatButton btnSaveTimer, btnCancelTimer, btnAddTimer, btnDelete, btnHuy;
    private Spinner sp_chooseMinute;
    private RelativeLayout rl_ChooseTimer, rl_delete, view_addTimer, fl_addTimer;
    private int deletedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        mDevice = getIntent().getParcelableExtra(OperateActivity.KEY_DEVICE_INFO);
        databaseHelper = new DatabaseHelper(this);
        initView();
        adapterLayout();
        ButtonOption();
    }

    private void initView() {
        rv_timer = findViewById(R.id.rv_timer);
        toolBar = findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");

        rl_ChooseTimer = findViewById(R.id.rl_addTimer);
        rl_delete = findViewById(R.id.rl_delete);
        btnAddTimer = findViewById(R.id.btnAddTimer);
        btnSaveTimer = findViewById(R.id.btnSaveTimer);
        btnCancelTimer = findViewById(R.id.btnCancelTimer);
        btnDelete = findViewById(R.id.btnDelete);
        btnHuy = findViewById(R.id.btnHuy);
        sp_chooseMinute = findViewById(R.id.sp_minute);
        ArrayAdapter<CharSequence> sp_adapter = ArrayAdapter.createFromResource(this, R.array.numbers, R.layout.item_minute);
        sp_adapter.setDropDownViewResource(R.layout.item_selected);
        sp_chooseMinute.setAdapter(sp_adapter);

        btnBack = findViewById(R.id.btnBack);
        btnBackTimer = findViewById(R.id.btnBackTimer);

        fl_addTimer = findViewById(R.id.fl_addTimer);
        view_addTimer = findViewById(R.id.view_addTimer);
        mTimerList = new ArrayList<>();
        mTimerList = databaseHelper.getTimerList();
    }

    private void ButtonOption() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toMainActivity = new Intent(TimerActivity.this, MainActivity.class);
                toMainActivity.putExtra(OperateActivity.KEY_DEVICE_INFO, mDevice);
                startActivity(toMainActivity);
            }
        });
        btnAddTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_delete.setVisibility(View.GONE);
                fl_addTimer.setVisibility(View.VISIBLE);
            }
        });
        btnSaveTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer timer = new Timer();
                int time = Integer.parseInt(sp_chooseMinute.getSelectedItem().toString());
                if(time % 60 == 0) {
                    timer.setHour(time / 60);
                } else {
                    timer.setHour(time/60);
                    timer.setMinute(time%60 % 60);
                }
                for (int i = 0; i < mTimerList.size(); i++) {
                    Timer timer1 = mTimerList.get(i);
                    if (timer1.getHour() == timer.getHour() && timer1.getMinute() == timer.getMinute()) {
                        Toast.makeText(TimerActivity.this, "Đã tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mTimerList.add(0, timer);
                timerAdapter.notifyItemInserted(0);
                databaseHelper.saveTimer(mTimerList);
            }
        });

        btnCancelTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_addTimer.setVisibility(View.GONE);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimerList.remove(deletedItem);
                timerAdapter.notifyItemRemoved(deletedItem);
                databaseHelper.saveTimer(mTimerList);
                rl_delete.setVisibility(View.GONE);
                btnAddTimer.setVisibility(View.VISIBLE);
            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_delete.setVisibility(View.GONE);
                btnAddTimer.setVisibility(View.VISIBLE);
            }
        });

        btnBackTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_addTimer.setVisibility(View.GONE);
            }
        });

        view_addTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_addTimer.setVisibility(View.GONE);
            }
        });
    }

    private void adapterLayout() {
        timerAdapter = new TimerAdapter(mTimerList, mDevice, onClickListener, onLongClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_timer.setLayoutManager(linearLayoutManager);
        rv_timer.setAdapter(timerAdapter);
    }

    private void startTimer(int position){
        Timer timer = mTimerList.get(position);
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {
                mTimeLeftInMillis = l;
            }
            @Override
            public void onFinish() {
                timerRunning = false;
                timer.setChecked(false);
                timerAdapter.notifyDataSetChanged();
                databaseHelper.saveTimer(mTimerList);
            }
        }.start();
        timerRunning = true;
        }

    private void pauseTimer(){
        mCountDownTimer.cancel();
        timerRunning = false;
        Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTimeLeftInMillis = savedInstanceState.getLong("millisLeft");
        timerRunning = savedInstanceState.getBoolean("timerRunning");
        currentRunning = savedInstanceState.getInt("currentRunning");
        mEndTime = savedInstanceState.getLong("endTime");
        if(timerRunning) {
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            startTimer(currentRunning);
        }
        else {
            pauseTimer();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        mTimeLeftInMillis = prefs.getLong("millisLeft",0);
        timerRunning = prefs.getBoolean("timerRunning", false);
        currentRunning = prefs.getInt("currentRunning",-1);

        if(timerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            if(mTimeLeftInMillis < 0){
                mTimeLeftInMillis = 0;
                timerRunning = false;
            }
            else {
                startTimer(currentRunning);
            }
        } else {
            if(mCountDownTimer != null)
            pauseTimer();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", mEndTime);
        editor.putInt("currentRunning", currentRunning);

        editor.apply();
    }
}