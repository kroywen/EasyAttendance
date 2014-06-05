package com.thepegeek.easyattendance.screen;

import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.GroupAdapter;
import com.thepegeek.easyattendance.model.Group;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class GroupsScreen extends BaseScreen implements OnClickListener {
	
	protected Button refreshBtn;
	protected ListView studentsList;
	protected TextView empty;
	
	protected long courseId;
	protected List<Student> students;
	protected long groupId;
	protected Group group;
	
	protected GroupAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groups_screen);
		getIntentData();
		setScreenTitle(group != null ? getString(R.string.groups_even_pattern, group.getCount()) : null);
		initializeViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateStudents();
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			courseId = intent.getLongExtra(DatabaseHelper.FIELD_COURSE_ID, 0);
			groupId = intent.getLongExtra(DatabaseHelper.FIELD_GROUP_ID, 0);
			group = dbStorage.getGroupById(groupId);
		}
	}
	
	protected void initializeViews() {
		refreshBtn = (Button) findViewById(R.id.refreshBtn);
		refreshBtn.setText(R.string.refresh);
		refreshBtn.setVisibility(View.VISIBLE);
		refreshBtn.setOnClickListener(this);
		
		studentsList = (ListView) findViewById(R.id.studentsList);
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.refreshBtn) {
			updateStudents();
		}
	}
	
	protected void updateStudents() {
		students = dbStorage.getStudentsByCourseId(courseId);
		if (Utils.isEmpty(students)) {
			empty.setVisibility(View.VISIBLE);
			studentsList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			studentsList.setVisibility(View.VISIBLE);
			Collections.shuffle(students);
			initAdapter();
		}
	}
	
	protected void initAdapter() {
		Parcelable state = studentsList.onSaveInstanceState();
		adapter = new GroupAdapter(this);
		int groupIndex = 1;
		boolean groupAdded = false;
		int i = 0;
		while (i < students.size()) {
//			if (i % group.getCount() == 0 && !groupAdded && i != students.size()-1) {
			if (i % group.getCount() == 0 && !groupAdded) {
				adapter.addItem(new GroupAdapter.Item(getString(R.string.group_count_pattern, groupIndex)));
				groupIndex++;
				groupAdded = true;
			} else {
				adapter.addItem(new GroupAdapter.Item(students.get(i)));
				i++;
				groupAdded = false;
			}
		}
		studentsList.setAdapter(adapter);
		studentsList.onRestoreInstanceState(state);
	}

}
