package com.thepegeek.easyattendance.model;

public class Group {
	
	protected long id;
	protected int count;
	
	public Group(long id, int count) {
		this.id = id;
		this.count = count;
	}
	
	public Group(int count) {
		this(0, count);
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean isRandom() {
		return count == 0;
	}

}
