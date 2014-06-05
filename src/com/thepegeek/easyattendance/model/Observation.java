package com.thepegeek.easyattendance.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Observation {
	
	public static final String DATE_FORMAT = "M/d/yy, h:mm a";  
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	
	protected long id;
	protected long studentId;
	protected String date;
	protected String note;
	
	public Observation(long id, long studentId, String date, String note) {
		this.id = id;
		this.studentId = studentId;
		this.date = date;
		this.note = note;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getStudentId() {
		return studentId;
	}
	
	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setDate(long milliseconds) {
		this.date = sdf.format(new Date(milliseconds));
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}

}
