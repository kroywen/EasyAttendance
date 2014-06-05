package com.thepegeek.easyattendance.screen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.thepegeek.easyattendance.R;

public class SplashScreen extends BaseScreen {
	
	public static final int STOPSPLASH = 0;
	public static final long SPLASHTIME = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		Message msg = new Message();  
        msg.what = STOPSPLASH;  
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
	}
	
	@Override
	public void onBackPressed() {}
	
	private Handler splashHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {
        	if (msg.what == STOPSPLASH) {
        		Intent intent = new Intent(SplashScreen.this, MainScreen.class);
        		startActivity(intent);
        		overridePendingTransition(0, 0);
        		SplashScreen.this.finish();
        	}
        	super.handleMessage(msg);
        }
    }; 

}
