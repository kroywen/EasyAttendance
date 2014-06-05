package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.LabelAdapter;
import com.thepegeek.easyattendance.model.Label;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class LabelsScreen extends BaseScreen  implements OnClickListener, OnItemClickListener {
	
	public static final int DELETE_MENU_ITEM = 0;
	
	protected TextView addCustomLabel;
	protected ListView labelsList;
	protected TextView empty;
	
	protected long studentId;
	protected List<Label> labels;
	protected LabelAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.labels_screen);
		setScreenTitle(R.string.labels);
		getIntentData();
		initializeViews();
		
		adapter = new LabelAdapter(this, labels);
		labelsList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateLabels();
	}
	
	protected void updateLabels() {
		labels = dbStorage.getLabels();
		if (Utils.isEmpty(labels)) {
			empty.setVisibility(View.VISIBLE);
			labelsList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			labelsList.setVisibility(View.VISIBLE);
			adapter.setLabels(labels);
			adapter.notifyDataSetChanged();
		}
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_STUDENT_ID)) {
			studentId = intent.getLongExtra(DatabaseHelper.FIELD_STUDENT_ID, 0);
		}
	}
	
	protected void initializeViews() {
		addCustomLabel = (TextView) findViewById(R.id.addCustomLabel);
		addCustomLabel.setOnClickListener(this);
		
		labelsList = (ListView) findViewById(R.id.labelsList);
		labelsList.setOnItemClickListener(this);
		registerForContextMenu(labelsList);
		
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addCustomLabel:
			showAddCustomLabelDialog();
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Label label = labels.get(position);
		dbStorage.addLabelToStudent(label, studentId);
		setResult(RESULT_OK);
		finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Label label = labels.get(info.position);
		menu.setHeaderTitle(label.getName());
		menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Label label = labels.get(info.position);
		switch (item.getItemId()) {
		case DELETE_MENU_ITEM:
			dbStorage.deleteLabel(label);
			updateLabels();
			break;
		}
		return true;
	}
	
	protected void showAddCustomLabelDialog() {		
		final EditText name = new EditText(this);
		name.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_label)
			.setView(name)
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String labelName = name.getText().toString().trim();
					if (!TextUtils.isEmpty(labelName)) {
						Label label = new Label(0, labelName);
						dbStorage.addLabel(label);
						updateLabels();
					} else {
						showErrorDialog(R.string.label_name_empty);
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
