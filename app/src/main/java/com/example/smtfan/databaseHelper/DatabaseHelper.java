package com.example.smtfan.databaseHelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smtfan.Model.Timer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DatabaseHelper {

    private SharedPreferences timerDatabase;
    private Gson gson;

    public DatabaseHelper(Context context) {
        timerDatabase = context.getSharedPreferences("timerDatabase",Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTimer(ArrayList<Timer> timerList) {
        SharedPreferences.Editor editor = timerDatabase.edit();
        editor.putString("timer", gson.toJson(timerList));
        editor.apply();
    }

    public ArrayList<Timer> getTimerList() {
        String timerString = timerDatabase.getString("timer", null);
        Type timerListType = new TypeToken<ArrayList<Timer>>(){}.getType();
        ArrayList<Timer> timerList = gson.fromJson(timerString,timerListType);

        if(timerList != null) return timerList;
        else return new ArrayList<>();

    }
}
