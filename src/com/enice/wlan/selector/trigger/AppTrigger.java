package com.enice.wlan.selector.trigger;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;

public class AppTrigger extends StateTrigger {
	private int interval;
	private List<String> appList;
	private ActivityManager mAM = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
	private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fireTrigger();
            handler.postDelayed(this, interval);
        }
    };
	
	public AppTrigger(Context context, Handler handler, int messageId, int exitMessageId, int interval) {
		super(context, handler, messageId, exitMessageId);
		this.interval = interval;
	}

	@Override
	protected void enableTriggerInternal() {
		stateOn = false;
		handler.post(runnable);
	}

	@Override
	protected void disableTriggerInternal() {
		handler.removeCallbacks(runnable);
	}
	
	protected boolean checkCondition() {
    	List<RunningTaskInfo> runningTaskInfos = mAM.getRunningTasks(1);
    	if (runningTaskInfos.size() > 0) {
    		RunningTaskInfo info = runningTaskInfos.get(0);
    		if (appList.contains(info.topActivity.getPackageName())) {
    			return true;
    		}
    	}
		return false;
	}

	public void setAppList(List<String> appList) {
		this.appList = appList;
	}
}
