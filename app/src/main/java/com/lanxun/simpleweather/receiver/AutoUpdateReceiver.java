package com.lanxun.simpleweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lanxun.simpleweather.service.AutoUpdateService;

/**
 * Created by Joey on 2016/11/7.
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
