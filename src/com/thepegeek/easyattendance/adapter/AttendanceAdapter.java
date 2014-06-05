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
import com.thepegeek.easyattendance.model.Attendance;

public class AttendanceAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Attendance> attendances;
	
	public AttendanceAdapter(Context context, List<Attendance> attendances) {
		this.context = context;
		setAttendances(attendances);
	}

	@Override
	public int getCount() {
		return attendances.size();
	}

	@Override
	public Attendance getItem(int position) {
		return attendances.get(position);
	}

	@Override
	public long getItemId(int position) {
		return attendances.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.accessory_list_item, null);
		}
		
		Attendance attendance = attendances.get(position);
		((TextView) convertView.findViewById(R.id.name)).setText(attendance.getDateString());
		
		return convertView;
	}
	
	public void setAttendances(List<Attendance> attendances) {
		this.attendances = (attendances != null) ? attendances : new ArrayList<Attendance>();
	}

}
