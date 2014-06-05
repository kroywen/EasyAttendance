package com.thepegeek.easyattendance.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Observation;
import com.thepegeek.easyattendance.storage.DatabaseHelper;

public class EditObservationScreen extends BaseScreen implements OnClickListener {
	
	protected EditText date;
	protected EditText note;
	protected Button saveBtn;
	
	protected long studentId;
	protected long observationId;
	protected Observation observation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_observation_screen);
		setScreenTitle(R.string.new_observation);
		getIntentData();
		initializeViews();
		updateViews();
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			studentId = intent.getLongExtra(DatabaseHelper.FIELD_STUDENT_ID, 0);
			observationId = intent.getLongExtra(DatabaseHelper.FIELD_OBSERVATION_ID, 0);
			if (observationId == 0) {
				observation = new Observation(0, studentId, null, null);
				observation.setDate(System.currentTimeMillis());
			} else {
				observation = dbStorage.getObservationById(observationId);
			}
		}
	}
	
	protected void initializeViews() {
		saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setText(inEditMode() ? R.string.save : R.string.add);
		saveBtn.setVisibility(View.VISIBLE);
		saveBtn.setOnClickListener(this);
		
		date = (EditText) findViewById(R.id.date);
		note = (EditText) findViewById(R.id.note);
	}
	
	protected void updateViews() {
		if (observation != null) {
			date.setText(observation.getDate());
			note.setText(observation.getNote());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveBtn:
			observation.setNote(note.getText().toString().trim());
			if (inEditMode()) {
				dbStorage.updateObservation(observation);
			} else {
				dbStorage.addObservation(observation);
			}
			finish();
			break;
		}
	}
	
	protected boolean inEditMode() {
		return observationId != 0;
	}

}
