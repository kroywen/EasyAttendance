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

public class ReportAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Item> items;
	
	public enum ItemType {
		ALL_COURSES,
		COURSE
	}
	
	public ReportAdapter(Context context) {
		this.context = context;
		setItems(null);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Item getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Item item = items.get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(item.type != ItemType.ALL_COURSES ? R.layout.course_list_item : R.layout.accessory_list_item, null);
		}
		if (item.type == ItemType.ALL_COURSES) {
			((TextView) convertView.findViewById(R.id.name)).setText(context.getString(R.string.all_courses)); 
		} else {
			Course course = item.course;
			((TextView) convertView.findViewById(R.id.name)).setText(course.getName());
			((TextView) convertView.findViewById(R.id.students)).setText(context.getString(R.string.attendances_count_pattern, course.getAttendancesCount()));
		}
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
		return items.get(position).type.ordinal();
	}
	
	@Override
	public int getViewTypeCount() {
		return ItemType.values().length;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return true;
	}
	
	public void setItems(List<Item> items) {
		this.items = (items != null) ? items : new ArrayList<Item>();
	}
	
	public void addItem(Item item) {
		items.add(item);
	}
	
	public void removeAllItems() {
		items.clear();
	}
	
	public static class Item {
		public ItemType type;
		public Course course;
		
		public Item(ItemType type) {
			this.type = type;
		}
		
		public Item(Course course) {
			this.type = ItemType.COURSE;
			this.course = course;
		}
	}

}
