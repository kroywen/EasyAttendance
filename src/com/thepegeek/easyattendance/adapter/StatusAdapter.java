package com.thepegeek.easyattendance.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.storage.DatabaseStorage;

public class StatusAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Item> items;
	
	public enum ItemType {
		GROUP,
		STATUS
	}
	
	public StatusAdapter(Context context) {
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
			convertView = inflater.inflate(item.type != ItemType.GROUP ? R.layout.status_list_item : R.layout.group_list_item, null);
		}
		if (item.type == ItemType.GROUP) {
			TextView group = (TextView) convertView;
			group.setGravity(Gravity.CENTER_HORIZONTAL);
			group.setText(item.group);
		} else {
			final Status status = item.status;
			
			convertView.setBackgroundResource(status.isDefault() ? R.drawable.default_list_item_background : 0);
			
			convertView.findViewById(R.id.color).setBackgroundColor(status.getColor());
			
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(status.getName());
			
			ToggleButton absent = (ToggleButton) convertView.findViewById(R.id.absent);
			absent.setChecked(status.isAbsent());
			absent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					status.setAbsent(isChecked);
					DatabaseStorage.getInstance(context).updateStatus(status);
				}
			});
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
	
	public void removeAllItems() {
		items.clear();
	}
	
	public static class Item {
		public ItemType type;
		public Status status;
		public String group;
		
		public Item(String group) {
			this.type = ItemType.GROUP;
			this.group = group;
		}
		
		public Item(Status status) {
			this.type = ItemType.STATUS;
			this.status = status;
		}
	}

}
