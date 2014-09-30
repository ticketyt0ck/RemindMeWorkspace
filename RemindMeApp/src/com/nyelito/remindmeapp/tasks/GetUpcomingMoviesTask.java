package com.nyelito.remindmeapp.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.nyelito.remindmeapp.Constants;
import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.SettingsActivity;

public class GetUpcomingMoviesTask extends AsyncTask<Void, Void, Boolean> {
	
	private int monthLookAhead;

	private static final String URL_DATE_FORMAT = "yyyy-MM-dd";
	
	private Activity currActivity;
	
	private ProgressDialog progressDialog;

	public GetUpcomingMoviesTask(Activity activity) {

		currActivity = activity;
	}

	@Override
	protected void onPreExecute() {
		
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(currActivity);
		monthLookAhead = sharedPref
				.getInt(SettingsActivity.LOOKAHEAD_STRING, 6);
		progressDialog = new ProgressDialog(currActivity);

		progressDialog.setTitle("Downloading Movie Data...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.show();

	};

	@Override
	protected void onPostExecute(Boolean isConnectedToNetwork) {
		
		if(!isConnectedToNetwork){
			AlertDialog.Builder builder = new AlertDialog.Builder(currActivity);
			
			builder.setMessage(R.string.no_internet_message);
			

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		progressDialog.dismiss();

		 new ReadMovieListFromFileTask(currActivity).execute();
	};

	@Override
	protected Boolean doInBackground(Void... params) {
		ConnectivityManager cm =
		        (ConnectivityManager)currActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		if(!isConnected){
			return false;
		} else {

			// check if the file exists, if so don't rewrite it
			File dir = currActivity.getFilesDir();
			File file = new File(dir, Constants.MOVIE_JSON_FILENAME);

			// if the file exists, delete it so we only get the movies we ask
			// for
			if (file.exists()) {
				file.delete();
			}

			List<Movie> movieList = retrieveListFromAPI();

			serializeListIntoFile(movieList);
			
			return true;

		}

	}

	private void serializeListIntoFile(List<Movie> movieList) {

		Gson gson = new Gson();
		String json = gson.toJson(movieList);

		saveJsonToFile(json);
	}

	private List<Movie> retrieveListFromAPI() {
		
		String configurationURL = "https://api.themoviedb.org/3/configuration?api_key=9912c46769eb04ae178bf1a88457890e";

		HttpClient configHttpclient = new DefaultHttpClient();
		HttpResponse configResponse;
		String configOutput = "";
		String baseURL = "";
		String posterSize = "w92";
		try {
			configResponse = configHttpclient.execute(new HttpGet(configurationURL));
			StatusLine statusLine = configResponse.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				configResponse.getEntity().writeTo(out);
				out.close();
				configOutput = out.toString();
				
				JSONObject obj = new JSONObject(configOutput);
				
				JSONObject imagesObject = obj.getJSONObject("images");
		
				baseURL = imagesObject.getString("secure_base_url");
			}else {
				// Closes the connection.
				configResponse.getEntity().getContent().close();
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
		
		String imageUrlPrefix = baseURL + posterSize;
				
		int pageNum = 1;
		int totalPages = 1000;
		String page = "&page=";

		String todaysDateString = getTodaysDate();
		String endDate = getLookAheadDate();
		Date todaysDate = new Date();

		List<Movie> movieList = new ArrayList<Movie>(1000);

		while (pageNum <= totalPages) {

			String discoverURL = "https://api.themoviedb.org/3/discover/movie?api_key=9912c46769eb04ae178bf1a88457890e&release_date.gte="
					+ todaysDateString
					+ "&release_date.lte="
					+ endDate
					+ page
					+ pageNum;
			String output = "";

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			try {
				response = httpclient.execute(new HttpGet(discoverURL));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					output = out.toString();

					if (totalPages == 1000) {
						JSONObject obj = new JSONObject(output);
						totalPages = obj.getInt("total_pages");
						progressDialog.setMax(totalPages);
					}

					// read json into objects
					String title;
					JSONObject obj;
					String releaseDate;
					String posterURL;
					try {
						obj = new JSONObject(output);

						JSONArray array = new JSONArray(
								obj.getString("results"));

						for (int j = 0; j < array.length(); j++) {
							JSONObject row = array.getJSONObject(j);
							title = row.getString("title");
							releaseDate = row.getString("release_date");
							posterURL = row.getString("poster_path");

							Date theDate = formatDate(releaseDate);
							// make sure the release date isn't before today before we add the movie to the list
							if(!theDate.before(todaysDate)){
								if(posterURL.contains("null")){
									movieList.add(new Movie(title, theDate, null));
								}else{
									movieList.add(new Movie(title, theDate, imageUrlPrefix+posterURL));
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					// Closes the connection.
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

	private Date formatDate(String releaseDate) {
		Date formattedDate = new Date();
		try {
			formattedDate = new SimpleDateFormat("yyyy-MM-dd")
					.parse(releaseDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return formattedDate;
	}

	private void saveJsonToFile(String toSave) {

		// check if the file exists
		File dir = currActivity.getFilesDir();
		String filename = Constants.MOVIE_JSON_FILENAME;
		File file = new File(dir, filename);

		if (file.exists()) {
			file.delete();
		}

		FileOutputStream outputStream;
		try {
			outputStream = currActivity.openFileOutput(filename,
					Context.MODE_APPEND);
			outputStream.write(toSave.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getTodaysDate() {

		DateFormat dateFormat = new SimpleDateFormat(URL_DATE_FORMAT);
		Date date = new Date();
		return dateFormat.format(date);

	}

	private String getLookAheadDate() {
		DateFormat dateFormat = new SimpleDateFormat(URL_DATE_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, monthLookAhead);
		return dateFormat.format(cal.getTime());
	}

				}

	