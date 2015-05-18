package com.enice.wlan.selector.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;

public class ScreenTrigger extends StateTrigger {
	private boolean screenOn;
	private PowerManager mPM = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				screenOn = true;
			} else {
				screenOn = false;
			}
			fireTrigger();
		}
	};
	
	public ScreenTrigger(Context context, Handler handler, int messageId, int exitMessageId) {
		super(context, handler, messageId, exitMessageId);
	}

	@Override
	protected void enableTriggerInternal() {
		stateOn = false;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		context.registerReceiver(receiver, filter);
		screenOn = mPM.isScreenOn();
		fireTrigger();
	}

	@Override
	protected void disableTriggerInternal() {
		context.unregisterReceiver(receiver);
	}
	
	protected boolean checkCondition() {
		return screenOn;
	}
}
