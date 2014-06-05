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
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseStorage;
import com.thepegeek.easyattendance.view.StatusView;

public class AttendanceStudentAdapter extends BaseAdapter {
	
	public static final String TAG = AttendanceStudentAdapter.class.getSimpleName(); 
	
	protected Context context;
	protected List<Student> students;
	protected List<Status> statuses;
	protected long attendanceId;
	
	public AttendanceStudentAdapter(Context context, List<Student> students, long attendanceId) {
		this.context = context;
		setStudents(students);
		statuses = DatabaseStorage.getInstance(context).getStatuses();
		this.attendanceId = attendanceId;
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
			convertView = inflater.inflate(R.layout.attendance_student_list_item, null);
		}
		final Student student = students.get(position);
		((TextView) convertView.findViewById(R.id.name)).setText(student.getFullname());
		
		StatusView statusView = (StatusView) convertView.findViewById(R.id.status);
		statusView.setStatuses(statuses);
		statusView.setCurrent(student.getStatus());
		statusView.setOnStatusChangeListener(new StatusView.OnStatusChangeListener() {
			@Override
			public void onStatusChanged(Status status) {
				student.setStatus(status);
				DatabaseStorage.getInstance(context).updateAttendanceStudent(attendanceId, student.getId(), status.getId());
			}
		});
		return convertView;
	}
	
	public void setStudents(List<Student> students) {
		this.students = (students != null) ? students : new ArrayList<Student>();
	}
	
	class ViewHolder {
		TextView name;
		StatusView status;
	}

}
