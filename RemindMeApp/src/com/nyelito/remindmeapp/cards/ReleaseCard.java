package com.nyelito.remindmeapp.cards;

import it.gmariotti.cardslib.library.internal.Card;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.view.View;

import com.nyelito.remindmeapp.Release;
import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.SettingsActivity;
import com.nyelito.remindmeapp.fragments.QuickReminderFragment;

public class ReleaseCard extends Card {
	
	private Release release;
	private Context cardContext;
	public ReleaseCard(Context context, Release theRelease) {
		super(context);
		this.cardContext = context;
		this.release = theRelease;

		setOnClickListener(new Card.OnCardClickListener() {
			@Override
			public void onClick(Card card, View view) {

				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(view.getContext());

				int calendarID = Integer.parseInt(sharedPref.getString(
						SettingsActivity.CALENDARID_STRING, "1"));
				
				final Context context = view.getContext();
				final Release currRelease = release;

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						view.getContext());
				alertDialogBuilder
						.setMessage(R.string.reminder_type_choice_message);
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Quick",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								
								QuickReminderFragment.setQuickReminder(currRelease, context);

							}
						});

				alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Custom",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
                	
          			// Put an event on their calender with reminder automatically
          			Calendar beginTime = Calendar.getInstance();
          			beginTime.setTime(release.getReleaseDate());
          			beginTime.add(Calendar.HOUR, 20);
          			Calendar endTime = Calendar.getInstance();
          			endTime.setTime(release.getReleaseDate());
          			endTime.add(Calendar.HOUR, 22);
          			
          			TimeZone timeZone = TimeZone.getDefault();
                  	
          			Intent intent = new Intent(Intent.ACTION_INSERT)
      				.setData(Events.CONTENT_URI)
      				.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
      						beginTime.getTimeInMillis())
      				.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
      						endTime.getTimeInMillis())
      				.putExtra(Events.TITLE, "Reminder to go see: " + release.getTitle())
      				.putExtra(Events.DESCRIPTION,
      						"Go see the movie " + release.getTitle())
      				.putExtra(Events.ALL_DAY, true)
      				.putExtra(Events.HAS_ALARM, 1)
      				.putExtra(Reminders.MINUTES, 30)
      				.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);

          			cardContext.startActivity(intent);
                  }

                  });

                alertDialog.show();  //<-- See This!
            	
            }
            	
	});
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release theRelease) {
		this.release = theRelease;
	}

	
}
