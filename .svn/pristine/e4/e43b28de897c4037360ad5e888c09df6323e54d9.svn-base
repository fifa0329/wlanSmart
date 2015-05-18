package com.enice.wlan.selector;

import com.enice.wlan.selector.ui.WLANSelectorSetting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(WLANSelectorSetting.KEY_SERVICE_RUNNING, false)) {
            context.startService(new Intent(context, WLANSelectorService.class));
		}
	}
}
