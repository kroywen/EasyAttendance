package com.thepegeek.easyattendance.screen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.thepegeek.easyattendance.R;

public class SettingsScreen extends BaseScreen implements OnClickListener {
	
	public static final int LINK_TO_DROPBOX_REQUEST_CODE = 0;
	
	protected TextView dropboxLink;
//	protected EditText nameFormat;
	protected View passcodeLayout;
	protected TextView passcodeSwitch;
	
	protected View contactUs;
	protected View visitWebsite;
	protected View reportBug;
	protected View tellFriend;
	
	protected View easyAssessment;
	protected View easyPD;
	protected View easyPortfolio;
	
	protected View twitterPage;
	protected View facebookPage;
	protected View googlePlusPage;
	
	protected boolean loggedIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_screen);
		setScreenTitle(R.string.settings_and_support);
		initializeViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateViews();
		
		AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                loggedIn = true;
                updateViews();
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void initializeViews() {
		dropboxLink = (TextView) findViewById(R.id.dropboxLink);
		dropboxLink.setOnClickListener(this);
//		nameFormat = (EditText) findViewById(R.id.nameFormat);
		passcodeLayout = findViewById(R.id.passcodeLayout);
		passcodeLayout.setOnClickListener(this);
		passcodeSwitch = (TextView) findViewById(R.id.passcodeSwitch);
		
		contactUs = findViewById(R.id.contactUs);
		contactUs.setOnClickListener(this);
		visitWebsite = findViewById(R.id.visitWebsite);
		visitWebsite.setOnClickListener(this);
		reportBug = findViewById(R.id.reportBug);
		reportBug.setOnClickListener(this);
		tellFriend = findViewById(R.id.tellFriend);
		tellFriend.setOnClickListener(this);
		
		easyAssessment = findViewById(R.id.easyAssessment);
		easyAssessment.setOnClickListener(this);
		easyPD = findViewById(R.id.easyPD);
		easyPD.setOnClickListener(this);
		easyPortfolio = findViewById(R.id.easyPortfolio);
		easyPortfolio.setOnClickListener(this);
		
		twitterPage = findViewById(R.id.twitterPage);
		twitterPage.setOnClickListener(this);
		facebookPage = findViewById(R.id.facebookPage);
		facebookPage.setOnClickListener(this);
		googlePlusPage = findViewById(R.id.googlePlusPage);
		googlePlusPage.setOnClickListener(this);
	}
	
	protected void updateViews() {
		passcodeSwitch.setText(prefs.isLock() ? R.string.on : R.string.off);
		dropboxLink.setText(mApi.getSession().isLinked() ? R.string.unlink_from_dropbox : R.string.link_to_dropbox);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dropboxLink:
			AndroidAuthSession session = mApi.getSession();
			if (session != null) {
				if (session.isLinked()) {
					session.unlink();
					clearKeys();
					updateViews();
				} else {
					session.startAuthentication(this);
				}
			}
			break;
		case R.id.passcodeLayout:
			switchPasscodeLock();
			break;
		case R.id.contactUs:
			contactUs();
			break;
		case R.id.visitWebsite:
			visitWebsite();
			break;
		case R.id.reportBug:
			reportBug();
			break;
		case R.id.tellFriend:
			tellFriend();
			break;
		case R.id.easyAssessment:
		case R.id.easyPD:
		case R.id.easyPortfolio:
		case R.id.twitterPage:
		case R.id.facebookPage:
		case R.id.googlePlusPage:
			showPage(v.getTag().toString());
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LINK_TO_DROPBOX_REQUEST_CODE) {
			updateViews();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	protected void showPage(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}
	
	protected void tellFriend() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.tell_prewritten_message));
		startActivity(Intent.createChooser(intent, getString(R.string.email_app_select)));
	}
	
	protected void reportBug() {
		String msgText = String.format(getString(R.string.report_bug_template), 
				android.os.Build.BRAND, 
				android.os.Build.MODEL,
				android.os.Build.VERSION.RELEASE);
	
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@thepegeekapps.com"});
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		startActivity(Intent.createChooser(intent, getString(R.string.email_app_select)));
	}
	
	protected void visitWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://www.thepegeekapps.com"));
		startActivity(intent);
	}
	
	protected void contactUs() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@thepegeekapps.com"});
		startActivity(Intent.createChooser(intent, getString(R.string.email_app_select)));
	}
	
	protected void switchPasscodeLock() {
		if (prefs.isLock()) {
			prefs.setLock(false);
		} else {
			showEnterPasscodeDialog();
		}
		updateViews();
	}
	
	protected void showEnterPasscodeDialog() {
		final EditText et = new EditText(this);
		et.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		et.setTransformationMethod(new PasswordTransformationMethod());
		et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(4)});
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					char last = s.charAt(s.length()-1);
					if (!Character.isDigit(last)) {
						s = s.subSequence(0, s.length()-1);
					}
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		et.setSingleLine();
		String passcode = prefs.getPasscode();
		if (!TextUtils.isEmpty(passcode)) {
			et.setText(passcode);
			et.setSelection(passcode.length());
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.enter_passcode)
			.setView(et)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String passcode = et.getText().toString().trim();
					if (!TextUtils.isEmpty(passcode) && passcode.length() == 4) {
						prefs.setLock(true);
						prefs.setPasscode(passcode);
						updateViews();
					} else {
						showErrorDialog(R.string.invalid_passcode_format);
					}
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}

}
