package com.nyelito.remindmeapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class GetUpcomingMoviesTaskOld extends AsyncTask<Context, Void, List<Movie>> {
	
	private ProgressDialog progressDialog;
	private Context context;
	    //declare other objects as per your need
	    @Override
	    protected void onPreExecute()
	    {
	        progressDialog= ProgressDialog.show(context, "Progress Dialog Title Text","Process Description Text", true);

	        //do initialization of required objects objects here                
	    };      
	    
	    protected void onPostExecute()
	    {
	        progressDialog.dismiss();
	    };

		@Override
		protected List<Movie> doInBackground(Context... params) {
			
			context = params[0];
	
			int pageNum = 1;
			int totalPages = 1000;
			String page = "&page=";
			List<Movie> movieList = new ArrayList<Movie>(100);
			
			String todaysDate = "2014-08-28";
			String endDate = "2014-12-31";
			
			while (pageNum <= totalPages) {
				
				

				String theUrl = "https://api.themoviedb.org/3/discover/movie?api_key=9912c46769eb04ae178bf1a88457890e&release_date.gte="
						+ todaysDate + "&release_date.lte=" + endDate
						+ page + pageNum;
				String outPut = "";
				
				HttpClient httpclient = new DefaultHttpClient();
			    HttpResponse response;
				try {
					response = httpclient.execute(new HttpGet(theUrl));
			    StatusLine statusLine = response.getStatusLine();
			    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
			        ByteArrayOutputStream out = new ByteArrayOutputStream();
			        response.getEntity().writeTo(out);
			        out.close();
			        outPut = out.toString();
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
				}


				String title;
				String releaseDate;
				JSONObject obj;
				try {
					obj = new JSONObject(outPut);
				
				if(totalPages == 1000){
					totalPages = Integer.parseInt(obj.getString("total_pages"));
				}

				System.out.println("total pages is: " + totalPages);
				JSONArray array = new JSONArray(obj.getString("results"));

				for (int i = 0; i < array.length(); i++) {
					JSONObject row = array.getJSONObject(i);
					title = row.getString("title");
					releaseDate = row.getString("release_date");
					
					
					movieList.add(new Movie(title, formatDate(releaseDate)));
				}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				pageNum++;
			}
			return movieList;

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
	
	private String getTodaysDate(){
		return null;
		
	}
	
	private String getEndDate(){
		return null;
	}	    
	}

	
	


