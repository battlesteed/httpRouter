package steed.router.api.domain;

import java.io.Serializable;

public class Parameter implements Serializable{
	private boolean require = true;
	private String type;
	private int maxLength = -1;
	private int minLength = -1;
	public boolean isRequire() {
		return require;
	}
	
	/*
	 * @Override public int hashCode() { if (name == null) { return 0; } return
	 * name.hashCode(); }
	 * 
	 * @Override public boolean equals(Object obj) { if (obj instanceof Parameter) {
	 * return name == null && ((Parameter)obj).name == null ||
	 * ((Parameter)obj).name.equals(name); } return false; }
	 */
	public void setRequire(boolean require) {
		this.require = require;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	public int getMinLength() {
		return minLength;
	}
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
	
}
