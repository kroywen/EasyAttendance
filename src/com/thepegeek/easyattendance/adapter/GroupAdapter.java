package com.thepegeek.easyattendance.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Student;

public class GroupAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Item> items;
	
	public enum ItemType {
		GROUP,
		STUDENT
	}
	
	public GroupAdapter(Context context) {
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
			convertView = inflater.inflate(item.type != ItemType.GROUP ? R.layout.simple_list_item : R.layout.group_list_item, null);
		}
		if (item.type == ItemType.GROUP) {
			TextView group = (TextView) convertView;
			group.setGravity(Gravity.CENTER_HORIZONTAL);
			group.setText(item.group);
		} else {
			Student student = item.student;
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(student.getFullname());
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
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return items.get(position).type != ItemType.GROUP;
	}
	
	public void setItems(List<Item> items) {
		this.items = (items != null) ? items : new ArrayList<Item>();
	}
	
	public void addItem(Item item) {
		items.add(item);
	}
	
	public static class Item {
		public ItemType type;
		public Student student;
		public String group;
		
		public Item(String group) {
			this.type = ItemType.GROUP;
			this.group = group;
		}
		
		public Item(Student student) {
			this.type = ItemType.STUDENT;
			this.student = student;
		}
	}

}
