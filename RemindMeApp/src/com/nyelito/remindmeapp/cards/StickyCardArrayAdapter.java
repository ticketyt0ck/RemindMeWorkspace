package com.nyelito.remindmeapp.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.nyelito.remindmeapp.Movie;
import com.nyelito.remindmeapp.R;
import com.nyelito.remindmeapp.Release;

public class StickyCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter, Filterable {

	private List<Release> movieList;
	private ArrayList<Card> cardsList;
	private ArrayList<Card> originalCardList = new ArrayList<Card>(1000);
    public StickyCardArrayAdapter(Context context, List<Card> cards, List<Release> movieList) {
		super(context, cards);
		this.movieList = movieList;
		this.cardsList = (ArrayList<Card>) cards;
		this.originalCardList.addAll(cardsList);
		// TODO Auto-generated constructor stub
	}

	@Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
		  LayoutInflater mInflater = LayoutInflater.from(getContext());
	        View view = mInflater.inflate(R.layout.sticky_header, null);

	        CardView cardView = (CardView)view.findViewById(R.id.carddemo_card_sticky_header_id);

	        Card headerCard = new Card(getContext());
	        headerCard.setBackgroundResourceId(R.drawable.header_card);
	        headerCard.setTitle(movieList.get(position).getReleaseMonthAndYear());

	        cardView.setCard(headerCard);
	        return view;
    }

    @Override
    public long getHeaderId(int position) {
       return movieList.get(position).getReleaseDate().getMonth();
   }


   /**
    * Sets the {@link CardListView}
    *
    * @param cardListView
    *            cardListView
    */
   public void setCardListView(StickyCardListView cardListView) {
       this.mCardListView = getCardListView();
   }
   
   @Override
   public Filter getFilter() {
       return new Filter() {
           @Override
           protected FilterResults performFiltering(CharSequence charSequence) {
               List<Card> filteredResult = getFilteredResults(charSequence);

               FilterResults results = new FilterResults();
               results.values = filteredResult;
               results.count = filteredResult.size();

               return results;
           }

           @Override
           protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
               List<Card> filteredList = (ArrayList<Card>) filterResults.values;
               // if you have the same sized list as the original, that means you didn't
               // filter anything, so don't change anything
               if(filteredList.size() != originalCardList.size()){
            	   cardsList.clear();
            	   cardsList.addAll(filteredList);
               }else{
            	   cardsList.clear();
            	   cardsList.addAll(originalCardList);
            	   
            	   
               }
               StickyCardArrayAdapter.this.notifyDataSetChanged();
           }


           private ArrayList<Card> getFilteredResults(CharSequence constraint){
               if (constraint.length() == 0){
                   return originalCardList;
               }
               ArrayList<Card> listResult = new ArrayList<Card>();
               for (Card c : originalCardList){
            	   // gotta make them upper case otherwise you won't get matches on different cases
                   if (c.getCardHeader().getTitle().toUpperCase().contains(constraint.toString().toUpperCase())){
                       listResult.add(c);
                   }
               }
               return listResult;
           }
       };
   }
}



