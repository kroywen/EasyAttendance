package com.thepegeek.easyattendance.screen;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.EntryRecordAdapter;
import com.thepegeek.easyattendance.model.EntryRecord;
import com.thepegeek.easyattendance.storage.EntryHolder;
import com.thepegeek.easyattendance.util.Utils;

public class DropboxScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
    
	protected Button leftBtn;
	protected Button rightBtn;
	protected ListView list;
	protected ImageView backBtn;
	protected TextView selected;
	
	protected EntryHolder entryHolder;
	protected EntryRecordAdapter adapter;
	
	protected boolean infoDialogShowed;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dropbox_screen);
		setTitle(R.string.my_dropbox);
		initializeViews();
		
		entryHolder = EntryHolder.getInstance(mApi);
		infoDialogShowed = true;
        (new InitEntryTask()).execute((Void[]) null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	protected void showInformationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.information)
			.setMessage(R.string.dropbox_dialog_info_test)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	protected void initializeViews() {
		leftBtn = (Button) findViewById(R.id.leftBtn);
		leftBtn.setVisibility(View.VISIBLE);
		leftBtn.setText(R.string.close);
		leftBtn.setOnClickListener(this);
		
		rightBtn = (Button) findViewById(R.id.rightBtn);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setText(R.string.download);
		rightBtn.setOnClickListener(this);
		
		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		
		backBtn = (ImageView) findViewById(R.id.back);
		backBtn.setOnClickListener(this);
		
		selected = (TextView) findViewById(R.id.selected);
		selected.setText(String.format(getString(R.string.selected_pattern), 0));
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.leftBtn:
			onBackPressed();
			break;
		case R.id.rightBtn:
			int checked = entryHolder.getCheckedCount();
			if (checked == 0) {
				Toast.makeText(this, R.string.no_files_selected, Toast.LENGTH_SHORT).show();
			} else {
				(new DownloadTask(entryHolder.getCheckedEntries())).execute((Void[]) null);
			}
			break;
		case R.id.back:
			back();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		EntryRecord record = (EntryRecord) parent.getItemAtPosition(position);
		if (record != null) {
			Entry entry = record.getEntry();
			if (entry != null) {
				if (entry.isDir) {
					entryHolder.setCurrent(record);
					if (record.getChildren() != null && !record.getChildren().isEmpty()) {
						adapter.setContents(record.getChildren());
						adapter.notifyDataSetChanged();
					} else {
						(new InitEntryTask()).execute((Void[]) null);
					}
				} else {
					record.setChecked(!record.isChecked());
					adapter.notifyDataSetChanged();
					int checked = entryHolder.getCheckedCount();
					selected.setText(String.format(getString(R.string.selected_pattern), checked));
				}
			}
		}
	}
	
	protected void back() {
		entryHolder.back();
		EntryRecord current = entryHolder.getCurrent();
		if (current != null) {
			adapter.setContents(current.getChildren());
			adapter.notifyDataSetChanged();
		}
	}
    
    class InitEntryTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		showProgressDialog(getString(R.string.loading));
    	}

		@Override
		protected Void doInBackground(Void... unused) {
			if (entryHolder.getRoot() == null) {
				entryHolder.initRoot(mApi);
			} else {
				entryHolder.populateChildEntries(mApi, entryHolder.getCurrent());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			hideProgressDialog();
			if (entryHolder.getCurrent() != null) {
				List<EntryRecord> contents = entryHolder.getCurrent().getChildren();
				if (adapter == null) {
					adapter = new EntryRecordAdapter(DropboxScreen.this, contents);
					list.setAdapter(adapter);
				}
				adapter.setContents(contents);
				adapter.notifyDataSetChanged();
			}
			
			if (!infoDialogShowed) {
				infoDialogShowed = true;
				showInformationDialog();
			}
		}
    }
    
    class DownloadTask extends AsyncTask<Void, Long, Boolean> {
    	
    	protected ProgressDialog dialog;
    	protected List<EntryRecord> records;
    	protected String error;
    	
    	protected String currentName;
    	
    	public DownloadTask(List<EntryRecord> records) {
    		this.records = records;
    		dialog = new ProgressDialog(DropboxScreen.this);
    		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		dialog.setMessage(getString(R.string.downloading));
    		dialog.setIndeterminate(true);
    	}
    	
    	@Override
    	public void onPreExecute() {
    		dialog.show();
    	}

		@Override
		protected Boolean doInBackground(Void... unused) {
			try {
				File docDir = new File(Environment.getExternalStorageDirectory(), "/easyassessment/csv/");
				docDir.mkdirs();
				
				for (EntryRecord record : records) {
					Entry entry = record.getEntry();
					File file = new File(docDir, entry.fileName());
					currentName = entry.fileName();
					FileOutputStream fos = new FileOutputStream(file);
					mApi.getFile(entry.path, null, fos, new ProgressListener() {
						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes, total);
						}
					});
					record.setChecked(false);
					record.setDownloadedPath(Environment.getExternalStorageDirectory() + "/easyassessment/csv/" + entry.fileName());
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				error = getString(R.string.download_error);
				return false;
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Long... values) {
			String message = String.format(getString(R.string.download_pattern), 
					currentName, Utils.getReadableFilesize(values[0]), Utils.getReadableFilesize(values[1]));
			dialog.setMessage(message);
			super.onProgressUpdate(values);
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			if (!result.booleanValue()) {
				Toast.makeText(DropboxScreen.this, error, Toast.LENGTH_SHORT).show();
			} else {
				setResult(RESULT_OK);
				finish();
			}
		}
    	
    }

}
