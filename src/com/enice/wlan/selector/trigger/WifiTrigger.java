package com.enice.wlan.selector.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class WifiTrigger extends StateTrigger {
	private boolean firstTime = true;
	private boolean wifiConnected;
	private WifiManager mWM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	private int scan_counter = 0;
	private Runnable runnable = new Runnable() {
        @Override
        public void run() {
			if (scan_counter > 0) {
				scan_counter --;
	        	mWM.startScan();
	        	Log.i("WLANSelector", "force wlan scan");
	            handler.postDelayed(this, 10000);
			}
        }
    };
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
				if (state == WifiManager.WIFI_STATE_ENABLED) {
					if (firstTime) {
						firstTime = false;
						addCmccProfile("\"CMCC\"");
						addCmccProfile("\"CMCC-EDU\"");
					}
					scan_counter = 6;
					handler.post(runnable);
				} else if (state == WifiManager.WIFI_STATE_DISABLED){
					wifiConnected = false;
					handler.removeCallbacks(runnable);
					fireTrigger();
				}
			} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info != null) {
					if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
						wifiConnected = true;
						handler.removeCallbacks(runnable);
						fireTrigger();
					} else {
						wifiConnected = false;
						fireTrigger();
					}
				}
			}
		}
	};
	
	public WifiTrigger(Context context, Handler handler, int messageId, int exitMessageId) {
		super(context, handler, messageId, exitMessageId);
	}
	
	@Override
	protected void enableTriggerInternal() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		context.registerReceiver(receiver, filter);
		initWifiStatus();
		stateOn = wifiConnected;
	}

	private void initWifiStatus() {
		WifiInfo info = mWM.getConnectionInfo();
		if (info != null && info.getSupplicantState().equals(SupplicantState.COMPLETED)) {
			wifiConnected = true;
		} else {
			wifiConnected = false;
		}
	}
	
	private void addCmccProfile(String ssid) {
		for (WifiConfiguration config : mWM.getConfiguredNetworks()) {
			if (config.SSID.equals(ssid)) {
				return;
			}
		}
		
		Log.i("WLANSelector", "addCmccProfile:" + ssid);
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = ssid;
		config.priority = 20;
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		
		int networkId = mWM.addNetwork(config);
		if (networkId != -1) {
			mWM.enableNetwork(networkId, false);
			mWM.saveConfiguration();
		}
	}

	@Override
	protected void disableTriggerInternal() {
		context.unregisterReceiver(receiver);
	}
	
	protected boolean checkCondition() {
		return wifiConnected;
	}
}
