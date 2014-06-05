package com.thepegeek.easyattendance.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import com.thepegeek.easyattendance.R;

public class TimePickerFragment extends DialogFragment {
	
	protected OnTimeSetListener listener;
	
	protected int hourOfDay;
	protected int minute;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		TimePickerDialog dialog = new TimePickerDialog(getActivity(), listener, hourOfDay, minute, DateFormat.is24HourFormat(getActivity()));
		dialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getActivity().getString(R.string.ok), dialog);
		return dialog;
	}
	
	public void set(int hourOfDay, int minute) {
		this.hourOfDay = hourOfDay;
		this.minute = minute;
	}
	
	public void setOnTimeSetListener(OnTimeSetListener listener) {
		this.listener = listener;
	}

}
