package com.thepegeek.easyattendance.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	
	public static final String PASSCODE = "passcode";
	public static final String LOCK = "lock";
	
	protected static Preferences instance;
	
	protected SharedPreferences prefs;
	
	public Preferences(Context context) {
		prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}
	
	public static Preferences getInstance(Context context) {
		if (instance == null) {
			instance = new Preferences(context);
		}
		return instance;
	}
	
	public String getPasscode() {
		return prefs.getString(PASSCODE, null);
	}
	
	public void setPasscode(String passcode) {
		set(PASSCODE, passcode);
	}
	
	public boolean isLock() {
		return prefs.getBoolean(LOCK, false);
	}
	
	public void setLock(boolean lock) {
		set(LOCK, lock);
	}
	
	public void set(String key, String value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public void set(String key, int value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public void set(String key, long value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public void set(String key, boolean value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
