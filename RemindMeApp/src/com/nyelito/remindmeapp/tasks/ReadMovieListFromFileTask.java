package com.nyelito.remindmeapp.tasks;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.nyelito.remindmeapp.Constants;
import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.Release;
import com.nyelito.remindmeapp.TabbedActivity;
import com.nyelito.remindmeapp.fragments.QuickReminderFragment;

public class ReadMovieListFromFileTask extends AsyncTask<Void, Void, ArrayList<Release>> {

	private ProgressDialog progressDialog;
	private Activity currActivity;
	private AutoCompleteTextView actv;
	private QuickReminderFragment quickFragment;

	public ReadMovieListFromFileTask(Activity activity) {
		currActivity = activity;
		TabbedActivity tabActivity = (TabbedActivity) currActivity;
		
		quickFragment = tabActivity.getQuickReminderFragment();
		progressDialog = new ProgressDialog(quickFragment.getActivity());
		progressDialog.setMessage("Reading Movie Data From File...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	protected void onPreExecute() {

		progressDialog.show();

	};

	@Override
	protected void onPostExecute(ArrayList<Release> movieList) {
//		Collections.sort(movieList, new Movie.MovieComparator());
		progressDialog.dismiss();
		quickFragment.setReleaseList(movieList);
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
			fis = currActivity.getApplicationContext().openFileInput(
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
	
	private void updateAutoCompleteText(List<Release> movieList) {
		List<String> titleList = new ArrayList<String>(100);
		for(Release pair : movieList){
			titleList.add(pair.getTitle());
		}
		
		ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<String>(
                currActivity,
                android.R.layout.simple_dropdown_item_1line, titleList);

				   actv = (AutoCompleteTextView) currActivity.findViewById(R.id.autoCompleteTextView1);
				   
				   actv.setAdapter(autocompletetextAdapter);
	}
	
	
}