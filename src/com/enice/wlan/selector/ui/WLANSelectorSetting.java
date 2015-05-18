package com.enice.wlan.selector.ui;

import com.enice.wlan.selector.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class WLANSelectorSetting extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	private static final int MENU_APP_LIST 		= 1;
	private static final int MENU_UPDATE_APK	= 2;
	private static final int MENU_UPDATE_DB		= 3;
	private static final int MENU_UPLOAD_LOG	= 4;
	
	public static final String KEY_SERVICE_RUNNING = "serviceRunning";
	public static final String KEY_SERVICE_MODE = "serviceMode";
	public static final String KEY_APP_LIST = "appList";
	public static final String[] MODE_DESCRIPTION = {"�����ʾģʽ����ϵͳ��������վ�źţ���������WLAN�����������ʾ����",
		"�Զ�����ģʽ������������WLAN��������ʱ��ʼɨ��WLAN�źţ�ȷ�����Ӻ���ʾ����½ʹ�á�",
		"ҵ������ģʽ������������ҵ������Ҵ���WLAN��������ʱ��ʼɨ��WLAN�źţ�ȷ�����Ӻ���ʾ����½ʹ�á�"};
	
	private SharedPreferences prefs;
	private ListPreference mListPreference;
	
	private Downloader downloader;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mListPreference = (ListPreference)getPreferenceScreen().findPreference(KEY_SERVICE_MODE);
        mListPreference.setSummary(MODE_DESCRIPTION[Integer.parseInt(prefs.getString(KEY_SERVICE_MODE, "0"))]);
		
		downloader = new Downloader(WLANSelectorSetting.this);
		if (!downloader.checkDatabase()) {
			Dialog dialog = new AlertDialog.Builder(WLANSelectorSetting.this)
				.setTitle("��������")
				.setMessage("δ�ҵ����ݿ⣬�Ƿ����أ�")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						downloader.downloadDatabase();
					}})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}})
				.create();
			dialog.show();
		}
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		if (prefs.getBoolean(KEY_SERVICE_RUNNING, false)) {
            startService(new Intent(this, WLANSelectorService.class));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_APP_LIST, Menu.NONE, "�����б�");
		menu.add(Menu.NONE, MENU_UPDATE_APK, Menu.NONE, "������");
		menu.add(Menu.NONE, MENU_UPDATE_DB, Menu.NONE, "��������");
		menu.add(Menu.NONE, MENU_UPLOAD_LOG, Menu.NONE, "�ϴ���־");
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
    	case MENU_APP_LIST:
    		startActivity(new Intent(this, AppSelector.class));
    		break;
    	case MENU_UPDATE_APK:
    		downloader.checkNewVersion();
    		break;
    	case MENU_UPDATE_DB:
    		downloader.downloadDatabase();
    		break;
    	case MENU_UPLOAD_LOG:
    		downloader.uploadLog();
    		break;
    	}
    	return false;
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.i("WLANSelector", "onSharedPreferenceChanged: " + key);
		if (prefs.getBoolean(KEY_SERVICE_RUNNING, false)) {
            startService(new Intent(this, WLANSelectorService.class));
		} else if (key.equals(KEY_SERVICE_RUNNING)) {
            stopService(new Intent(this, WLANSelectorService.class));
		}
		
		if (key.equals(KEY_SERVICE_MODE)) {
			mListPreference.setSummary(MODE_DESCRIPTION[Integer.parseInt(prefs.getString(KEY_SERVICE_MODE, "0"))]);
		}
	}
}