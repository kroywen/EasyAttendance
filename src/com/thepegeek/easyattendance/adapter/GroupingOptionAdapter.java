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
import com.thepegeek.easyattendance.model.Group;

public class GroupingOptionAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Group> groups;
	protected long studentsSize;
	
	public GroupingOptionAdapter(Context context, List<Group> groups) {
		this.context = context;
		setGroups(groups);
	}

	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Group getItem(int position) {
		return groups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return groups.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.accessory_list_item, null);
		}
		
		Group group = groups.get(position);
		TextView name = (TextView) convertView.findViewById(R.id.name);
		if (group.isRandom()) {
			name.setText(R.string.random_student);
			name.setTextColor(context.getResources().getColor(R.color.dark_text));
		} else {
			boolean uneven = (studentsSize % group.getCount()) != 0;
			int countPattern = (uneven) ? R.string.groups_uneven_pattern : R.string.groups_even_pattern;
			name.setText(context.getString(countPattern, group.getCount()));
			
			int color = (uneven) ? R.color.red_text : R.color.dark_text;
			name.setTextColor(context.getResources().getColor(color));
		}
		
		return convertView;
	}
	
	public void setGroups(List<Group> groups) {
		this.groups = (groups != null) ? groups : new ArrayList<Group>();
	}
	
	public void setStudentsSize(long studentsSize) {
		this.studentsSize = studentsSize;
	}

}
