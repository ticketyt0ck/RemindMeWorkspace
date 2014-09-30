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


public class LookAheadDialogPreference extends DialogPreference {
	
	private static int DEFAULT_VALUE = 0;
	private NumberPicker monthsPicker;
	private int mCurrentValue;
	
    public LookAheadDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setDialogLayoutResource(R.layout.lookahead_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        
        int monthsInt = sharedPref.getInt(SettingsActivity.LOOKAHEAD_STRING, 6);
        String summary = 
             context.getResources().getString(R.string.lookAheadMonths_summary, monthsInt );
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
		 monthsPicker = (NumberPicker)view.findViewById(R.id.lookAheadMonthsNumberPicker);
		 monthsPicker.setMinValue(3);
		 monthsPicker.setMaxValue(12);
		 monthsPicker.setValue(getPersistedInt(DEFAULT_VALUE));
	 }
	 
	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	     // When the user selects "OK", persist the new value
	     if (positiveResult) {
	         persistInt(monthsPicker.getValue());
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
