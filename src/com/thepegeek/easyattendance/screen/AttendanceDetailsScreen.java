package com.thepegeek.easyattendance.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.AttendanceDetailsAdapter;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Category;
import com.thepegeek.easyattendance.model.Category.Item;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class AttendanceDetailsScreen extends BaseScreen implements OnGroupClickListener {
	
	protected ExpandableListView list;
	protected TextView empty;
	
	protected long studentId;
	protected AttendanceDetailsAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attendance_details_screen);
		setScreenTitle(R.string.attendance_details);
		getIntentData();
		initializeViews();
		initAdapter();
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_STUDENT_ID)) {
			studentId = intent.getLongExtra(DatabaseHelper.FIELD_STUDENT_ID, 0);
		}
	}
	
	protected void initializeViews() {
		list = (ExpandableListView) findViewById(R.id.list);
		list.setOnGroupClickListener(this);
		empty = (TextView) findViewById(R.id.empty);
	}
	
	protected void initAdapter() {
		boolean takePart = dbStorage.hasStudentTakePartInAnyAttendance(studentId);
		if (takePart) {
			empty.setVisibility(View.INVISIBLE);
			list.setVisibility(View.VISIBLE);
			
			List<Category> groups = new ArrayList<Category>();
			HashMap<Status, List<Attendance>> map = new HashMap<Status, List<Attendance>>();
			initMap(map);
			
			if (map.size() > 0) {
				Set<Entry<Status, List<Attendance>>> entries = map.entrySet();
				Iterator<Entry<Status, List<Attendance>>> i = entries.iterator();
				while (i.hasNext()) {
					Entry<Status, List<Attendance>> entry = i.next();
					Status status = entry.getKey();
					List<Attendance> values = entry.getValue();
					Category group = new Category(status.getName(), values.size(), null);
					List<Item> items = new ArrayList<Item>();
					if (!Utils.isEmpty(values)) {
						for (Attendance attendance : values) {
							Course course = dbStorage.getCourseById(attendance.getCourseId());
							Item item = group.new Item(course.getName(), attendance.getDateString());
							items.add(item);
						}
					}
					group.setItems(items);
					groups.add(group);
				}
			}			
			
			adapter = new AttendanceDetailsAdapter(this, groups);
			list.setAdapter(adapter);
		} else {
			empty.setVisibility(View.VISIBLE);
			list.setVisibility(View.INVISIBLE);
		}
	}
	
	protected void initMap(HashMap<Status, List<Attendance>> map) {
		List<Course> courses = dbStorage.getCourses();
		if (!Utils.isEmpty(courses)) {
			for (Course course : courses) {
				List<Attendance> attendances = dbStorage.getAttendancesByCourseId(course.getId(), true);
				if (!Utils.isEmpty(attendances)) {
					for (Attendance attendance : attendances) {
						Student student = attendance.getStudent(studentId);
						if (student != null) {
							Status status = student.getStatus();
							if (status != null) {
								if (map.containsKey(status)) {
									List<Attendance> atts = map.get(status);
									if (!atts.contains(attendance)) {
										atts.add(attendance);
									}
								} else {
									List<Attendance> atts = new ArrayList<Attendance>();
									if (!atts.contains(attendance)) {
										atts.add(attendance);
									}
									map.put(status, atts);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		Drawable img = getResources().getDrawable(parent.isGroupExpanded(groupPosition) ? R.drawable.cell_accessory_rotated : R.drawable.cell_accessory);
		((TextView) v).setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		return false;
	}

}
