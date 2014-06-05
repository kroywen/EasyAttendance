package com.thepegeek.easyattendance.model;

import android.text.TextUtils;

public class Student {
	
	protected long id;
	protected String firstname;
	protected String lastname;
	protected String photo;
	protected Status status;
	
	public Student(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public Student(long id, String firstname, String lastname, String photo) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.photo = photo;
	}
	
	public long getId() {
		return id;
	}
	
	public void setid(long id) {
		this.id = id;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getFullname() {
		String fullname = firstname;
		if (!TextUtils.isEmpty(lastname)) {
			fullname += " " + lastname;
		}
		return fullname;
	}
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

}
