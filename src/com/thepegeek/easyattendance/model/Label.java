package com.thepegeek.easyattendance.model;

public class Label {
	
	protected long id;
	protected long labelId;
	protected String name;
	protected long studentId;
	protected String value;
	
	public Label(long id, long labelId, String name, long studentId, String value) {
		this.id = id;
		this.labelId = labelId;
		this.name = name;
		this.studentId = studentId;
		this.value = value;
	}
	
	public Label(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getLabelId() {
		return labelId;
	}
	
	public void setLabelId(long labelId) {
		this.labelId = labelId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getStudentId() {
		return studentId;
	}
	
	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

}
