package com.enice.wlan.selector.trigger;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public abstract class Trigger {
	private boolean triggerStarted = false;
	protected Handler handler;
	protected int messageId;
	protected Context context;
	
	public Trigger(Context context, Handler handler, int messageId) {
		this.context = context;
		this.handler = handler;
		this.messageId = messageId;
	}
	
	public void enableTrigger() {
		Log.v("WLANSelector", "enableTrigger " + this.getClass().getSimpleName());
		if (!triggerStarted) {
			triggerStarted = true;
			enableTriggerInternal();
		}
	}
	
	public void disableTrigger() {
		Log.v("WLANSelector", "disableTrigger " + this.getClass().getSimpleName());
		if (triggerStarted) {
			triggerStarted = false;
			disableTriggerInternal();
		}
	}
	
	public boolean isTriggerStarted() {
		return triggerStarted;
	}
	
	protected void fireTrigger() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				handler.sendEmptyMessage(messageId);
			}
		});
	}
	
	protected boolean debugModeOn() {
		return ((com.enice.wlan.selector.WLANSelectorService)context).debugModeOn();
	}
	
	protected void debugOutput(String text) {
		((com.enice.wlan.selector.WLANSelectorService)context).debugOutput(text);
	}
	
	protected abstract void enableTriggerInternal();
	protected abstract void disableTriggerInternal();
}
