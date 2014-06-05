package com.thepegeek.easyattendance.model;

import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;

public class EntryRecord {
	
	protected Entry entry;
	protected boolean checked;
	protected List<EntryRecord> children;
	protected EntryRecord parent;
	protected String downloadedPath;
	
	public EntryRecord(Entry entry, EntryRecord parent) {
		this.entry = entry;
		this.parent = parent;
		this.checked = false;
		children = new ArrayList<EntryRecord>();
	}
	
	public Entry getEntry() {
		return entry;
	}
	
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public EntryRecord getParent() {
		return parent;
	}
	
	public void setParent(EntryRecord parent) {
		this.parent = parent;
	}
		
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public List<EntryRecord> getChildren() {
		return children;
	}
	
	public void setChildren(List<EntryRecord> children) {
		this.children = children;
	}
	
	public String getDownloadedPath() {
		return downloadedPath;
	}
	
	public void setDownloadedPath(String downloadedPath) {
		this.downloadedPath = downloadedPath;
	}

}
