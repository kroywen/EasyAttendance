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
import com.thepegeek.easyattendance.model.Label;

public class LabelAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Label> labels;
	
	public LabelAdapter(Context context, List<Label> labels) {
		this.context = context;
		setLabels(labels);
	}

	@Override
	public int getCount() {
		return labels.size();
	}

	@Override
	public Label getItem(int position) {
		return labels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return labels.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.simple_list_item, null);
		}
		Label label = labels.get(position);
		((TextView) convertView).setText(label.getName());
		return convertView;
	}
	
	public void setLabels(List<Label> labels) {
		this.labels = (labels != null) ? labels : new ArrayList<Label>();
	}

}
