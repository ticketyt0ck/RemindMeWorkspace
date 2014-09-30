package com.nyelito.remindmeapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HelpActivity extends Activity{
	
	
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);


	        setContentView(R.layout.help_layout);
	    }
	
	 public static class HelpFragment extends Fragment {

	        public HelpFragment() { }

	        @Override
	        public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                  Bundle savedInstanceState) {
	              View rootView = inflater.inflate(R.layout.help_layout,
	                      container, false);
	              return rootView;
	        }
	    } 

}
