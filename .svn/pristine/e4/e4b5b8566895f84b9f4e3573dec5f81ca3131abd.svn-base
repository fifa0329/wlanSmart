package com.enice.wlan.selector.trigger;

import android.content.Context;
import android.os.Handler;

public class TimerTrigger extends Trigger {
	private int interval;
	private boolean repeat;
	private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fireTrigger();
            if (repeat) {
            	handler.postDelayed(this, interval);
            }
        }
    };
	
	public TimerTrigger(Context context, Handler handler, int messageId, int interval, boolean repeat) {
		super(context, handler, messageId);
		this.interval = interval;
		this.repeat = repeat;
	}
	
	@Override
	protected void enableTriggerInternal() {
		handler.postDelayed(runnable, interval);
	}

	@Override
	protected void disableTriggerInternal() {
		handler.removeCallbacks(runnable);
	}
}
