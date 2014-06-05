package com.thepegeek.easyattendance.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.EntryRecord;

public class EntryRecordAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<EntryRecord> contents;
	
	public EntryRecordAdapter(Context context, List<EntryRecord> contents) {
		this.context = context;
		setContents(contents);
	}

	@Override
	public int getCount() {
		return contents.size();
	}

	@Override
	public EntryRecord getItem(int position) {
		return contents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.entry_list_item, null);
			holder = new ViewHolder();
			
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.size = (TextView) convertView.findViewById(R.id.size);
			holder.checked = (ImageView) convertView.findViewById(R.id.checked);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		EntryRecord record = contents.get(position);
		if (record != null) {
			Entry entry = record.getEntry();
			holder.name.setText(entry.fileName());
			if (entry.isDir) {
				holder.icon.setImageResource(R.drawable.ic_file_folder);
				holder.size.setVisibility(View.INVISIBLE);
				holder.checked.setVisibility(View.GONE);
			} else {
				holder.icon.setImageResource(R.drawable.ic_file_file);
				holder.size.setVisibility(View.VISIBLE);
				holder.size.setText(entry.size);
				holder.checked.setVisibility(record.isChecked() ? View.VISIBLE : View.GONE);
			}
		}

		return convertView;
	}
	
	public void setContents(List<EntryRecord> contents) {
		this.contents = (contents != null) ? contents : new ArrayList<EntryRecord>(); 
	}
	
	class ViewHolder {
		ImageView icon;
		TextView name;
		TextView size;
		ImageView checked;
	}

}
