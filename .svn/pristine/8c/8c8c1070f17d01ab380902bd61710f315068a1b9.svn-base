package com.enice.wlan.selector.ui; 

import com.enice.wlan.selector.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class About extends Activity {
	public final static String SMS_BODY = "Õ∆ºˆƒ„ ‘ ‘WLANæ´¡È" + Downloader.APK_URL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		Button share_button = (Button) findViewById(R.id.share_button);
		share_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.putExtra("sms_body", SMS_BODY);  
				i.setType("vnd.android-dir/mms-sms");  
				startActivity(i); 
			}
		});
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		CheckBox debug_checkbox = (CheckBox) findViewById(R.id.debug_checkbox);
		debug_checkbox.setChecked(prefs.getBoolean(WLANSelectorService.KEY_DEBUG_MODE, false));
		debug_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton debug, boolean value) {
				prefs.edit().putBoolean(WLANSelectorService.KEY_DEBUG_MODE, value).commit();
			}
		});
		
		CheckBox debug_sound_checkbox = (CheckBox) findViewById(R.id.debug_sound_checkbox);
		debug_sound_checkbox.setChecked(prefs.getBoolean(WLANSelectorService.KEY_DEBUG_SOUND, false));
		debug_sound_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton debug, boolean value) {
				prefs.edit().putBoolean(WLANSelectorService.KEY_DEBUG_SOUND, value).commit();
			}
		});
	}
}