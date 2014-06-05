package com.thepegeek.easyattendance.model;


public class Course {
	
	protected long id;
	protected String name;
	protected long studentsCount;
	protected long attendancesCount;
	
	public Course(String name) {
		this.name = name;
	}
	
	public Course(long id, String name, long studentsCount, long attendancesCount) {
		this.id = id;
		this.name = name;
		this.studentsCount = studentsCount;
		this.attendancesCount = attendancesCount;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getStudentsCount() {
		return studentsCount;
	}
	
	public void setStudentsCount(long studentsCount) {
		this.studentsCount = studentsCount;
	}
	
	public long getAttendancesCount() {
		return attendancesCount;
	}
	
	public void setAttendancesCount(long attendancesCount) {
		this.attendancesCount = attendancesCount;
	}

}
