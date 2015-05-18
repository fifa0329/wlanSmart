package com.enice.wlan.selector;

import java.util.Arrays;

import com.enice.wlan.selector.trigger.AppTrigger;
import com.enice.wlan.selector.trigger.CellTrigger;
import com.enice.wlan.selector.trigger.ScreenTrigger;
import com.enice.wlan.selector.trigger.TimerTrigger;
import com.enice.wlan.selector.trigger.WifiTrigger;
import com.enice.wlan.selector.ui.AuthDialog;
import com.enice.wlan.selector.ui.MainTab;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

public class WLANSelectorService extends Service {
	private static final int MESSAGE_TIMER			= 0;
	private static final int MESSAGE_SCREEN_ON		= 1;
	private static final int MESSAGE_SCREEN_OFF		= -1;
	private static final int MESSAGE_APP_ON			= 2;
	private static final int MESSAGE_APP_OFF		= -2;
	private static final int MESSAGE_CELL_ON		= 3;
	private static final int MESSAGE_CELL_OFF		= -3;
	private static final int MESSAGE_WIFI_CONNECTED		= 4;
	private static final int MESSAGE_WIFI_DISCONNECTED	= -4;
	
	public static final String KEY_DEBUG_MODE = "debugMode";
	public static final String KEY_DEBUG_SOUND = "debugSound";
	public static final String KEY_SERVICE_MODE = "serviceMode";
	public static final String KEY_APP_LIST = "appList";
	
	private static final int SERVICE_MODE_SIMPLE	= 0;
	private static final int SERVICE_MODE_AUTO		= 1;
	private static final int SERVICE_MODE_SMART		= 2;
	
    private volatile Looper mServiceLooper;
    private volatile Handler mServiceHandler;
	private NotificationManager mNM;
	
	public static final int NOTIFICATION_DEBUG = R.string.app_name + 1;
	public static final int NOTIFICATION_AUTH	= R.string.app_name + 2;
	public static final int NOTIFICATION_AREA 	= R.string.app_name + 3;
	
	private SharedPreferences prefs = null;
	private boolean debugMode = false;
	private String debugString = null;
	
	private ScreenTrigger screenTrigger;
	private AppTrigger appTrigger;
	private CellTrigger cellTrigger;
	private WifiTrigger wifiTrigger;
	private TimerTrigger timerTrigger;
	private WifiAction wifiAction;
	private WakeUpTimer wakeUpTimer;
    
	public boolean debugModeOn() {
		return debugMode;
	}
	
	public void debugOutput(String text) {
		Log.d("WLANSelector", text);
		if (debugMode) {
			if (!text.equals(debugString)) {
				debugString = text;
				showDebugNotification(text);
			}
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		cleanUp();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		debugMode = prefs.getBoolean(KEY_DEBUG_MODE, false);
		showDebugNotification("启动WLAN优选服务");
		
		int serviceMode = Integer.parseInt(prefs.getString(KEY_SERVICE_MODE, "0"));
		switch (serviceMode) {
		case SERVICE_MODE_SIMPLE:
			mServiceHandler = new SimpleServiceHandler(mServiceLooper);
			screenTrigger = new ScreenTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_SCREEN_ON, MESSAGE_SCREEN_OFF);
			cellTrigger = new CellTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_CELL_ON, MESSAGE_CELL_OFF, 5000);
			wakeUpTimer = new WakeUpTimer(WLANSelectorService.this, 300000);
			screenTrigger.enableTrigger();
			cellTrigger.enableTrigger();
			break;
		case SERVICE_MODE_AUTO:
			mServiceHandler = new AutoServiceHandler(mServiceLooper);
	        screenTrigger = new ScreenTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_SCREEN_ON, MESSAGE_SCREEN_OFF);
	        cellTrigger = new CellTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_CELL_ON, MESSAGE_CELL_OFF, 5000);
	        wifiTrigger = new WifiTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_WIFI_CONNECTED, MESSAGE_WIFI_DISCONNECTED);
	        timerTrigger = new TimerTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_TIMER, 30000, false);
	        screenTrigger.enableTrigger();
	        wifiTrigger.enableTrigger();
			break;
		case SERVICE_MODE_SMART:
			mServiceHandler = new SmartServiceHandler(mServiceLooper);
	        screenTrigger = new ScreenTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_SCREEN_ON, MESSAGE_SCREEN_OFF);
	        cellTrigger = new CellTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_CELL_ON, MESSAGE_CELL_OFF, 5000);
	        wifiTrigger = new WifiTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_WIFI_CONNECTED, MESSAGE_WIFI_DISCONNECTED);
	        timerTrigger = new TimerTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_TIMER, 30000, false);
			appTrigger = new AppTrigger(WLANSelectorService.this, mServiceHandler, MESSAGE_APP_ON, MESSAGE_APP_OFF, 5000);
			String[] apps = prefs.getString(KEY_APP_LIST, "").split(";");
			appTrigger.setAppList(Arrays.asList(apps)); 
	        screenTrigger.enableTrigger();
	        wifiTrigger.enableTrigger();
			break;
		}
        
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
        		Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        wifiAction = WifiAction.getInstance(WLANSelectorService.this);
	}

	private void cleanUp() {
		if (screenTrigger != null) screenTrigger.disableTrigger();
		if (appTrigger != null) appTrigger.disableTrigger();
		if (cellTrigger != null) cellTrigger.disableTrigger();
		if (wifiTrigger != null) wifiTrigger.disableTrigger();
		if (timerTrigger != null) timerTrigger.disableTrigger();
		if (wakeUpTimer != null) wakeUpTimer.stopWakeUpTimer();
		hideDebugNotification();
		hideAuthNotification();
		hideAreaNotification();
	}
	
	@Override
	public void onDestroy() {
		cleanUp();
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
    private void showDebugNotification(String text) {
        Notification notification = new Notification(R.drawable.wlan_icon, text,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainTab.class), 0);
        notification.setLatestEventInfo(this, "WLANSelector",
                       text, contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        if (prefs.getBoolean(KEY_DEBUG_SOUND, false)) {
        	notification.defaults |= Notification.DEFAULT_SOUND;
        }
        
        startForeground(NOTIFICATION_DEBUG, notification);
    }
    
    private void hideDebugNotification() {
    	stopForeground(true);
    }
    
    private void hideAuthNotification() {
        mNM.cancel(NOTIFICATION_AUTH);
    }
    
    private void showAreaNotification() {
    	String text = "已进入WLAN网络覆盖区域";
        Notification notification = new Notification(R.drawable.wireless, text,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        		new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
        notification.setLatestEventInfo(this, "WLANSelector",
                       text, contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        mNM.notify(NOTIFICATION_AREA, notification);
    }
    
    private void hideAreaNotification() {
        mNM.cancel(NOTIFICATION_AREA);
    }
    
    private final class SimpleServiceHandler extends Handler {
        public SimpleServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	Log.d("WLANSelector", "MESSAGE:" + msg.what);
        	switch (msg.what) {
        	case MESSAGE_SCREEN_ON:
        		wakeUpTimer.stopWakeUpTimer();
        		break;
        	case MESSAGE_SCREEN_OFF:
        		wakeUpTimer.startWakeUpTimer();
        		break;
        	case MESSAGE_CELL_ON:
        		showAreaNotification();
        		break;
        	case MESSAGE_CELL_OFF:
        		hideAreaNotification();
        		break;
        	}
        }
    }
    
    private final class AutoServiceHandler extends Handler {
        public AutoServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	Log.d("WLANSelector", "MESSAGE:" + msg.what);
        	switch (msg.what) {
        	case MESSAGE_TIMER:
        		if (!screenTrigger.isStateOn()) {
        			wifiAction.turnOffWifi();
        		}
        		timerTrigger.disableTrigger();
        		break;
        	case MESSAGE_SCREEN_ON:
        		cellTrigger.enableTrigger();
        		break;
        	case MESSAGE_SCREEN_OFF:
        		cellTrigger.disableTrigger();
        		if (wifiTrigger.isStateOn()) {
	        		timerTrigger.disableTrigger();
	        		timerTrigger.enableTrigger();
        		} else {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	case MESSAGE_CELL_ON:
        		wifiAction.turnOnWifi();
        		break;
        	case MESSAGE_CELL_OFF:
        		if (!wifiTrigger.isStateOn()) {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	case MESSAGE_WIFI_CONNECTED:
        		if (!(cellTrigger.isTriggerStarted() && cellTrigger.isStateOn())) {
        			cellTrigger.rememberUserAp();
        		}
        		if (wifiAction.isCmccAp()) {
        			Intent intent = new Intent(WLANSelectorService.this, AuthDialog.class);
        			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			WLANSelectorService.this.startActivity(intent);
        		}
        		break;
        	case MESSAGE_WIFI_DISCONNECTED:
        		hideAuthNotification();
        		sendBroadcast(new Intent(AuthDialog.class.getName()));  
        		if (!(cellTrigger.isTriggerStarted() && cellTrigger.isStateOn())) {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	}
        }
    }
    
    private final class SmartServiceHandler extends Handler {    	
        public SmartServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	Log.d("WLANSelector", "MESSAGE:" + msg.what);
        	switch (msg.what) {
        	case MESSAGE_TIMER:
        		if (!(screenTrigger.isStateOn() && appTrigger.isTriggerStarted() && appTrigger.isStateOn())) {
        			wifiAction.turnOffWifi();
        		}
        		timerTrigger.disableTrigger();
        		break;
        	case MESSAGE_SCREEN_ON:
        		appTrigger.enableTrigger();
        		break;
        	case MESSAGE_SCREEN_OFF:
        		appTrigger.disableTrigger();
        		cellTrigger.disableTrigger();
        		if (wifiTrigger.isStateOn()) {
	        		timerTrigger.disableTrigger();
	        		timerTrigger.enableTrigger();
        		} else {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	case MESSAGE_APP_ON:
        		cellTrigger.enableTrigger();
        		break;
        	case MESSAGE_APP_OFF:
        		cellTrigger.disableTrigger();
        		if (wifiTrigger.isStateOn()) {
	        		timerTrigger.disableTrigger();
	        		timerTrigger.enableTrigger();
        		} else {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	case MESSAGE_CELL_ON:
        		wifiAction.turnOnWifi();
        		break;
        	case MESSAGE_CELL_OFF:
        		if (!wifiTrigger.isStateOn()) {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	case MESSAGE_WIFI_CONNECTED:
        		if (!(cellTrigger.isTriggerStarted() && cellTrigger.isStateOn())) {
        			cellTrigger.rememberUserAp();
        		}
        		if (wifiAction.isCmccAp()) {
        			Intent intent = new Intent(WLANSelectorService.this, AuthDialog.class);
        			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			WLANSelectorService.this.startActivity(intent);
        		}
        		break;
        	case MESSAGE_WIFI_DISCONNECTED:
        		hideAuthNotification();
        		sendBroadcast(new Intent(AuthDialog.class.getName())); 
        		if (!(cellTrigger.isTriggerStarted() && cellTrigger.isStateOn())) {
        			wifiAction.turnOffWifi();
        		}
        		break;
        	}
        }
    }
}
