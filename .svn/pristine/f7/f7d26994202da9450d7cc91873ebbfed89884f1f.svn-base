package com.enice.wlan.selector.trigger;

import android.content.Context;
import android.os.Handler;

public abstract class StateTrigger extends Trigger {
	protected boolean stateOn;
	private int exitMessageId;

	public StateTrigger(Context context, Handler handler, int messageId, int exitMessageId) {
		super(context, handler, messageId);
		this.exitMessageId = exitMessageId;
	}

	protected void fireTrigger() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (!stateOn && checkCondition()) {
					stateOn = true;
					handler.sendEmptyMessage(messageId);
				} else if (stateOn && !checkCondition()) {
					stateOn = false;
					handler.sendEmptyMessage(exitMessageId);
				}
			}
		});
	}
	
	protected abstract boolean checkCondition();
	
	public boolean isStateOn() {
		return stateOn;
	}
}
