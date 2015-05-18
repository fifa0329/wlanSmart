package com.enice.wlan.selector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class WakeUpTimer {
	private int interval;
	private PendingIntent operation;
	private AlarmManager mAM;

	public WakeUpTimer(Context context, int interval) {
		this.interval = interval;
		mAM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, WakeUpReceiver.class);
		operation = PendingIntent.getBroadcast(context, 0, i, 0);
	}
	
	public void startWakeUpTimer() {
		mAM.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, operation);
	}
	
	public void stopWakeUpTimer() {
		mAM.cancel(operation);
	}
}