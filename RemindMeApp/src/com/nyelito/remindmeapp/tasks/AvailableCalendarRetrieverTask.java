package com.nyelito.remindmeapp.tasks;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CalendarContract.Calendars;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.nyelito.remindmeapp.CalendarInfo;
import com.nyelito.remindmeapp.fragments.CalendarFragment;

public class AvailableCalendarRetrieverTask extends AsyncTask<CalendarFragment, Void, List<CalendarInfo>>{
	
	
	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT                  // 3
	};
	  
	
	private ProgressDialog progressDialog;
	private CalendarFragment currFragment;
	
	public AvailableCalendarRetrieverTask(CalendarFragment currFragment){
		this.currFragment = currFragment;
	}
	
	@Override
	protected void onPreExecute() {

		Fragment fragment = null;
		if(currFragment instanceof Fragment){
			fragment = (Fragment) currFragment;
		}
		progressDialog = new ProgressDialog(fragment.getActivity());
		progressDialog.setMessage("Finding calendars on this device...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();

	};

	@Override
	protected void onPostExecute(List<CalendarInfo> theList) {
		
		currFragment.handleCalendarList(theList);
		progressDialog.dismiss();

	};

	
	
	@Override
	protected List<CalendarInfo> doInBackground(CalendarFragment... params) {
		Fragment fragment;
		if(currFragment instanceof Fragment){
			fragment = (Fragment) currFragment;
			return retrieveAvailableCalendars(fragment.getActivity());
		}
		return new ArrayList<CalendarInfo>();
	}
	private static List<CalendarInfo> retrieveAvailableCalendars(Activity activity) {

		GoogleAccountManager googleAccountManager = new GoogleAccountManager(
				activity);

		List<CalendarInfo> calendarList = new ArrayList<CalendarInfo>();
		
		String selection = "((" + Calendars.VISIBLE + " = 1) AND (" 
                + Calendars.ACCOUNT_TYPE + " = com.google))";
		
		String[] projection = 
			      new String[]{
			            Calendars._ID, 
			            Calendars.NAME, 
			            Calendars.ACCOUNT_NAME, 
			            Calendars.ACCOUNT_TYPE,
			            Calendars.CALENDAR_ACCESS_LEVEL};
			Cursor calCursor = 
			      activity.getContentResolver().
			            query(Calendars.CONTENT_URI, 
			                  projection, 
			                  null, 
			                  null, 
			                  Calendars._ID + " ASC");
			if (calCursor.moveToFirst()) {
			   do {
			      long id = calCursor.getLong(0);
			      String displayName = calCursor.getString(1);
			      String accountType = calCursor.getString(3);
			      int accessLevel = calCursor.getInt(4);
			      if(isValidCalendar(displayName, accessLevel)){
			    	  CalendarInfo calInfo = new CalendarInfo(id, displayName, accountType);
			    	  calendarList.add(calInfo);
			      }
			   } while (calCursor.moveToNext());
			}
			
			
			return calendarList;
//		
//		// Run query
//		Cursor cur = null;
//		ContentResolver cr = activity.getContentResolver();
//		Uri uri = Calendars.CONTENT_URI;   
//		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
//		                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
//		                        + Calendars.OWNER_ACCOUNT + " = ?))";
//		String[] selectionArgs = new String[] {accounts[0].name, accounts[0].type, accounts[0].name}; 
//		// Submit the query and get a Cursor object back. 
//		cur = cr.query(uri, EVENT_PROJECTION, selection, null, null);
//		
//		
//		// Use the cursor to step through the returned records
//		while (cur.moveToNext()) {
//		    long calID = 0;
//		    String displayName = null;
//		    String accountName = null;
//		    String ownerName = null;
//		      
//		    // Get the field values
//		    calID = cur.getLong(PROJECTION_ID_INDEX);
//		    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
//		    accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
//		    ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
//		              
//		    calendarList.add(displayName);
//
//		}
		
		
	}

	private static boolean isValidCalendar(String displayName, int accessLevel) {
		return accessLevel == Calendars.CAL_ACCESS_CONTRIBUTOR || accessLevel == Calendars.CAL_ACCESS_EDITOR || accessLevel == Calendars.CAL_ACCESS_OWNER && displayName != null;
	}

}
