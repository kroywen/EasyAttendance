package com.thepegeek.easyattendance.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.thepegeek.easyattendance.util.Utils;

public class Attendance {
	
	public static final String DATE_FORMAT = "EEE, dd LLL, h:mm a, yyyy";  
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	
	public static final String DATE_FORMAT2 = "M/d/yy, h:mm a";  
	public static final SimpleDateFormat sdf2 = new SimpleDateFormat(DATE_FORMAT2, Locale.getDefault());
	
	public static final String DATE_FORMAT3 = "M-d-yy";
	public static final SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT3, Locale.getDefault());
	
	public static final String DATE_FORMAT4 = "EEE, dd LLL, h-mm a, yyyy";
	public static final SimpleDateFormat sdf4 = new SimpleDateFormat(DATE_FORMAT4, Locale.getDefault());
	
	protected long id;
	protected long courseId;
	protected long date;
	protected String note;
	protected String photo;
	
	protected List<Student> students;
	
	public Attendance(long id, long courseId, long date, String note, String photo) {
		this.id = id;
		this.courseId = courseId;
		this.date = date;
		this.note = note;
		this.photo = photo;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getCourseId() {
		return courseId;
	}
	
	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
	public long getDate() {
		return date;
	}
	
	public String getDateString() {
		return sdf.format(new Date(date));	
	}
	
	public String getDateString2() {
		return sdf2.format(new Date(date));
	}
	
	public String getDateString3() {
		return sdf3.format(new Date(date));
	}
	
	public String getDateString4() {
		return sdf4.format(new Date(date));
	}
	
	public void setDate(long date) {
		this.date = date;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	public List<Student> getStudents() {
		return students;
	}
	
	public void setStudents(List<Student> students) {
		this.students = students;
	}
	
	public Student getStudent(long studentId) {
		if (!Utils.isEmpty(students)) {
			for (Student student : students) {
				if (student.getId() == studentId) {
					return student;
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attendance other = (Attendance) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

}
