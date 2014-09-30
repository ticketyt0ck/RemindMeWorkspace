package com.nyelito.remindmeapp.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nyelito.remindmeapp.Release;
import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.MovieThumbnail;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.cards.ReleaseCard;
import com.nyelito.remindmeapp.cards.StickyCardArrayAdapter;
import com.nyelito.remindmeapp.cards.StickyCardListView;
 
public class BrowseFragment extends Fragment {
	
	
	private static String BROWSE_MOVIE_LIST_KEY = "browseMovieListKey";
	List<Release> movieList;
	private StickyCardArrayAdapter adapter;
	private View currView;
	private boolean shouldShowAds;
	
	public void setShouldShowAds(boolean toSet){
		shouldShowAds = toSet;
	}
 
    public StickyCardArrayAdapter getAdapter() {
		return adapter;
	}


	public void setReleaseList(List<Release> realeaseList) {
		this.movieList = realeaseList;
		
		 ArrayList<Card> cards = new ArrayList<Card>();
	        if(realeaseList != null){
	        	int count = 0;
		        for (Release m : realeaseList) {
		            // Create a Card
		            ReleaseCard card = new ReleaseCard(getActivity(), m);
		            // Create a CardHeader
		            CardHeader header = new CardHeader(getActivity());
		            // Add Header to card
		            header.setTitle(m.getTitle());
		            card.setTitle(m.getFormattedDate());
		            card.addCardHeader(header);
		            
		            // if we don't have an image, don't try and add an image
		            if(m.getPosterURL() != null && !m.getPosterURL().isEmpty()){
		            	
			            //Add Thumbnail
			            MovieThumbnail thumbnail = new MovieThumbnail(getActivity(), m.getPosterURL());
			            //You need to set true to use an external library
			            thumbnail.setExternalUsage(true);
			            card.addCardThumbnail(thumbnail);
		            }
		 
		            cards.add(card);
//		            
//		            if(count == 0){
//		            	
////		              if (shouldShowAds) {
//		    			
//		    			Card adCard = new Card(getActivity(), R.layout.browse_ad);
//		    		
//		    			
////		    			adCard.setTitle("AD CARD");
//		    			cards.add(adCard);
////		    		}
//		    			
//		            }
//		            
//		            count++;
		            
		        }
	        }
	        // if the activity is null, don't do this
	        if(getActivity() != null){
		        StickyCardArrayAdapter adapter = new StickyCardArrayAdapter(getActivity(), cards, realeaseList);
		        this.adapter = adapter;
		        StickyCardListView stickyList = (StickyCardListView) currView.findViewById(R.id.carddemo_extra_sticky_list);
		        stickyList.setFastScrollEnabled(true);
		        if (stickyList != null) {
		            stickyList.setAdapter(adapter);
		        }
	        }
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(BROWSE_MOVIE_LIST_KEY, (ArrayList<? extends Parcelable>) movieList);
	}


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
		RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.cardlib_list, container, false);
        currView = rootView;
        
        if(movieList != null && !movieList.isEmpty()){
        	setReleaseList(movieList);
        }
        
        if(savedInstanceState != null){
//			movieList = savedInstanceState.getParcelableArrayList(BROWSE_MOVIE_LIST_KEY);
//			setReleaseList(movieList);
		}
         
        

         
        return rootView;
    }
	
	
	@Override
	public void onActivityCreated(Bundle bundle) {
	    super.onActivityCreated(bundle);
		if (shouldShowAds) {
			AdView mAdView = (AdView) getView().findViewById(R.id.adView2);
			AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
			adRequestBuilder.addKeyword("movies");
			adRequestBuilder.addKeyword("theater");
			adRequestBuilder.addKeyword("trailer");
			adRequestBuilder.addKeyword("film");
			adRequestBuilder.addKeyword("cinema");
			AdRequest adRequest = adRequestBuilder.build();

			mAdView.loadAd(adRequest);
		}
	}
	
}