package com.enice.wlan.selector;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiAction {
	private static WifiAction instance = null;
	
	protected Context context;
	private WifiManager mWM;
	
	private WifiAction(Context context) {
		this.context = context;
		mWM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public static WifiAction getInstance(Context context) {
		if (instance == null) {
			instance = new WifiAction(context);
		}
		return instance;
	}
	
	public void turnOnWifi() {
		Log.i("WLANSelector", "turnOnWifi");
		if (!mWM.isWifiEnabled()) {
			mWM.setWifiEnabled(true);
		}
	}
	
	public void turnOffWifi() {
		Log.i("WLANSelector", "turnOffWifi");
		if (mWM.isWifiEnabled()) {
			mWM.setWifiEnabled(false);
		}
	}

	public boolean isWifiEnabled() {
		return mWM.isWifiEnabled();
	}
	
	public boolean isCmccAp() {
		String ssid = mWM.getConnectionInfo().getSSID();
		if (ssid.equals("CMCC")) {
			return true;
		}
		if (ssid.equals("CMCC-EDU")) {
			return true;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean(WLANSelectorService.KEY_DEBUG_MODE, false)) {
			if (ssid.equals("Bus2")) {
				return true;
			}
			if (ssid.startsWith("ENICE")) {
				return true;
			}
		}
		
		return false;
	}
}
