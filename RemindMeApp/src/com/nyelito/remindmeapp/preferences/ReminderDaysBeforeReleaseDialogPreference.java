package com.nyelito.remindmeapp.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.SettingsActivity;


public class ReminderDaysBeforeReleaseDialogPreference extends DialogPreference {
	
	private static int DEFAULT_VALUE = 0;
	private NumberPicker reminderDaysPicker;
	private int mCurrentValue;
	
    public ReminderDaysBeforeReleaseDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setDialogLayoutResource(R.layout.reminder_days_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        
        int daysInt = sharedPref.getInt(SettingsActivity.REMINDERDAYS_STRING, 7);
        String summary = null;
        if(daysInt == 1){
        	summary = context.getResources().getString(R.string.reminderDays_summary_one, daysInt );
        }else{
        	summary = context.getResources().getString(R.string.reminderDays_summary, daysInt );
        }
        
        setSummary(summary);
        
        setDialogIcon(null);
    }
	
	
	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		setPersistent(true);
	}

	 @Override
     public void onBindDialogView(View view){
		 reminderDaysPicker = (NumberPicker)view.findViewById(R.id.reminderDaysNumberPicker);
		 reminderDaysPicker.setMinValue(1);
		 reminderDaysPicker.setMaxValue(30);
		 reminderDaysPicker.setValue(getPersistedInt(DEFAULT_VALUE));
	 }
	 
	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	     // When the user selects "OK", persist the new value
	     if (positiveResult) {
	         persistInt(reminderDaysPicker.getValue());
	     }
	 }
	 
	 @Override
	 protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
	     if (restorePersistedValue) {
	         // Restore existing state
	         mCurrentValue = this.getPersistedInt(DEFAULT_VALUE);
	     } else {
	         // Set default state from the XML attribute
	         mCurrentValue = (Integer) defaultValue;
	         persistInt(mCurrentValue);
	     }
	 }

	 @Override
	 protected Object onGetDefaultValue(TypedArray a, int index) {
	     return a.getInteger(index, DEFAULT_VALUE);
	 }
}
