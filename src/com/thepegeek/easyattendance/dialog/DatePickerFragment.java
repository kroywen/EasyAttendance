package com.thepegeek.easyattendance.dialog;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {
	
	protected OnDateSetListener listener;
	
	protected int year;
	protected int month;
	protected int day;
	
	protected int positiveButtonText;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		DatePickerDialog dialog = new DatePickerDialog(getActivity(), listener, year, month, day);
		dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getActivity().getString(positiveButtonText), dialog);
		return dialog;
	}
	
	public void set(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public void setOnDateSetListener(OnDateSetListener listener) {
		this.listener = listener;
	}
	
	public void setPositiveButtonText(int positiveButtonText) {
		this.positiveButtonText = positiveButtonText;
	}
	
}
