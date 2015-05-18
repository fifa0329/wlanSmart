package com.enice.wlan.selector.ui; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.enice.wlan.selector.*;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AppSelector extends ListActivity {
	private PackageManager mPM;
	private List<ApplicationInfo> infoList;
	private boolean[] checkList;
	private AppListAdapter appListAdapter;
	private SharedPreferences prefs;
	private Handler handler;
	private ProgressDialog waitingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_selector);
		
		Button ok_button = (Button) findViewById(R.id.ok_button);
		ok_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if (checkList != null) {
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < checkList.length; i++) {
						if (checkList[i]) {
							buffer.append(infoList.get(i).packageName).append(";");
						}
					}
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("appList", buffer.toString());
					editor.commit();
				}
				finish();
			}
		});
		Button cancel_button = (Button) findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		mPM = getPackageManager();
        infoList = new ArrayList<ApplicationInfo>();
		appListAdapter = new AppListAdapter();
		setListAdapter(appListAdapter);
		handler = new Handler() {   
	    	public void handleMessage(Message message) {
	    		appListAdapter.notifyDataSetChanged();
	    		waitingDialog.dismiss();
	    	};
	    };
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage("∂¡»°÷–...");
        waitingDialog.show();
		
		new Thread(new Runnable() {
			public void run() {
		        for (PackageInfo p : mPM.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
		        	if ((p.applicationInfo != null) && (p.requestedPermissions != null)) {
		        		if (Arrays.asList(p.requestedPermissions).contains("android.permission.INTERNET")) {
		        			infoList.add(p.applicationInfo);
		        		}
		        	}
		        }
		        Collections.sort(infoList, new PackageItemInfo.DisplayNameComparator(mPM));
		        checkList = new boolean[infoList.size()];
				
		        prefs = PreferenceManager.getDefaultSharedPreferences(AppSelector.this);
		        String[] apps = prefs.getString("appList", "").split(";");
		        List<String> preferredApps = Arrays.asList(apps);
		        for (int i = 0; i < checkList.length; i++) {
		        	if (preferredApps.contains(infoList.get(i).packageName)) {
		        		checkList[i] = true;
		        	} else {
		        		checkList[i] = false;
		        	}
		        }
		        
		        handler.sendEmptyMessage(0);
			}
		}).start();
	}
	
	private class AppListAdapter extends BaseAdapter {
		private LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		@Override
		public int getCount() {
			return infoList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {			
			View view = inflater.inflate(R.layout.app_item, null);
			ImageView app_icon = (ImageView) view.findViewById(R.id.app_icon);
			TextView app_tilte = (TextView) view.findViewById(R.id.app_title);
			CheckBox check = (CheckBox) view.findViewById(R.id.check);
			
            ApplicationInfo res = infoList.get(position);
            app_icon.setImageDrawable(res.loadIcon(mPM));
            app_tilte.setText(res.loadLabel(mPM));
            check.setChecked(checkList[position]);
            check.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (((CheckBox)view).isChecked()) {
						checkList[position] = true;
					} else {
						checkList[position] = false;
					}
				}
            });
            
            return view;
		}
	}
}
