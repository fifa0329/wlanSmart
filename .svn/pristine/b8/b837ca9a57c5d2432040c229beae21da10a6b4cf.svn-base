package com.enice.wlan.selector.ui;

import com.enice.wlan.selector.*;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class AuthDialog extends Activity {
	private NotificationManager mNM;
	private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AuthDialog.class.getName())) {
				AuthDialog.this.finish();
			}
		}
	};
	
    private void showAuthNotification() {
    	String text = "已认证，计费中";
    	long start_time = SystemClock.elapsedRealtime();
        Notification notification = new Notification(R.drawable.money, text,
                System.currentTimeMillis());
        Intent logoutIntent = new Intent(this, LogoutDialog.class);
        logoutIntent.putExtra("start_time", start_time);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        		logoutIntent, 0);
        notification.setLatestEventInfo(this, "WLANSelector",
                       text, contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.timer_notification);
        notification.contentView = contentView;
        contentView.setChronometer(R.id.chronometer, start_time, null, true);
        
        mNM.notify(WLANSelectorService.NOTIFICATION_AUTH, notification);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_dialog);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(AuthDialog.class.getName());
		registerReceiver(closeReceiver, filter);
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		TextView text_view = (TextView) findViewById(R.id.text);
		text_view.setText("您当前所在位置已经提供中国移动WLAN覆盖，您是否需要使用该服务？");
		Button ok_button = (Button) findViewById(R.id.ok_button);
		ok_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				ProgressDialog progress = new ProgressDialog(AuthDialog.this);
				progress.setTitle("登陆WLAN网络");
				progress.setMessage("正在登陆，请等待...");
				progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progress.show();
				new Thread(new Runnable() {
					@Override
					public void run() {						
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AuthDialog.this);
						boolean result = AuthPortal.getInstance().login(prefs.getString("user", ""), prefs.getString("password", ""));
						if (result) {
							showAuthNotification();
							AuthDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(AuthDialog.this, "登陆成功", Toast.LENGTH_LONG).show();
								}
							});
						} else {
							AuthDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(AuthDialog.this, "登陆失败", Toast.LENGTH_LONG).show();
								}
							});
						}
						finish();
					}
				}).start();
			}
		});
		Button cancel_button = (Button) findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				WifiAction.getInstance(AuthDialog.this).turnOffWifi();
				finish();
			}
		});
	}
}
