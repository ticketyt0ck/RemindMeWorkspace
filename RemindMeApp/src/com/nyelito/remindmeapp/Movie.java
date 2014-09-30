package com.nyelito.remindmeapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable, Comparable<Movie>, Release{
	
	private String title;
	
	private Date releaseDate;
	private String posterURL;
	
	private static final String CARD_DATE_FORMAT = "EEEE MMMM dd yyyy";
	
	public static class MovieComparator implements Comparator<Movie>{

		@Override
		public int compare(Movie lhs, Movie rhs) {
			return lhs.compareTo(rhs);
		}
	}
	
	public Movie(String title, Date releaseDate, String posterURL) {
		super();
		this.title = title;
		this.releaseDate = releaseDate;
		this.posterURL = posterURL;
	}

	private Movie(Parcel in) {
		title = in.readString();
		releaseDate = new Date(in.readLong());
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
	
	public String getPosterURL() {
		return posterURL;
	}

	public void setPosterURL(String posterURL) {
		this.posterURL = posterURL;
	}

	@Override
	public int describeContents() {
		return this.hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		if(releaseDate != null){
		dest.writeLong(releaseDate.getTime());
		}else{
			dest.writeLong(0L);
		}
		
	}
	
	public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
		public Movie createFromParcel(Parcel in) {
			return new Movie(in);
		}

		public Movie[] newArray(int size) {
			return new Movie[size];
		}
	};


	@Override
	public int compareTo(Movie another) {
		return releaseDate.compareTo(another.releaseDate);
	}

	public String getFormattedDate(){
		DateFormat dateFormat = new SimpleDateFormat(CARD_DATE_FORMAT);
		return dateFormat.format(releaseDate);

	}
	
	public String getReleaseMonthAndYear(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(releaseDate);
		return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + cal.get(Calendar.YEAR); 
	}
	
	

}
