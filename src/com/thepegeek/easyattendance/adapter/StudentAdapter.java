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
import com.thepegeek.easyattendance.model.Student;

public class StudentAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Student> students;
	
	public StudentAdapter(Context context, List<Student> students) {
		this.context = context;
		setStudents(students);
	}

	@Override
	public int getCount() {
		return students.size();
	}

	@Override
	public Student getItem(int position) {
		return students.get(position);
	}

	@Override
	public long getItemId(int position) {
		return students.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.accessory_list_item, null);
		}
		
		Student student = students.get(position);
		((TextView) convertView).setText(student.getFullname());
		
		return convertView;
	}
	
	public void setStudents(List<Student> students) {
		this.students = (students != null) ? students : new ArrayList<Student>();
	}

}
