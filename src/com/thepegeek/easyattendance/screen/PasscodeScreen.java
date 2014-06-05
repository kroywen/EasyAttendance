package com.thepegeek.easyattendance.screen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.thepegeek.easyattendance.R;

public class PasscodeScreen extends BaseScreen implements OnFocusChangeListener {
	
	protected EditText first;
	protected EditText second;
	protected EditText third;
	protected EditText forth;
	
	protected EditText current;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passcode_screen);
		setScreenTitle(R.string.enter_passcode);
		initializeViews();
	}
	
	protected void initializeViews() {
		first = (EditText) findViewById(R.id.first);
		first.addTextChangedListener(nextWatcher);
		first.setOnFocusChangeListener(this);
		
		second = (EditText) findViewById(R.id.second);
		second.addTextChangedListener(nextWatcher);
		second.setOnFocusChangeListener(this);
		
		third = (EditText) findViewById(R.id.third);
		third.addTextChangedListener(nextWatcher);
		third.setOnFocusChangeListener(this);
		
		forth = (EditText) findViewById(R.id.forth);
		forth.addTextChangedListener(endWatcher);
		
		current = first;
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			current = (EditText) v;
			current.setText(null);
		}
	}
	
	TextWatcher endWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			String passcode = prefs.getPasscode();
			String code = first.getText().toString() + second.getText().toString() +
					third.getText().toString() + forth.getText().toString();
			if (!TextUtils.isEmpty(code) && code.length() == 4) {
				if (code.equals(passcode)) {
					setResult(RESULT_OK);
					finish();
					overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
				} else {
					showErrorDialog(R.string.invalid_passcode, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							first.setText(null);
							second.setText(null);
							third.setText(null);
							forth.setText(null);
							current = first;
							first.requestFocus();
						}
					});
				}
			}
		}
	};
	
	TextWatcher nextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			if (!TextUtils.isEmpty(s.toString())) {
				if (current != null) {
					switch (current.getId()) {
					case R.id.first:	second.requestFocus();	break;
					case R.id.second:	third.requestFocus();	break;
					case R.id.third:	forth.requestFocus();	break;
					}
				}
			}
		}
	};
	
}