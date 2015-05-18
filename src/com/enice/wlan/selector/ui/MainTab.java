package com.enice.wlan.selector.ui;

import com.enice.wlan.selector.*;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

public class MainTab extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
 
	    TabHost tabHost = getTabHost();
	    Resources res = getResources();
	    TabHost.TabSpec spec;
	    Intent intent;
	    
	    intent = new Intent().setClass(this, WLANSelectorSetting.class);
	    spec = tabHost.newTabSpec("artists")
	                  .setIndicator("", res.getDrawable(R.drawable.artists2))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, About.class);
	    spec = tabHost.newTabSpec("about")
	                  .setIndicator("", res.getDrawable(R.drawable.about3))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTab(0);
	    refreshTabBackground();
	    tabHost.setOnTabChangedListener(new OnTabChangeListener() {
	    	@Override
	    	public void onTabChanged(String tabId) {
	    		refreshTabBackground();
	    	}
	    });
	}
	
	private void refreshTabBackground() {
		TabHost tabs = getTabHost();
		TabWidget tabWidget = tabs.getTabWidget();
		for (int i =0; i < tabWidget.getChildCount(); i++) {
			View vvv = tabWidget.getChildAt(i);
			if (tabs.getCurrentTab() == i) {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg4));
			} else {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg5));
			}
		}
	}
}
