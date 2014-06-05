package com.thepegeek.easyattendance.storage;

import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.thepegeek.easyattendance.model.EntryRecord;

public class EntryHolder {
	
	protected DropboxAPI<AndroidAuthSession> api;
	protected static EntryHolder instance;
	
	protected EntryRecord root;
	protected EntryRecord current;
	
	protected EntryHolder(DropboxAPI<AndroidAuthSession> api) {
		this.api = api;
		root = null;
		current = root;
	}
	
	public static EntryHolder getInstance(DropboxAPI<AndroidAuthSession> api) {
		if (instance == null)
			instance = new EntryHolder(api);
		return instance;
	}
	
	public boolean initRoot(DropboxAPI<AndroidAuthSession> api) {
		this.api = api;
		try {
			Entry contact = api.metadata("/", 0, null, true, null);
			root = new EntryRecord(contact, null);
			populateChildEntries(api, root);
			current = root;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void populateChildEntries(DropboxAPI<AndroidAuthSession> api, EntryRecord record) {
		this.api = api;
		
		if (record == null)
			return;
		
		Entry entry = record.getEntry();
		if (entry == null)
			return;
		
		List<Entry> contents = entry.contents;
		if (contents == null || contents.isEmpty())
			return;
		
		for (Entry content : contents) {
			if (content.isDir) {
				try {
					Entry contact = api.metadata(content.path, 0, null, true, null);
					EntryRecord dirRecord = new EntryRecord(contact, record);
					record.getChildren().add(dirRecord);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				EntryRecord fileRecord = new EntryRecord(content, record);
				String ext = content.fileName().substring(content.fileName().lastIndexOf('.')+1);
				if (ext != null && ext.equalsIgnoreCase("csv"))
					record.getChildren().add(fileRecord);
			}
		}
	}
	
	public EntryRecord getRoot() {
		return root;
	}
	
	public void setRoot(EntryRecord root) {
		this.root = root;
	}
	
	public EntryRecord getCurrent() {
		return current;
	}
	
	public void setCurrent(EntryRecord current) {
		this.current = current;
	}
	
	public void back() {
		if (current == null || current.getParent() == null)
			return;
		setCurrent(current.getParent());
	}
	
	public int getCheckedCount() {
		return getCheckedCount(root);
	}
	
	protected int getCheckedCount(EntryRecord record) {
		if (record == null)
			return 0;
		int checked = 0;
		Entry entry = record.getEntry();
		if (entry != null) {
			if (entry.isDir) {
				for (EntryRecord r : record.getChildren())
					checked += getCheckedCount(r);
			} else {
				if (record.isChecked())
					checked++;
			}
		}
		return checked;
	}
	
	public List<EntryRecord> getCheckedEntries() {
		List<EntryRecord> entries = new ArrayList<EntryRecord>();
		getCheckedEntries(root, entries);
		return entries;
	}
	
	protected void getCheckedEntries(EntryRecord record, List<EntryRecord> entries) {
		if (record == null)
			return;
		Entry entry = record.getEntry();
		if (entry != null) {
			if (entry.isDir) {
				for (EntryRecord r : record.getChildren())
					getCheckedEntries(r, entries);
			} else {
				if (record.isChecked()) {
					entries.add(record);
				}
			}
		}
	}
	
	public void uncheckEntries(EntryRecord record) {
		if (record == null)
			return;
		Entry entry = record.getEntry();
		if (entry.isDir) {
			for (EntryRecord r : record.getChildren())
				uncheckEntries(r);
		} else {
			record.setChecked(false);
			record.setDownloadedPath(null);
		}
	}
	
	public List<EntryRecord> getDownloadedEntries() {
		List<EntryRecord> entries = new ArrayList<EntryRecord>();
		getDownloadedEntries(root, entries);
		return entries;
	}
	
	protected void getDownloadedEntries(EntryRecord record, List<EntryRecord> entries) {
		if (record == null)
			return;
		Entry entry = record.getEntry();
		if (entry != null) {
			if (entry.isDir) {
				for (EntryRecord r : record.getChildren())
					getDownloadedEntries(r, entries);
			} else {
				if (record.getDownloadedPath() != null && record.getDownloadedPath().length() > 0) {
					entries.add(record);
				}
			}
		}
	}
	
	public void clear() {
		root = null;
	}

}
