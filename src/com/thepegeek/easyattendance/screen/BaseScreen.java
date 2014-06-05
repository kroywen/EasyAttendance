package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.storage.DatabaseStorage;
import com.thepegeek.easyattendance.storage.Preferences;

public class BaseScreen extends FragmentActivity {
	
	public static final String APP_FOLDER = "easyattendance/";
	public static final String TEMP_FOLDER = APP_FOLDER + "temp/";
	
	public static final String APP_KEY = "zv59agobd4zmclu";
	public static final String APP_SECRET = "ruaqrh5osp84ru4";
	public static final String ACCOUNT_PREFS_NAME = "prefs";
    public static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    public static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    public static final AccessType ACCESS_TYPE = AccessType.DROPBOX;    
	
	protected DropboxAPI<AndroidAuthSession> mApi;
	
	protected DatabaseStorage dbStorage;
	protected Preferences prefs;
	
	protected ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbStorage = DatabaseStorage.getInstance(this);
		prefs = Preferences.getInstance(this);
		
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/* it calls for passcode screen every time main screen is started
		if (!(getParent() instanceof MainScreen)) {
			View titlebar = findViewById(R.id.titlebar);
			if (titlebar != null) {
				titlebar.setOnTouchListener(new OnSwipeTouchListener() {
					@Override
					public void onSwipeRight() {
						Intent intent = new Intent(BaseScreen.this, MainScreen.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						overridePendingTransition(R.anim.view_transition_in_right, R.anim.view_transition_out_right);
					}
				});
			}
		}
		*/
	}
	
	protected void setScreenTitle(int resid) {
		setScreenTitle(getString(resid));
	}
	
	protected void setScreenTitle(String title) {
		TextView titleView = (TextView) findViewById(R.id.title);
		if (titleView != null) {
			titleView.setText(title);
		}
	}
	
	protected void showErrorDialog(int message) {
		showErrorDialog(getString(message));
	}
	
	protected void showErrorDialog(String message) {
		showErrorDialog(getString(R.string.error), message, null);
	}
	
	protected void showErrorDialog(int title, int message) {
		showErrorDialog(getString(title), getString(message), null);
	}
	
	protected void showErrorDialog(int message, DialogInterface.OnClickListener listener) {
		showErrorDialog(null, getString(message), listener);
	}
	
	protected void showErrorDialog(String title, String message, DialogInterface.OnClickListener listener) {
		DialogInterface.OnClickListener l = (listener != null) ? listener :
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.ok, l)
			.create()
			.show();
	}
	
	protected void showConfirmDialog(String title, String message, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.ok, listener)
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	protected void showInfoDialog(int title, int message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	protected boolean isIntentAvailable(String action) {
	    Intent intent = new Intent(action);
	    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return !list.isEmpty();
	}
	
	protected void showProgressDialog(String message) {
		if (progressDialog == null) {
			 progressDialog = new ProgressDialog(BaseScreen.this);
			 progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 }
		 progressDialog.setMessage(message);
		 progressDialog.show();
	}
	
	protected void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			try {
				progressDialog.dismiss();
			} catch (IllegalArgumentException e) {
				// activity is destroyed, so dialog is not attached to window 
				e.printStackTrace();
			}
		}
	}
	
	protected AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
	
	protected String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    protected void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    protected void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

}
