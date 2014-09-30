package com.nyelito.remindmeapp.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.nyelito.remindmeapp.Constants;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.TVShow;

public class GetUpcomingTVTask extends AsyncTask<Void, Void, Boolean>
{


	private static final String URL_DATE_FORMAT = "yyyy-MM-dd";
	
	private Activity currActivity;
	
	private ProgressDialog progressDialog;

	public GetUpcomingTVTask(Activity activity) {

		currActivity = activity;
	}

	@Override
	protected void onPreExecute() {
		
		progressDialog = new ProgressDialog(currActivity);

		progressDialog.setTitle("Downloading TV Data...");
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

		 new ReadTVListFromFileTask(currActivity).execute();
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
			File file = new File(dir, Constants.TV_JSON_FILENAME);

			// if the file exists, delete it so we only get the movies we ask
			// for
			if (file.exists()) {
				file.delete();
			}

			List<TVShow> onAirTVList = retrieveTVListFromAPI();

			serializeListIntoFile(onAirTVList);
			
			return true;

		}

	}

	private void serializeListIntoFile(List<TVShow> onAirTVList) {

		Gson gson = new Gson();
		String json = gson.toJson(onAirTVList);

		saveJsonToFile(json);
	}

	private List<TVShow> retrieveTVListFromAPI() {

		int pageNum = 1;
		int totalPages = 1000;
		String page = "&page=";
		List<TVShow> onAirTVList = new ArrayList<TVShow>(200);

		while (pageNum <= totalPages) {
		
		String configurationURL = "https://api.themoviedb.org/3/tv/on_the_air?api_key=9912c46769eb04ae178bf1a88457890e"
				+ page
				+ pageNum;

		HttpClient configHttpclient = new DefaultHttpClient();
		HttpResponse configResponse;
		String onAirTVOutput = "";
		try {
			configResponse = configHttpclient.execute(new HttpGet(configurationURL));
			StatusLine statusLine = configResponse.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				configResponse.getEntity().writeTo(out);
				out.close();
				onAirTVOutput = out.toString();
				
				if (totalPages == 1000) {
					JSONObject obj = new JSONObject(onAirTVOutput);
					totalPages = obj.getInt("total_pages");
					progressDialog.setMax(totalPages);
				}
				
				JSONObject obj = new JSONObject(onAirTVOutput);
				
				
				JSONArray array = new JSONArray(
						obj.getString("results"));
				
				String title;
				int id;
				
				for (int j = 0; j < array.length(); j++) {
					JSONObject row = array.getJSONObject(j);
					title = row.getString("name");
					id = row.getInt("id");

					TVShow currShow = new TVShow(title, id+"");
					onAirTVList.add(currShow);
				}
				
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
		
		progressDialog.setProgress(pageNum);
		pageNum++;

		}
		return onAirTVList;
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
		String filename = Constants.TV_JSON_FILENAME;
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

}
