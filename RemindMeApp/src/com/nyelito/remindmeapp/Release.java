package com.nyelito.remindmeapp;

import java.util.Date;

public interface Release {
	
	public String getTitle();
	
	public String getFormattedDate();
	
	public Date getReleaseDate();
	
	public String getPosterURL();
	
	public String getReleaseMonthAndYear();
	
	
	public enum ReleaseType{
		MOVIE, TV, DVD;
	}

}
