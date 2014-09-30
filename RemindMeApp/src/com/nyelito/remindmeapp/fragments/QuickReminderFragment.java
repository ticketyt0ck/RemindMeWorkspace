package com.nyelito.remindmeapp.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.nyelito.remindmeapp.CalendarInfo;
import com.nyelito.remindmeapp.Constants;
import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.Release;
import com.nyelito.remindmeapp.Release.ReleaseType;
import com.nyelito.remindmeapp.SettingsActivity;
import com.nyelito.remindmeapp.tasks.GetUpcomingMoviesTask;
import com.nyelito.remindmeapp.tasks.ReadTVListFromFileTask;
 
public class QuickReminderFragment extends Fragment implements CalendarFragment{
	
	private static String MOVIE_JSON_FILENAME = Constants.MOVIE_JSON_FILENAME;
	private static String REMOVE_ADS_FILENAME = "RemoveAds";
	private static String MOVIE_LIST_LOADED_KEY = "movieListLoaded";
	private static String MOVIE_LIST_KEY = "movieListKey";
	private AutoCompleteTextView actv;
	private ArrayList<Release> releaseList;
	private boolean movieListLoaded;
	OnMovieListLoadedListener mCallback;
	private boolean shouldShowAds;

	
	public void setShouldShowAds(boolean toSet){
		shouldShowAds = toSet;
	}
	
	public void readAndLoadList(ReleaseType releaseType){
		
		switch(releaseType){
			case MOVIE:
				new ReadMovieListFromFileTask().execute();
				break;
			case TV:
				new ReadTVListFromFileTask(getActivity()).execute();
				break;
				
		}
	}
	

	public interface OnMovieListLoadedListener{
		public void onMovieListLoaded(List<Release> movieList);
	}
		
	  @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        
	        // This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (OnMovieListLoadedListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnMovieListLoadedListener");
	        }
	    }
	
	
	
	public ArrayList<Release>	getMovieList(){
		return releaseList;
	}
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(MOVIE_LIST_LOADED_KEY, movieListLoaded);
		outState.putParcelableArrayList(MOVIE_LIST_KEY, (ArrayList<? extends Parcelable>) releaseList);
	}
	
	@Override
	public void onActivityCreated(Bundle bundle) {
	    super.onActivityCreated(bundle);
		if (shouldShowAds) {
			AdView mAdView = (AdView) getView().findViewById(R.id.adView);
			// AdRequest adRequest = new
			// AdRequest.Builder().addTestDevice("93E94C72701A4AB5BBFAC83ED1171B77").build();
			AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
			adRequestBuilder.addKeyword("movies");
			adRequestBuilder.addKeyword("theater");
			adRequestBuilder.addKeyword("trailer");
			adRequestBuilder.addKeyword("film");
			adRequestBuilder.addKeyword("cinema");
			AdRequest adRequest = adRequestBuilder.build();

			mAdView.loadAd(adRequest);
		}
	}
	
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_quick_reminder, container, false);
		
		actv = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTextView1);
		
		actv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				hideKeyboard();
				
			}
		});

		if(savedInstanceState != null && actv != null){
			movieListLoaded = savedInstanceState.getBoolean(MOVIE_LIST_LOADED_KEY, false);
//			releaseList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
//			updateAutoCompleteText(releaseList);
		}
		
		
		
		File movieListFile = new File(getActivity().getFilesDir(), MOVIE_JSON_FILENAME);

		ConnectivityManager cm =
		        (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();

		
		// only update the list when there are no files present
		if((!movieListFile.exists() && isConnected)|| (listNeedsUpdate() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)){
			new GetUpcomingMoviesTask(getActivity()).execute();
		} else {
			
			if(!isConnected){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				
				builder.setMessage(R.string.no_internet_message);
				

				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			// Read the files and update list
			if (shouldReadMovieList()) {
				new ReadMovieListFromFileTask().execute();
				movieListLoaded = true;
			}
		}
		
//		new GetUpcomingTVTask(getActivity()).execute();
		

		return rootView;
    }
    
    
    private boolean listNeedsUpdate(){
    	File movieListFile = new File(getActivity().getFilesDir(), MOVIE_JSON_FILENAME);
    	Date lastModifiedDate = new Date(movieListFile.lastModified());
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	calendar.add(Calendar.DAY_OF_YEAR, -7);
    	Date weekAgoDate = calendar.getTime();
    	
    	if(lastModifiedDate.before(weekAgoDate)){
    		return true;
    	}else{
    		return false;
    	}
    	
    }
   
    
    
    private boolean shouldReadMovieList(){
    	return !movieListLoaded;
    }
    
    public void setMovieListLoaded(boolean toSet){
    	this.movieListLoaded = toSet;
    }
    
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(
				getActivity().getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public void remindMeButtonClick(View view){

		hideKeyboard();

		view.requestFocus();
		
		String movieQuery = actv.getText().toString();
		
		Release foundRelease = null;
		for (Release m : this.releaseList) {
			if(m.getTitle().equalsIgnoreCase(movieQuery)){
				foundRelease = m;
			}
		}

		if (foundRelease != null) {

			QuickReminderFragment.setQuickReminder(foundRelease, getActivity());
			actv.setText("");
			
			
		} else if(movieQuery != null && !movieQuery.isEmpty()){
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			builder.setMessage(R.string.tryRemindAlertMessage);
			
			// Add the buttons
//			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) {
//			        	   // Set something to tell it to find this movie periodically
//			           }
//			       });
//			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) {
//			               // User cancelled the dialog
//			           }
//			       });

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
			
		} else {
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					"Please enter a movie title", Toast.LENGTH_SHORT);
			toast.show();

		}

	}
	
	public static void setQuickReminder(Release m, Context context){
		
		int reminderDays;
		int calendarID;
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		reminderDays = sharedPref
				.getInt(SettingsActivity.REMINDERDAYS_STRING, 7);
		
		calendarID = Integer.parseInt(sharedPref.getString(SettingsActivity.CALENDARID_STRING, "1"));
		
		
		
		// Put an event on their calender with reminder automatically
		Calendar beginTime = Calendar.getInstance();
		beginTime.setTime(m.getReleaseDate());
		beginTime.add(Calendar.HOUR, 20);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(m.getReleaseDate());
		endTime.add(Calendar.HOUR, 22);
		
		TimeZone timeZone = TimeZone.getDefault();
		
		
	    ContentResolver contentResolver = context.getContentResolver();
	        
	    ContentValues calEvent = new ContentValues();
	    calEvent.put(CalendarContract.Events.CALENDAR_ID, calendarID); // XXX pick)
	    calEvent.put(CalendarContract.Events.TITLE, "Reminder to go see: " + m.getTitle());
	    calEvent.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
	    calEvent.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis() + 5000);
	    calEvent.put(CalendarContract.Events.DESCRIPTION,
				"Go see the movie " + m.getTitle());
	    calEvent.put(Events.ALL_DAY, false);
	    calEvent.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
	    calEvent.put(Events.EVENT_TIMEZONE, timeZone.getID());
	    Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, calEvent);
	        
	    // The returned Uri contains the content-retriever URI for 
	    // the newly-inserted event, including its id
	    int eventID = Integer.parseInt(uri.getLastPathSegment());
	    
	    ContentResolver cr = context.getContentResolver();
	    ContentValues values = new ContentValues();
	    values.put(Reminders.MINUTES, Constants.MINUTES_IN_DAY * reminderDays);
	    values.put(Reminders.EVENT_ID, eventID);
	    values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
	    Uri reminderURI = cr.insert(Reminders.CONTENT_URI, values);
		
		
	    Toast.makeText(context, "Created Calendar Event for: " + m.getTitle() + " on " + beginTime.getTime().toString(),
	    		Toast.LENGTH_LONG).show();
		
	}
	
	public void setReleaseList(ArrayList<Release> movieList){
		mCallback.onMovieListLoaded(movieList);
		this.releaseList = movieList;
	}
	
	public class ReadMovieListFromFileTask extends AsyncTask<Void, Void, ArrayList<Release>> {

		private ProgressDialog progressDialog;
		private AutoCompleteTextView actv;


		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("Reading Movie List...");
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();

		};

		@Override
		protected void onPostExecute(ArrayList<Release> movieList) {
//			Collections.sort(movieList, new Movie.MovieComparator());
			mCallback.onMovieListLoaded(movieList);
			setReleaseList(movieList);
			progressDialog.dismiss();
			updateAutoCompleteText(movieList);
		};

		@Override
		protected ArrayList<Release> doInBackground(Void... params) {

			return readMovieListFromFile();
		}
		
		
		private ArrayList<Release> readMovieListFromFile(){

			ArrayList<Release> movieList = new ArrayList<Release>(500);

			FileInputStream fis;
			String output = null;
			try {
				fis = getActivity().getApplicationContext().openFileInput(
						Constants.MOVIE_JSON_FILENAME);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader bufferedReader = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					sb.append(line);
				}

				output = sb.toString();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Gson gson = new Gson();
			Movie[] movieArray = gson.fromJson(output, Movie[].class);
			
			movieList = new ArrayList<Release>(Arrays.asList(movieArray));


			return movieList;
		}
		
	
	}
	
	private void updateAutoCompleteText(List<Release> movieList) {
		List<String> titleList = new ArrayList<String>(100);
		for(Release pair : movieList){
			titleList.add(pair.getTitle());
		}
		
		ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line, titleList);

				   
		actv.setAdapter(autocompletetextAdapter);
	}

	@Override
	public void handleCalendarList(List<CalendarInfo> calendarList) {
		if(calendarList.isEmpty()){
AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.no_calendar_message);
			
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   // Set something to tell it to find this movie periodically
			           }
			       });
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
	}

}