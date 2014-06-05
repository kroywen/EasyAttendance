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
import com.thepegeek.easyattendance.model.Observation;

public class ObservationAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Observation> observations;
	
	public ObservationAdapter(Context context, List<Observation> observations) {
		this.context = context;
		setObservations(observations);
	}

	@Override
	public int getCount() {
		return observations.size();
	}

	@Override
	public Observation getItem(int position) {
		return observations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return observations.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.observation_list_item, null);
		}
		
		Observation observation = observations.get(position);
		((TextView) convertView.findViewById(R.id.date)).setText(observation.getDate());
		
		return convertView;
	}
	
	public void setObservations(List<Observation> observations) {
		this.observations = (observations != null) ? observations : new ArrayList<Observation>();
	}

}
