package com.thepegeek.easyattendance.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Course;

public class SelectCourseAdapter extends CourseAdapter {

	public SelectCourseAdapter(Context context, List<Course> courses) {
		super(context, courses);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.course_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.students = (TextView) convertView.findViewById(R.id.students);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag(); 
		}
		
		Course course = courses.get(position);
		holder.name.setText(course.getName());
		holder.students.setText(context.getString(R.string.attendances_count_pattern, course.getAttendancesCount()));
		
		return convertView;
	}

}
