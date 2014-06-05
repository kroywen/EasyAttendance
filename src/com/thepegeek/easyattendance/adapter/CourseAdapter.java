package com.thepegeek.easyattendance.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Course;

public class CourseAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Course> courses;
	
	public CourseAdapter(Context context, List<Course> courses) {
		this.context = context;
		setCourses(courses);
	}

	@Override
	public int getCount() {
		return courses.size();
	}

	@Override
	public Course getItem(int position) {
		return courses.get(position);
	}

	@Override
	public long getItemId(int position) {
		return courses.get(position).getId();
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
		holder.students.setText(context.getString(R.string.students_count_pattern, course.getStudentsCount()));
		
		return convertView;
	}
	
	class ViewHolder {
		TextView name;
		TextView students;
	}
	
	public void setCourses(List<Course> courses) {
		this.courses = (courses != null) ? courses : new ArrayList<Course>();
	}

}
