package com.nyelito.remindmeapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.nyelito.remindmeapp.fragments.SettingsFragment;

public class SettingsActivity extends PreferenceActivity{
	
	public static final String LOOKAHEAD_STRING = "lookAheadMonths";
	public static final String REMINDERDAYS_STRING = "reminderDays";
	public static final String CALENDARID_STRING = "calendarPicker";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    

   
}