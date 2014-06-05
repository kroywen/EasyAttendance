package com.thepegeek.easyattendance.model;

import java.util.List;

public class Category {
	
	protected String name;
	protected int count;
	protected List<Item> items;
	
	public Category(String name, int count, List<Item> items) {
		this.name = name;
		this.count = count;
		this.items = items;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public String getTitle() {
		return name + ": " + count;
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public void setItems(List<Item> items) {
		this.items = items;
	}

	
	public class Item {
		
		protected String courseName;
		protected String attendanceDate;
		
		public Item(String courseName, String attendanceDate) {
			this.courseName = courseName;
			this.attendanceDate = attendanceDate;
		}
		
		public String getCourseName() {
			return courseName;
		}
		
		public void setCourseName(String courseName) {
			this.courseName = courseName;
		}
		
		public String getAttendanceDate() {
			return attendanceDate;
		}
		
		public void setAttendanceDate(String attendanceDate) {
			this.attendanceDate = attendanceDate;
		}
	}

}
