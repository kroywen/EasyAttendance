package com.thepegeek.easyattendance.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Category;
import com.thepegeek.easyattendance.model.Category.Item;

public class AttendanceDetailsAdapter extends BaseExpandableListAdapter {
	
	protected Context context;
	protected List<Category> groups;
	
	public AttendanceDetailsAdapter(Context context, List<Category> groups) {
		this.context = context;
		this.groups = groups;
	}

	@Override
	public Item getChild(int groupPosition, int childPosition) {
		return groups.get(groupPosition).getItems().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition*10 + childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.attendance_details_list_item, null);
		}
		
		Item child = groups.get(groupPosition).getItems().get(childPosition);
		((TextView) convertView.findViewById(R.id.courseName)).setText(child.getCourseName());
		((TextView) convertView.findViewById(R.id.attendanceDate)).setText(child.getAttendanceDate());
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return groups.get(groupPosition).getItems().size();
	}

	@Override
	public Category getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.attendance_group_list_item, null);
		}
		Category group = groups.get(groupPosition);
		TextView tv = (TextView) convertView;
		tv.setText(group.getTitle());
		Drawable img = context.getResources().getDrawable(isExpanded ? R.drawable.cell_accessory_rotated : R.drawable.cell_accessory);
		tv.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
