package steed.router.test.domain;

import java.util.Date;

import steed.hibernatemaster.domain.UUIDDomain;

public class Student extends UUIDDomain{
	private String name;
	private String address;
	private Integer number;
	private Boolean man;
	private Date enterDate;
	
	public Date getEnterDate() {
		return enterDate;
	}
	public void setEnterDate(Date enterDate) {
		this.enterDate = enterDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public Boolean getMan() {
		return man;
	}
	public void setMan(Boolean man) {
		this.man = man;
	}
	
}
