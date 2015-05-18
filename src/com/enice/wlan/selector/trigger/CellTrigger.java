package com.enice.wlan.selector.trigger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class CellTrigger extends StateTrigger {
	private int interval;
	private boolean isTD;
	private int city;
	private int lac;
	private int cellId;
	private boolean firstTime;
	private HashSet<Integer> neighbors = new HashSet<Integer>();
	private HashSet<Integer> userGsmCellIds = new HashSet<Integer>();
	private HashSet<Integer> userTdCellIds = new HashSet<Integer>();
	private SQLiteDatabase debug_db = null;
	private TelephonyManager mTM = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	private PhoneStateListener phoneStateListener  = new PhoneStateListener() {
		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			if (firstTime) {
				firstTime = false;
				return;
			}
			if (location instanceof GsmCellLocation) {
				GsmCellLocation gsm = (GsmCellLocation)location;
				int newType = mTM.getNetworkType();
				int newLac = gsm.getLac();
				int newCellId = normalizeCid(gsm.getCid());
				if (cellId != newCellId || lac != newLac || isTD != isTD(newType)) {
					isTD = isTD(newType);
					cellId = newCellId;
					if (lac != newLac ) {
						lac = newLac;
						int newCity = getCityIndex(isTD, lac);
						if (city != newCity) {
							city = newCity;
							loadCityData("/sdcard/WLANSelector/" + city + ".dat");
							loadDebugData("/sdcard/WLANSelector/" + city + ".db");
						}
					}
					fireTrigger();
				} else {
					List<NeighboringCellInfo> newNeighbors = mTM.getNeighboringCellInfo();
					if (newNeighbors == null) {
						if (neighbors.size() > 0) {
							neighbors.clear();
							fireTrigger();
						}
					} else {
						Iterator<NeighboringCellInfo> i = newNeighbors.iterator();
						while (i.hasNext()) {
							NeighboringCellInfo n = i.next();
							if (normalizeCid(n.getCid()) == NeighboringCellInfo.UNKNOWN_CID || normalizeCid(n.getCid()) == 0xFFFF || n.getRssi() == NeighboringCellInfo.UNKNOWN_RSSI || n.getRssi() <= 11) {
								i.remove();
							}
						}
						boolean same = true;
						if (newNeighbors.size() == neighbors.size()) {
							for (NeighboringCellInfo n : newNeighbors) {
								if (!neighbors.contains(normalizeCid(n.getCid()))) {
									same = false;
								}
							}
						} else {
							same = false;
						}
						if (!same) {
							neighbors.clear();
							for (NeighboringCellInfo n : newNeighbors) {
								neighbors.add(normalizeCid(n.getCid()));
							}
							fireTrigger();
						}
					}
				}
			}
		}
	};
	private Runnable runnable = new Runnable() {
        @Override
        public void run() {
        	mTM.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
            handler.postDelayed(this, interval);
        }
    };
    
    private int normalizeCid(int cid) {
		if (cid > 0xFFFF) {
			cid &= 0xFFFF;
		}
		return cid;
    }
    
	public CellTrigger(Context context, Handler handler, int messageId, int exitMessageId, int interval) {
		super(context, handler, messageId, exitMessageId);
		this.interval = interval;
		
		try {
			SQLiteDatabase user_db = SQLiteDatabase.openDatabase("/sdcard/WLANSelector/USER.db", null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			Cursor c = user_db.query("user", new String[]{"ci", "isTD"}, null, null, null, null, null);
			c.moveToFirst();
			while (!c.isAfterLast())  {
				int ci = c.getInt(0);
				int isTD = c.getInt(1);
				if (isTD == 0) {
					userGsmCellIds.add(ci);
				} else {
					userTdCellIds.add(ci);
				}
				c.moveToNext();
			}
			c.close();
			user_db.close();
		} catch (SQLiteException e) {
			userGsmCellIds.clear();
			userTdCellIds.clear();
		}
	}

	@Override
	protected void enableTriggerInternal() {
		stateOn = false;
		isTD = false;
		city = -1;
		lac = -1;
		cellId = -1;
		firstTime = true;
		neighbors.clear();
		handler.post(runnable);
	}

	@Override
	protected void disableTriggerInternal() {
		handler.removeCallbacks(runnable);
		mTM.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		debugOutput("停止更新");
		if (debug_db != null) {
			debug_db.close();
			debug_db = null;
		}
	}
	
	private boolean isGSM(int type) {
		return (type == TelephonyManager.NETWORK_TYPE_EDGE)
			|| (type == TelephonyManager.NETWORK_TYPE_GPRS);
	}
	
	private boolean isTD(int type) {
		return (type == TelephonyManager.NETWORK_TYPE_HSPA)
			|| (type == TelephonyManager.NETWORK_TYPE_HSDPA)
			|| (type == TelephonyManager.NETWORK_TYPE_HSUPA);
	}
	
	private String getCellName(boolean isTD, int lac, int ci) {
		if (debug_db != null) {
			Cursor c = debug_db.query("cell", new String[]{"name"}, "isTD=" + (isTD ? 1 : 0) + " and ci=" + ci, null, null, null, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				return c.getString(0);
			} else {
				return "未知ci:" + ci;
			}
		}
		
		return Integer.toString(ci);
	}
	
	private void loadDebugData(String debugData) {
		try {
			debug_db = SQLiteDatabase.openDatabase(debugData, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		} catch (SQLiteException e) {
			debug_db = null;
		}
	}
	
	private void showDebugInfo(boolean isTD, int lac, int ci, String info) {
		if (debugModeOn()) {
			debugOutput(info + ":" + getCellName(isTD, lac, ci));
		}
	}
	
	protected boolean checkCondition() {
		if (wifiCovered(isTD, lac, cellId)) {
			showDebugInfo(isTD, lac, cellId, "有覆盖");
			return true;
		}
		
		for (Integer ci : neighbors) {
			if (wifiCovered(isTD, 0, ci)) {
				showDebugInfo(isTD, 0, ci, "邻区有覆盖");
				return true;
			}
		}
		
		if (userWifiCovered()) {
			showDebugInfo(isTD, lac, cellId, "自定义覆盖");
			return true;
		}
		
		showDebugInfo(isTD, lac, cellId, "无覆盖");
		return false;
	}
	
	public void rememberUserAp() {
		GsmCellLocation.requestLocationUpdate();
		CellLocation location = mTM.getCellLocation();
		if (location instanceof GsmCellLocation) {
			boolean isTD = isTD(mTM.getNetworkType());
			int lac = ((GsmCellLocation)location).getLac();
			int ci = normalizeCid(((GsmCellLocation)location).getCid());
			
			if (ci == -1) {
				return;
			}
			
			boolean insertOk;			
			if (isTD) {
				insertOk = userTdCellIds.add(cellId);
			} else {
				insertOk = userGsmCellIds.add(cellId);
			}
			
			if (insertOk) {
				SQLiteDatabase user_db = SQLiteDatabase.openDatabase("/sdcard/WLANSelector/USER.db", null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
				ContentValues values = new ContentValues();
				values.put("lac", lac);
				values.put("ci", ci);
				values.put("isTD", isTD ? 1 : 0);
				WifiManager mWM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				values.put("ssid", mWM.getConnectionInfo().getSSID());
				values.put("mac", mWM.getConnectionInfo().getMacAddress());
				user_db.insert("user", null, values);
				user_db.close();
				debugOutput("记录当前热点:" + mWM.getConnectionInfo().getSSID());
			}
		}
	}
	
	private boolean userWifiCovered() {
		return isTD ? userTdCellIds.contains(cellId) : userGsmCellIds.contains(cellId);
	}
	
	private native int getCityIndex(boolean isTD, int lac);
	private native void loadCityData(String cityData);
	private native boolean wifiCovered(boolean isTD, int lac, int ci);
	
    static {
        System.loadLibrary("data");
    }
}
