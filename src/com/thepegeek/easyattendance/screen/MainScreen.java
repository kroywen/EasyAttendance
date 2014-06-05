package com.thepegeek.easyattendance.screen;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.storage.Preferences;

@SuppressWarnings("deprecation")
public class MainScreen extends TabActivity {
	
	public static final int ENTER_PASSCODE_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        initTabs();
        checkPasscode();
    }
    
    protected void initTabs() {
		TabHost tabHost = getTabHost();
		Resources res = getResources();
		
		TabSpec takeTabSpec = tabHost.newTabSpec("take")
			.setIndicator(getString(R.string.take), res.getDrawable(R.drawable.ic_tab_take))
			.setContent(new Intent(this, SelectCourseScreen.class));
		
		TabSpec addTabSpec = tabHost.newTabSpec("add")
				.setIndicator(getString(R.string.add_view), res.getDrawable(R.drawable.ic_tab_add))
				.setContent(new Intent(this, CoursesScreen.class));
				
		TabSpec randomTabSpec = tabHost.newTabSpec("random")
				.setIndicator(getString(R.string.random), res.getDrawable(R.drawable.ic_tab_random))
				.setContent(new Intent(this, SelectCourse2Screen.class));
		
		TabSpec reportTabSpec = tabHost.newTabSpec("report")
				.setIndicator(getString(R.string.report), res.getDrawable(R.drawable.ic_tab_report))
				.setContent(new Intent(this, ReportScreen.class));
		
		TabSpec settingsTabSpec = tabHost.newTabSpec("settings")
				.setIndicator(getString(R.string.settings), res.getDrawable(R.drawable.ic_tab_settings))
				.setContent(new Intent(this, SettingsScreen.class));
		
		tabHost.addTab(takeTabSpec);
		tabHost.addTab(addTabSpec);
		tabHost.addTab(randomTabSpec);
		tabHost.addTab(reportTabSpec);
		tabHost.addTab(settingsTabSpec);
	}
    
    protected void checkPasscode() {
    	if (Preferences.getInstance(this).isLock()) {
    		Intent intent = new Intent(this, PasscodeScreen.class);
    		startActivityForResult(intent, ENTER_PASSCODE_REQUEST_CODE);
    		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == ENTER_PASSCODE_REQUEST_CODE && resultCode != RESULT_OK) {
    		finish();
    	}
    }
    
}
