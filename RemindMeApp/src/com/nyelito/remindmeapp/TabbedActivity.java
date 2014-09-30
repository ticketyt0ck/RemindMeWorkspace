package com.nyelito.remindmeapp;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nyelito.remindmeapp.Release.ReleaseType;
import com.nyelito.remindmeapp.fragments.BrowseFragment;
import com.nyelito.remindmeapp.fragments.QuickReminderFragment;
import com.nyelito.remindmeapp.tasks.GetUpcomingMoviesTask;

public class TabbedActivity extends Activity implements QuickReminderFragment.OnMovieListLoadedListener, OnSharedPreferenceChangeListener{
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	private QuickReminderFragment quickFragment;
	private BrowseFragment browseFragment;
	private List<Release> movieList;
	private int currTabPosition;
	private ListView mDrawerList;
	private String [] tabTitles ={"Movies","Television", "DVDs"};
	private DrawerLayout mDrawerLayout;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	

	IInAppBillingService mService;

	ServiceConnection mServiceConn = new ServiceConnection() {
	   @Override
	   public void onServiceDisconnected(ComponentName name) {
	       mService = null;
	   }

	   @Override
	   public void onServiceConnected(ComponentName name, 
	      IBinder service) {
	       mService = IInAppBillingService.Stub.asInterface(service);
	       quickFragment.setShouldShowAds(shouldShowAds());
	       browseFragment.setShouldShowAds(shouldShowAds());
	   }
	};
	
	
	@Override
	public void onPause() {
		super.onResume();
		// Set up a listener whenever a key changes
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Set up a listener whenever a key changes
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		
		if(result == ConnectionResult.SUCCESS){
			//good
		}else{
			GooglePlayServicesUtil.getErrorDialog(result, this, 0);
		}
		
		
	}
	
	public QuickReminderFragment getQuickReminderFragment(){
		return quickFragment;
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabbed);
		
		Intent serviceIntent = new Intent(
				"com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
		
		
		currTabPosition = 0;
		
		
		quickFragment = new QuickReminderFragment();
		browseFragment = new BrowseFragment();
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		 mViewPager.setOnPageChangeListener(
		            new ViewPager.SimpleOnPageChangeListener() {
		                @Override
		                public void onPageSelected(int position) {
		                    // When swiping between pages, select the
		                    // corresponding tab.
		                    getActionBar().setSelectedNavigationItem(position);
		                }
		            });
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		 
		 // Create a tab listener that is called when the user changes tabs.
		    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
		        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		        	mViewPager.setCurrentItem(tab.getPosition());
		        	currTabPosition = tab.getPosition();
		        	invalidateOptionsMenu();
		        }

		        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		        	try{
		        		hideKeyboard();
		        	}catch(NullPointerException e){
		        		// uhhhh whoops
		        	}
		            // hide the given tab
		        }

		        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		            // probably ignore this event
		        }
		    };

		actionBar.addTab(actionBar.newTab().setText(R.string.quick_tab_title)
				.setTabListener(tabListener));
		

		actionBar.addTab(actionBar.newTab().setText(R.string.browse_tab_title)
				.setTabListener(tabListener));
		
		
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, tabTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerList.bringToFront();

        mDrawerLayout.requestLayout();


        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

	}
	
	 @Override
	    protected void onPostCreate(Bundle savedInstanceState) {
	        super.onPostCreate(savedInstanceState);
	        // Sync the toggle state after onRestoreInstanceState has occurred.
	        mDrawerToggle.syncState();
	    }

	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	        mDrawerToggle.onConfigurationChanged(newConfig);
	    }



	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}
	
	
	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
	    // Create a new fragment and specify the planet to show based on position
	    
		switch(position){
			case 0:
				quickFragment.readAndLoadList(ReleaseType.MOVIE);
				break;
			case 1:
				quickFragment.readAndLoadList(ReleaseType.TV);
				break;
		}

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    setTitle(tabTitles[position]);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
	    getActionBar().setTitle(title);
	}
	
	 @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (mService != null) {
	            unbindService(mServiceConn);
	        }   
	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(currTabPosition == 0){
			getMenuInflater().inflate(R.menu.tabbed_without_search, menu);
		}else if(currTabPosition == 1){
			getMenuInflater().inflate(R.menu.tabbed_with_search, menu);
			
			final SearchView editText = (SearchView) menu.findItem(R.id.action_search).getActionView();
	        editText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextSubmit(String query) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean onQueryTextChange(String newText) {
					  if (browseFragment.getAdapter() != null) {
		                	browseFragment.getAdapter().getFilter().filter(newText);
		                }
					return false;
				}
			});
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		// Handle presses on the action bar items
		 if (mDrawerToggle.onOptionsItemSelected(item)) {
	          return true;
	        }
		
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
	        	new GetUpcomingMoviesTask(this).execute();
	            return true;
	        case R.id.action_settings:
	        	Intent intent = new Intent(this, SettingsActivity.class);
			    startActivity(intent);
	            return true;
	        case R.id.action_remove_ads:
	        	removeAds();
	        	return true;
	        case R.id.action_about:
	        	openAboutDialog();
	        	return true;
	        case R.id.action_help:
	        	openHelpActivity();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
	}
	
	private void setNoAds(){
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.REMOVED_ADS, true);
		editor.apply();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	   if (requestCode == 1001) {           
	      int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
	      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
	      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
	        
	      if (resultCode == RESULT_OK) {
	         try {
	            JSONObject jo = new JSONObject(purchaseData);
	            String sku = jo.getString("productId");
	            Toast toast = Toast.makeText(getApplicationContext(),
						"Thank you! You may need to restart the app to see your purchase take effect.", Toast.LENGTH_LONG);
				toast.show();

				setNoAds();
	          }
	          catch (JSONException e) {
	        	  Toast toast = Toast.makeText(getApplicationContext(),
	  					"Remove ads didn't work bro", Toast.LENGTH_SHORT);
	  			toast.show();
	             e.printStackTrace();
	          }
	      }
	   }
	}
	
	private void openHelpActivity(){
		Intent intent = new Intent(this, HelpActivity.class);
	    startActivity(intent);
	}
	
	private void openAboutDialog(){
		
		 AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         final View dialogLayout = getLayoutInflater().inflate(R.layout.about_dialog_layout, null);
         TextView tv = (TextView) dialogLayout.findViewById(R.id.about_text_view);
         tv.setText(Html.fromHtml(getString(R.string.about_message_string)));
         tv.setMovementMethod(LinkMovementMethod.getInstance());
         dialogBuilder.setView(dialogLayout);
         String version = "";
		try {
			version = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         dialogBuilder.setTitle("Release Date Reminder Version " + version).create();
         dialogBuilder.show();
		
	}
	
	private void removeAds(){
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
        			   "remove_ads", "inapp", "test");
			
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			
			if(pendingIntent != null){
				startIntentSenderForResult(pendingIntent.getIntentSender(),
						   1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
						   Integer.valueOf(0));
			}else{
				
				setNoAds();
				Toast toast = Toast.makeText(getApplicationContext(),
					R.string.removed_ads_toast, Toast.LENGTH_LONG);
				toast.show();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (SendIntentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	  private boolean shouldShowAds(){
	    	Bundle ownedItems;
	    	
	    	SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
	    	return !prefs.getBoolean(Constants.REMOVED_ADS, false);
//			try {
//				ownedItems = mService.getPurchases(3, getPackageName(), "remove_ads", null);
//				int response = ownedItems.getInt("RESPONSE_CODE");
//				if (response == 0) {
//					ArrayList<String> ownedSkus = ownedItems
//							.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
//	
//					if (ownedSkus.contains("remove_ads")) {
//						return false;
//					}
//	
//				}
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	    }

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
	        case 0:
	            // Top Rated fragment activity
	        	return quickFragment;
	        case 1:
	        	return browseFragment;
	            // Games fragment activity
	        }
	 
	        return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.quick_tab_title).toUpperCase(l);
			case 1:
				return getString(R.string.browse_tab_title).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_tabbed,
					container, false);
			return rootView;
		}
	}
	
	public void remindMeButtonClick(View v){
		try{
		quickFragment.remindMeButtonClick(v);
		}catch(Exception e){
			
			TextView showText = new TextView(this);
			showText.setText(e.getMessage());
			showText.setTextIsSelectable(true);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setCancelable(true).setView(showText).show();
		}
	}
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onMovieListLoaded(List<Release> movieList) {

		browseFragment.setReleaseList(movieList);
		this.movieList = movieList;
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Constants.REMOVED_ADS)) {
			removeAdViews();
			
		}
		
	}
	
	private void removeAdViews(){
		RelativeLayout layout = (RelativeLayout) quickFragment.getView();
		layout.removeView(layout.findViewById(R.id.adView));
		
		RelativeLayout layout2 = (RelativeLayout) browseFragment.getView();
		layout2.removeView(layout2.findViewById(R.id.adView2));
	}

}
