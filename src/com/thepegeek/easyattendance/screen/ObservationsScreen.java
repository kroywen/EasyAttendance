package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.ObservationAdapter;
import com.thepegeek.easyattendance.model.Observation;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class ObservationsScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int EDIT_MENU_ITEM = 0;
	public static final int DELETE_MENU_ITEM = 1;
	
	protected Button addObservationBtn;
	protected ListView observationsList;
	protected TextView empty;
	
	protected List<Observation> observations;
	protected ObservationAdapter adapter;
	protected long studentId;
	protected Student student;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.observations_screen);
		getIntentData();
		setScreenTitle(student != null ? student.getFullname() : getString(R.string.observations));
		initializeViews();
		
		adapter = new ObservationAdapter(this, observations);
		observationsList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateObservations();
	}
	
	protected void updateObservations() {
		observations = dbStorage.getObservationsByStudentId(studentId);
		if (Utils.isEmpty(observations)) {
			empty.setVisibility(View.VISIBLE);
			observationsList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			observationsList.setVisibility(View.VISIBLE);
			adapter.setObservations(observations);
			adapter.notifyDataSetChanged();
		}
	}
	
	protected void getIntentData() {
		Intent intent = getIntent(); 
		if (intent != null && intent.getExtras() != null) {
			studentId = intent.getLongExtra(DatabaseHelper.FIELD_STUDENT_ID, 0);
			student = dbStorage.getStudentById(studentId);
		}
	}
	
	protected void initializeViews() {
		addObservationBtn = (Button) findViewById(R.id.addObservationBtn);
		addObservationBtn.setOnClickListener(this);
		
		observationsList = (ListView) findViewById(R.id.observationsList);
		observationsList.setOnItemClickListener(this);
		registerForContextMenu(observationsList);
		
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addObservationBtn) {
			Intent intent = new Intent(this, EditObservationScreen.class);
			intent.putExtra(DatabaseHelper.FIELD_STUDENT_ID, studentId);
			startActivity(intent);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Observation observation = observations.get(info.position);
		menu.setHeaderTitle(observation.getDate());
		menu.add(0, EDIT_MENU_ITEM, 0, R.string.edit);
		menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Observation observation = observations.get(info.position);
		switch (item.getItemId()) {
		case EDIT_MENU_ITEM:
			Intent intent = new Intent(this, EditObservationScreen.class);
			intent.putExtra(DatabaseHelper.FIELD_STUDENT_ID, studentId);
			intent.putExtra(DatabaseHelper.FIELD_OBSERVATION_ID, observation.getId());
			startActivity(intent);
			break;
		case DELETE_MENU_ITEM:
			dbStorage.deleteObservation(observation);
			updateObservations();
			break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Observation observation = observations.get(position);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822"); // in real devices
//		intent.putExtra(Intent.EXTRA_CC, new String[] {"sukovMax@gmail.com"}); // TODO write real addresses
		String subject = getString(R.string.email_subject_pattern, student.getFullname(), observation.getDate());
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, observation.getNote());
		startActivity(Intent.createChooser(intent, getString(R.string.email_app_select)));
	}

}
