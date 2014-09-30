package com.nyelito.remindmeapp.fragments;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nyelito.remindmeapp.CalendarInfo;
import com.nyelito.remindmeapp.Constants;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.SettingsActivity;
import com.nyelito.remindmeapp.tasks.AvailableCalendarRetrieverTask;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, CalendarFragment{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        new AvailableCalendarRetrieverTask(this).execute();
        
    }
    
    
    
    
	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		sharedPref.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onResume();
		// Set up a listener whenever a key changes
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
   			String key) {
       	
   		if (key.equals(SettingsActivity.LOOKAHEAD_STRING)) {

   			int monthsInt = sharedPreferences.getInt(
   					SettingsActivity.LOOKAHEAD_STRING, 6);
   			String summary = null;
   			summary = getResources().getString(R.string.lookAheadMonths_summary, monthsInt);
   			
   			Preference connectionPref = findPreference(getString(R.string.lookAheadMonths_string));
   			connectionPref.setSummary(summary);
   			
			// if the lookahead months have changed, delete the file so that we
			// will reload it when we come back in
			File movieListFile = new File(getActivity().getFilesDir(),
					Constants.MOVIE_JSON_FILENAME);

			ConnectivityManager cm = (ConnectivityManager) getActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null
					&& activeNetwork.isConnectedOrConnecting();

			if (movieListFile.exists() && isConnected) {
				movieListFile.delete();
			}

   		}else if(key.equals(SettingsActivity.REMINDERDAYS_STRING)){
   			 int daysInt = sharedPreferences.getInt(SettingsActivity.REMINDERDAYS_STRING, 7);
   		        String summary = null;
   		        if(daysInt == 1){
   		        	summary = getResources().getString(R.string.reminderDays_summary_one, daysInt );
   		        }else{
   		        	summary = getResources().getString(R.string.reminderDays_summary, daysInt );
   		        }
   		        
   		        Preference connectionPref = findPreference(key);
   		        connectionPref.setSummary(summary);
   		}

   	}
    
    
    private static void setListPreferenceData(ListPreference listPreference, CharSequence[] calendarList, CharSequence[] idList) {
        listPreference.setEntries(calendarList);
        listPreference.setDefaultValue(idList[0]);
        listPreference.setEntryValues(idList);
    }

	@Override
	public void handleCalendarList(List<CalendarInfo> calendarList) {

		final ListPreference listPreference = (ListPreference) findPreference("calendarPicker");


		int length = calendarList.size();
		if(length > 0){

			CharSequence[] calendarNameList = new CharSequence[length];
			CharSequence[] idList = new CharSequence[length];
		
			int i = 0;
			for (CalendarInfo info : calendarList) {
				calendarNameList[i] = info.getCalendarName();
				idList[i] = String.valueOf(info.getId());
				i++;
			}
			
			setListPreferenceData(listPreference, calendarNameList, idList);
			
		// You have no calendars, so don't show the preference
		}else{
			listPreference.setEnabled(false);
			listPreference.setSummary(R.string.no_calendar_message);
		}

		
	}
}