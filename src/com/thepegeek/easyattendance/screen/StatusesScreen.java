package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.StatusAdapter;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.util.Utils;
import com.thepegeek.easyattendance.view.colorpicker.AmbilWarnaDialog;
import com.thepegeek.easyattendance.view.colorpicker.AmbilWarnaDialog.OnAmbilWarnaListener;

public class StatusesScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int SET_DEFAULT_MENU_ITEM = 0;
	public static final int CHANGE_COLOR_MENU_ITEM = 1;
	public static final int DELETE_MENU_ITEM = 2;
	
	protected Button addStatusBtn;
	protected ListView statusesList;
	
	protected StatusAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statuses_screen);
		setScreenTitle(R.string.status);
		initializeViews();
		
		adapter = new StatusAdapter(this);
		statusesList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateStatuses();
	}
	
	protected void initializeViews() {
		addStatusBtn = (Button) findViewById(R.id.addStatusBtn);
		addStatusBtn.setOnClickListener(this);
		
		statusesList = (ListView) findViewById(R.id.statusesList);
		statusesList.setOnItemClickListener(this);
		registerForContextMenu(statusesList);		
	}
	
	protected void updateStatuses() {
		adapter = new StatusAdapter(this);
		adapter.addItem(new StatusAdapter.Item(getString(R.string.common)));
		List<Status> defaultStatuses = dbStorage.getCommonStatuses();
		for (Status status : defaultStatuses) {
			adapter.addItem(new StatusAdapter.Item(status));
		}
		
		List<Status> userStatuses = dbStorage.getUserStatuses();
		if (!Utils.isEmpty(userStatuses)) {
			adapter.addItem(new StatusAdapter.Item(getString(R.string.user_created)));
			for (Status status : userStatuses) {
				adapter.addItem(new StatusAdapter.Item(status));
			}
		}
		statusesList.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addStatusBtn:
			showEditStatusDialog(null);
			break;
		}
	}
	
	protected void showEditStatusDialog(final Status status) {
		final EditText statusName = new EditText(this);
		statusName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		statusName.setHint(R.string.status_name);
		statusName.setSingleLine();
		statusName.setText(status != null ? status.getName() : null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(status == null ? R.string.new_status : R.string.name_will_be_autocapitalized)
			.setView(statusName)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = Utils.uppercaseFirstChar(statusName.getText().toString().trim());
					if (!TextUtils.isEmpty(name)) {
						if (status != null) {
							status.setName(name);
							dbStorage.updateStatus(status);
						} else {
							dbStorage.addStatus(new Status(name));
						}
						updateStatuses();
					} else {
						showErrorDialog(R.string.status_name_empty);
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Status status = adapter.getItem(info.position).status;
		menu.setHeaderTitle(status.getName());
		menu.add(0, SET_DEFAULT_MENU_ITEM, 0, R.string.set_as_default);
		menu.add(0, CHANGE_COLOR_MENU_ITEM, 0, R.string.change_color);
		if (!status.isCommon()) {
			menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Status status = adapter.getItem(info.position).status;
		switch (item.getItemId()) {
		case SET_DEFAULT_MENU_ITEM:
			if (!status.isDefault()) {
				dbStorage.setDefaultStatus(status);
				Parcelable state = statusesList.onSaveInstanceState();
				updateStatuses();
				statusesList.onRestoreInstanceState(state);
			}
			break;
		case CHANGE_COLOR_MENU_ITEM:
			showSelectColorDialog(status);
			break;
		case DELETE_MENU_ITEM:
			if (!status.isCommon()) {
				if (dbStorage.statusCanBeDeleted(status.getId())) {
					dbStorage.deleteStatus(status);
					updateStatuses();
				} else {
					showErrorDialog(R.string.status_is_used);
				}
			}
			break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Status status = adapter.getItem(position).status;
		if (status != null) {
			showEditStatusDialog(status);
		}
	}
	
	protected void showSelectColorDialog(final Status status) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, status.getColor(), new OnAmbilWarnaListener() {
	        @Override
	        public void onOk(AmbilWarnaDialog dialog, int color) {
	        	if (color != status.getColor()) {
	        		status.setColor(color);
	        		dbStorage.updateStatus(status);
	        		Parcelable state = statusesList.onSaveInstanceState();
					updateStatuses();
					statusesList.onRestoreInstanceState(state);
	        	}
	        }   
	        @Override
	        public void onCancel(AmbilWarnaDialog dialog) {}
		});
		dialog.show();
	}

}
