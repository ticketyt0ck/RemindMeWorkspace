package com.nyelito.remindmeapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.gson.Gson;


@SuppressLint("SimpleDateFormat")
public class MainActivity extends ActionBarActivity {

	
	private static String MOVIE_JSON_FILENAME = "MovieListJSON";
	private AutoCompleteTextView actv;
	private List<Movie> movieList;
	

	public void setMovieList(List<Movie> movieList) {
		this.movieList = movieList;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		
		actv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				hideKeyboard();
				
			}
		});

		
		File movieListFile = new File(this.getFilesDir(), MOVIE_JSON_FILENAME);
		// only update the list when there are no files present
		if(!movieListFile.exists()){
			new GetUpcomingMoviesTask().execute();
		}else{
			// Read the files and update list
			new ReadMovieListFromFileTask().execute();
			
		}
		
	}

	private void updateAutoCompleteText(List<Movie> movieList) {
		List<String> titleList = new ArrayList<String>(100);
		for(Movie pair : movieList){
			titleList.add(pair.getTitle());
		}
		
		ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line, titleList);

				   actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
				   
				   actv.setAdapter(autocompletetextAdapter);
	}
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public void remindMeButtonClick(View view){

		hideKeyboard();

		view.requestFocus();
		
		String movieQuery = actv.getText().toString();
		
		Movie foundMovie = null;
		for (Movie m : movieList) {
			if(m.getTitle().equalsIgnoreCase(movieQuery)){
				foundMovie = m;
			}
		}

		if (foundMovie != null) {

//			Intent intent = new Intent(Intent.ACTION_INSERT)
//					.setData(Events.CONTENT_URI)
//					.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
//							beginTime.getTimeInMillis())
//					.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
//							endTime.getTimeInMillis())
//					.putExtra(Events.TITLE, foundMovie.getTitle())
//					.putExtra(Events.DESCRIPTION,
//							"Go see the movie " + foundMovie.getTitle())
//					.putExtra(Events.ALL_DAY, true)
//					.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);

//			startActivity(intent);
			
			
			// Put an event on their calender with reminder automatically
			Calendar beginTime = Calendar.getInstance();
			beginTime.setTime(foundMovie.getReleaseDate());
			Calendar endTime = Calendar.getInstance();
			endTime.setTime(foundMovie.getReleaseDate());
			
			TimeZone timeZone = TimeZone.getDefault();
			
			
		    ContentResolver contentResolver = this.getApplicationContext().getContentResolver();
		        
		    ContentValues calEvent = new ContentValues();
		    calEvent.put(CalendarContract.Events.CALENDAR_ID, 1); // XXX pick)
		    calEvent.put(CalendarContract.Events.TITLE, "Reminder to go see: " + foundMovie.getTitle());
		    calEvent.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
		    calEvent.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis() + 5000);
		    calEvent.put(CalendarContract.Events.DESCRIPTION,
					"Go see the movie " + foundMovie.getTitle());
		    calEvent.put(Events.ALL_DAY, true);
		    calEvent.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
		    calEvent.put(Events.EVENT_TIMEZONE, timeZone.getID());
		    Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, calEvent);
		        
		    // The returned Uri contains the content-retriever URI for 
		    // the newly-inserted event, including its id
		    int eventID = Integer.parseInt(uri.getLastPathSegment());
		    
		    
		    ContentResolver cr = getContentResolver();
		    ContentValues values = new ContentValues();
		    values.put(Reminders.MINUTES, 15);
		    values.put(Reminders.EVENT_ID, eventID);
		    values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		    Uri reminderURI = cr.insert(Reminders.CONTENT_URI, values);
			
			
		    Toast.makeText(this.getApplicationContext(), "Created Calendar Event for: " + foundMovie.getTitle() + " on " + foundMovie.getReleaseDate().toString(),
		    		Toast.LENGTH_LONG).show();
			
			
			
			
		} else if(movieQuery != null && !movieQuery.isEmpty()){
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setMessage(R.string.tryRemindAlertMessage);
			
			// Add the buttons
			builder.setPositiveButton(R.string.tryToRemind_Yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   // Set something to tell it to find this movie periodically
			           }
			       });
			builder.setNegativeButton(R.string.tryToRemind_No, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
			
		} else{
			Toast toast = Toast.makeText(this.getApplicationContext(),
					"Please enter a movie title", Toast.LENGTH_SHORT);
			toast.show();
			
		}
		
		
		
		

	}
	

	
	public class GetUpcomingMoviesTask extends AsyncTask<Void, Void, Void> {
		
		private static final int MONTH_LOOK_AHEAD = 4;

		private static final String URL_DATE_FORMAT = "yyyy-MM-dd";

		
		
		ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

		
		    @Override
		    protected void onPreExecute()
		    {	
		    	
		    	progressDialog.setTitle("Downloading Movie Data...");
		    	progressDialog.setCanceledOnTouchOutside(false);
		    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		    	progressDialog.setProgress(0);
		    	progressDialog.show();
		    	

		    };      
		    @Override
		    protected void onPostExecute(Void temp)
		    {
		    	progressDialog.dismiss();

		    	new ReadMovieListFromFileTask().execute();
		    };

		    @Override
		protected Void doInBackground(Void... params) {

			// check if the file exists, if so don't rewrite it
			File dir = getFilesDir();
			File file = new File(dir, MOVIE_JSON_FILENAME);

			// if file exists or it's time to refresh them
			// if (!file.exists()) {
			List<Movie> movieList = retrieveListFromAPI();
			// }
			
			serializeListIntoFile(movieList);

			return null;

		}
		    
		    
		    private void serializeListIntoFile(List<Movie> movieList){
		    	
		    	Gson gson = new Gson();
		    	String json = gson.toJson(movieList);
		    	
		    	saveJsonToFile(json);
		    }
		    
		    
		    
			private List<Movie> retrieveListFromAPI() {
				int pageNum = 1;
				int totalPages = 1000;
				String page = "&page=";
				
				String todaysDate = getTodaysDate();
				String endDate = getLookAheadDate();
				
				List<Movie> movieList = new ArrayList<Movie>(1000);
				
				while (pageNum <= totalPages) {
					
					

					String theUrl = "https://api.themoviedb.org/3/discover/movie?api_key=9912c46769eb04ae178bf1a88457890e&release_date.gte="
							+ todaysDate + "&release_date.lte=" + endDate
							+ page + pageNum;
					String output = "";
					
					HttpClient httpclient = new DefaultHttpClient();
				    HttpResponse response;
					try {
						response = httpclient.execute(new HttpGet(theUrl));
				    StatusLine statusLine = response.getStatusLine();
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				        ByteArrayOutputStream out = new ByteArrayOutputStream();
				        response.getEntity().writeTo(out);
				        out.close();
				        output = out.toString();
				        
				        if(totalPages == 1000){
				        JSONObject obj = new JSONObject(output);
				        totalPages = obj.getInt("total_pages");
				        progressDialog.setMax(totalPages);
				        }
				        
				        
				     // read json into objects
						String title;
						JSONObject obj;
						String releaseDate;
						try {
							obj = new JSONObject(output);

							JSONArray array = new JSONArray(obj.getString("results"));

							for (int j = 0; j < array.length(); j++) {
								JSONObject row = array.getJSONObject(j);
								title = row.getString("title");
								releaseDate = row.getString("release_date");

								movieList.add(new Movie(title, formatDate(releaseDate)));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
				        
				    } else{
				        //Closes the connection.
				        response.getEntity().getContent().close();
				        throw new IOException(statusLine.getReasonPhrase());
				    }
					} catch (ClientProtocolException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					progressDialog.setProgress(pageNum);
					pageNum++;
				}
				
				return movieList;
			}
		
		
		
		
		private String getTodaysDate(){
			
			DateFormat dateFormat = new SimpleDateFormat(URL_DATE_FORMAT);
			Date date = new Date();
			return dateFormat.format(date);
			
		}
		
		private String getLookAheadDate(){
			DateFormat dateFormat = new SimpleDateFormat(URL_DATE_FORMAT);
			Calendar cal = Calendar.getInstance(); 
			cal.add(Calendar.MONTH, MONTH_LOOK_AHEAD);
			return dateFormat.format(cal.getTime());
		}

				}
	
	
	
	private void saveJsonToFile(String toSave) {

		// check if the file exists
		File dir = this.getFilesDir();
		String filename = MOVIE_JSON_FILENAME;
		File file = new File(dir, filename);

		if (file.exists()) {
			file.delete();
		}
		

		FileOutputStream outputStream;
		try {
			outputStream = this.openFileOutput(filename, Context.MODE_APPEND);
			outputStream.write(toSave.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private Date formatDate(String releaseDate){
		Date formattedDate = null;
		try {
			 formattedDate = new SimpleDateFormat("yyyy-MM-dd")
			.parse(releaseDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return formattedDate;
	}
	
	public class ReadMovieListFromFileTask extends AsyncTask<Void, Void, List<Movie>> {

		ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPreExecute() {

			progressDialog.setMessage("Reading Movie Data From File...");
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();

		};

		@Override
		protected void onPostExecute(List<Movie> movieList) {
			progressDialog.dismiss();
			setMovieList(movieList);
			updateAutoCompleteText(movieList);
		};

		@Override
		protected List<Movie> doInBackground(Void... params) {

			return readMovieListFromFile();
		}
		
		
		private List<Movie> readMovieListFromFile(){

			List<Movie> movieList = new ArrayList<Movie>(500);

			FileInputStream fis;
			String output = null;
			try {
				fis = getApplicationContext().openFileInput(
						MOVIE_JSON_FILENAME);
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
			
			movieList = Arrays.asList(movieArray);

			return movieList;
		}
		
	}
	
	
}