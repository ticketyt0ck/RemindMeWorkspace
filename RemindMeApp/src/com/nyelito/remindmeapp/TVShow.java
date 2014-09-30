package com.nyelito.remindmeapp;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class TVShow implements Parcelable, Release{
	
	private String title;
	private String id;
	private Date releaseDate;
	
	
	
	public TVShow(String name, String id) {
		super();
		this.title = name;
		this.id = id;
	}
	
	private TVShow(Parcel in) {
		title = in.readString();
		id = in.readString();
		releaseDate = new Date(in.readLong());
	}
	public void setTitle(String name) {
		this.title = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getReleaseDate() {
		return new Date();
//		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	@Override
	public int describeContents() {
		return this.hashCode();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(id);
		if(releaseDate != null){
		dest.writeLong(releaseDate.getTime());
		}else{
			dest.writeLong(0L);
		}
		
	}
	

	public static final Parcelable.Creator<TVShow> CREATOR = new Parcelable.Creator<TVShow>() {
		public TVShow createFromParcel(Parcel in) {
			return new TVShow(in);
		}

		public TVShow[] newArray(int size) {
			return new TVShow[size];
		}
	};



	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getFormattedDate() {
		// TODO Auto-generated method stub
		return "Uhh...today";
	}

	@Override
	public String getPosterURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReleaseMonthAndYear() {
		// TODO Auto-generated method stub
		return "HIII";
	}


}
