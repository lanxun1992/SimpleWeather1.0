package com.lanxun.simpleweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.lanxun.simpleweather.receiver.AutoUpdateReceiver;
import com.lanxun.simpleweather.util.HttpCallbackListener;
import com.lanxun.simpleweather.util.HttpUtil;
import com.lanxun.simpleweather.util.Utility;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Joey on 2016/11/7.
 */

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int threeHour = 3 * 60 * 60 * 1000; //这是3小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + threeHour;
        Intent intent1 = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent1, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("county_name", "");
        try {
            String address = "http://v.juhe.cn/weather/index?format=2&cityname=" + URLEncoder.encode(name, "UTF-8") +
                    "&key=bc52bd0429b5edb0ba49b5850f2b5489";
            HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                @Override
                public void onFinish(InputStream in) {
                    Utility.handleWeatherResponse(AutoUpdateService.this, in);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
