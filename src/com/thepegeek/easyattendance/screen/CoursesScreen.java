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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.CourseAdapter;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class CoursesScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int EDIT_MENU_ITEM = 0;
	public static final int DELETE_MENU_ITEM = 1;

	protected Button addCourseBtn;
	protected Button statusesBtn;
	protected ListView coursesList;
	protected TextView empty;
	
	protected List<Course> courses;
	protected CourseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.courses_screen);
		setScreenTitle(R.string.add_view_courses);
		initializeViews();
		
		adapter = new CourseAdapter(this, courses);
		coursesList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateCourses();
	}
	
	protected void updateCourses() {
		courses = dbStorage.getCourses();
		if (Utils.isEmpty(courses)) {
			empty.setVisibility(View.VISIBLE);
			coursesList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			coursesList.setVisibility(View.VISIBLE);
			adapter.setCourses(courses);
			adapter.notifyDataSetChanged();
		}
	}
	
	protected void initializeViews() {
		addCourseBtn = (Button) findViewById(R.id.addCourseBtn);
		addCourseBtn.setOnClickListener(this);
		
		statusesBtn = (Button) findViewById(R.id.statusesBtn);
		statusesBtn.setOnClickListener(this);
		
		coursesList = (ListView) findViewById(R.id.coursesList);
		coursesList.setOnItemClickListener(this);
		registerForContextMenu(coursesList);
		
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addCourseBtn:
			showEditCourseDialog(null);
			break;
		case R.id.statusesBtn:
			Intent intent = new Intent(this, StatusesScreen.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (courses != null && !courses.isEmpty()) {
			Course course = courses.get(position);
			Intent intent = new Intent(this, CourseStudentsScreen.class);
			intent.putExtra(DatabaseHelper.FIELD_COURSE_ID, course.getId());
			startActivity(intent);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Course course = courses.get(info.position);
		menu.setHeaderTitle(course.getName());
		menu.add(0, EDIT_MENU_ITEM, 0, R.string.edit);
		menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Course course = courses.get(info.position);
		switch (item.getItemId()) {
		case EDIT_MENU_ITEM:
			showEditCourseDialog(course);
			break;
		case DELETE_MENU_ITEM:
			dbStorage.deleteCourse(course);
			updateCourses();
			break;
		}
		return true;
	}
	
	protected void showEditCourseDialog(final Course course) {
		final EditText courseName = new EditText(this);
		courseName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		courseName.setHint(R.string.course_name);
		courseName.setSingleLine();
		courseName.setText(course != null ? course.getName() : null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.new_course)
			.setView(courseName)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = courseName.getText().toString().trim();
					if (!TextUtils.isEmpty(name)) {
						if (course != null) {
							course.setName(name);
							dbStorage.updateCourse(course);
						} else {
							dbStorage.addCourse(new Course(name));
						}
						updateCourses();
					} else {
						showErrorDialog(R.string.course_name_empty);
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
