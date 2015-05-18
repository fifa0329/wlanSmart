package com.enice.wlan.selector.ui;

import com.enice.wlan.selector.*;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class LogoutDialog extends Activity {
	private NotificationManager mNM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logout_dialog);
		long start_time = getIntent().getLongExtra("start_time", 0);
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		TextView text_view = (TextView) findViewById(R.id.text);
		text_view.setText("您是否要停止使用中国移动WLAN业务？");
		Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
		chronometer.setBase(start_time);
		chronometer.start();
		Button ok_button = (Button) findViewById(R.id.ok_button);
		ok_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				ProgressDialog progress = new ProgressDialog(LogoutDialog.this);
				progress.setTitle("登出WLAN网络");
				progress.setMessage("正在登出，请等待...");
				progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progress.show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean result = AuthPortal.getInstance().logout();
						if (result) {
							LogoutDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(LogoutDialog.this, "登出成功", Toast.LENGTH_LONG).show();
								}
							});
						} else {
							LogoutDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(LogoutDialog.this, "登出失败", Toast.LENGTH_LONG).show();
								}
							});
						}
						mNM.cancel(WLANSelectorService.NOTIFICATION_AUTH);
						WifiAction.getInstance(LogoutDialog.this).turnOffWifi();
						finish();
					}
				}).start();
			}
		});
		Button cancel_button = (Button) findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}
