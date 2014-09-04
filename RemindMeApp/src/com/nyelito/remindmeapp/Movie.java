package com.nyelito.remindmeapp;

import java.util.Date;

public class Movie {
	
	private String title;
	
	private Date releaseDate;
	

	public Movie(String title, Date releaseDate) {
		super();
		this.title = title;
		this.releaseDate = releaseDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	

}
