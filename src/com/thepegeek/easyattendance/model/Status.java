package com.thepegeek.easyattendance.model;

public class Status {
	
	public static final int[] defaultColors = {0xff00aa00, 0xfffd0000, 0xfffc7f00, 0xff0000fb};
	
	public static final int COLOR_RED = 0xfffd0000;
	public static final int COLOR_GREEN = 0xff00aa00;
	
	protected long id;
	protected String name;
	protected boolean absent;
	protected boolean common;
	protected boolean _default;
	protected int color;
	
	public Status(long id, String name, boolean absent, boolean common, boolean _default, int color) {
		this.id = id;
		this.name = name;
		this.absent = absent;
		this.common = common;
		this._default = _default;
		this.color = color;
	}
	
	public Status(String name) {
		this(0, name, true, false, false, COLOR_RED);
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
	
	public boolean isAbsent() {
		return absent;
	}
	
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}
	
	public boolean isCommon() {
		return common;
	}
	
	public void setCommon(boolean common) {
		this.common = common;
	}
	
	public boolean isDefault() {
		return _default;
	}
	
	public void setDefault(boolean _default) {
		this._default = _default;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
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
		Status other = (Status) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
