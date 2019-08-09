package steed.router.api.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReturnVal implements Serializable{
	public static final boolean defaultRequire = true;
	
	private String indent = "　";
	
	private String id;
	
	private Boolean require;
	/**
	 * 无需在json文件配置,自动填充
	 */
	private String name;
	/**
	 * 无需在json文件配置,自动填充
	 */
	private String type;
	private String desc;
	private int maxLength = -1;
	private int minLength = -1;
	
	private List<ReturnVal> returns = new ArrayList<ReturnVal>();
	
	public Boolean isRequire() {
		return require;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public void setDesc(String desc) {
		this.desc = desc;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getRequire() {
		return require;
	}

	public void setRequire(Boolean require) {
		this.require = require;
	}

	public void addSonReturn(ReturnVal son) {
		String sonIndent = indent + "　";
		son.name = indent + son.name;
		son.indent = sonIndent;
		returns.add(son);
	}
	
	public List<ReturnVal> getReturns() {
		return returns;
	}

	public void setReturns(List<ReturnVal> returns) {
		this.returns = returns;
	}
	
}
