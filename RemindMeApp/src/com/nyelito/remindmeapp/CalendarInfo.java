package com.nyelito.remindmeapp;

public class CalendarInfo {
	
	private long id;
	private String calendarName;
	private String accountType;
	
	
	
	
	
	public CalendarInfo(long id, String calendarName, String accountType) {
		super();
		this.id = id;
		this.calendarName = calendarName;
		this.accountType = accountType;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCalendarName() {
		return calendarName;
	}
	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
	

}
