package com.thepegeek.easyattendance.screen;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.CourseAdapter;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class SelectCourse2Screen extends BaseScreen implements OnItemClickListener {
	
	protected ListView coursesList;
	protected TextView empty;
	
	protected List<Course> courses;
	protected CourseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_course_2_screen);
		setScreenTitle(R.string.select_course);
		initializeViews();
		
		adapter = new CourseAdapter(this, courses);
		coursesList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateCourses();
	}
	
	protected void initializeViews() {
		coursesList = (ListView) findViewById(R.id.coursesList);
		coursesList.setOnItemClickListener(this);
		
		empty = (TextView) findViewById(R.id.empty);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Course course = courses.get(position);
		Intent intent = new Intent(this, GroupOptionsScreen.class);
		intent.putExtra(DatabaseHelper.FIELD_COURSE_ID, course.getId());
		startActivity(intent);
	}

}
