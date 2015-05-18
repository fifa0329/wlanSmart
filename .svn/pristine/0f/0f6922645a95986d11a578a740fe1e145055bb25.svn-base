package com.enice.wlan.selector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    	PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "WLANSelecgtor");
    	wl.acquire(0);
    }
}