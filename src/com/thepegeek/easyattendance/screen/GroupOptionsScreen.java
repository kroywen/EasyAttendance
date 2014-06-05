package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.GroupingOptionAdapter;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Group;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class GroupOptionsScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int EDIT_MENU_ITEM = 0;
	public static final int DELETE_MENU_ITEM = 1;
	
	protected View addNewGroupingOption;
	protected ListView groupingOptionsList;
	protected TextView empty;
	
	protected long courseId;
	protected Course course; 
	protected long studentsSize;
	protected List<Group> groups;
	protected GroupingOptionAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_options_screen);
		getIntentData();
		setScreenTitle(course != null ? course.getName() : getString(R.string.groups)); 
		initializeViews();
		
		adapter = new GroupingOptionAdapter(this, groups);
		adapter.setStudentsSize(studentsSize);
		groupingOptionsList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateGroupingOptions();
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_COURSE_ID)) {
			courseId = intent.getLongExtra(DatabaseHelper.FIELD_COURSE_ID, 0);
			course = dbStorage.getCourseById(courseId);
			studentsSize = dbStorage.getStudentsCount(courseId);
		}
	}
	
	protected void initializeViews() {
		addNewGroupingOption = findViewById(R.id.addNewGroupingOption);
		addNewGroupingOption.setOnClickListener(this);
		
		groupingOptionsList = (ListView) findViewById(R.id.groupingOptionsList);
		groupingOptionsList.setOnItemClickListener(this);
		registerForContextMenu(groupingOptionsList);
		
		empty = (TextView) findViewById(R.id.empty);
	}
	
	protected void updateGroupingOptions() {
		groups = dbStorage.getGroups();
		if (Utils.isEmpty(groups)) {
			empty.setVisibility(View.VISIBLE);
			groupingOptionsList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			groupingOptionsList.setVisibility(View.VISIBLE);
			adapter.setGroups(groups);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addNewGroupingOption) {
			showEditGroupDialog(null);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Group group = adapter.getItem(position);
		if (group != null) {
			if (group.isRandom()) {
				Intent intent = new Intent(this, RandomStudentScreen.class);
				intent.putExtra(DatabaseHelper.FIELD_COURSE_ID, courseId);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, GroupsScreen.class);
				intent.putExtra(DatabaseHelper.FIELD_COURSE_ID, courseId);
				intent.putExtra(DatabaseHelper.FIELD_GROUP_ID, group.getId());
				startActivity(intent);
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Group group = adapter.getItem(info.position);
		if (!group.isRandom()) {
			menu.setHeaderTitle(getString(R.string.group_count_pattern, group.getCount()));
			menu.add(0, EDIT_MENU_ITEM, 0, R.string.edit);
			menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Group group = adapter.getItem(info.position);
		switch (item.getItemId()) {
		case EDIT_MENU_ITEM:
			showEditGroupDialog(group);
			break;
		case DELETE_MENU_ITEM:
			dbStorage.deleteGroup(group);
			updateGroupingOptions();
			break;
		}
		return true;
	}
	
	protected void showEditGroupDialog(final Group group) {
		LayoutInflater infalter = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = infalter.inflate(R.layout.add_group_dialog, null);
		
		final EditText text = (EditText) view.findViewById(R.id.text);
		final Spinner count = (Spinner) view.findViewById(R.id.count);
		if (group != null) {
			text.setText(getString(R.string.groups_even_pattern, group.getCount()));
			count.setSelection(group.getCount()-2);
		} else {
			text.setText(getString(R.string.groups_even_pattern, 2));
			count.setSelection(0);
		}
		count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				text.setText(getString(R.string.groups_even_pattern, position+2));
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.new_option)
			.setView(view)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (group != null) {
						group.setCount(count.getSelectedItemPosition()+2);
						dbStorage.updateGroup(group);
					} else {
						Group group = new Group(count.getSelectedItemPosition()+2);
						dbStorage.addGroup(group);
					}
					updateGroupingOptions();
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
