package com.nyelito.remindmeapp;

import it.gmariotti.cardslib.library.internal.CardThumbnail;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieThumbnail extends CardThumbnail {
	
	private String imageURL;

    public MovieThumbnail(Context context, String URL) {
		super(context);
		this.imageURL = URL;
    }

	@Override
    public void setupInnerViewElements(ViewGroup parent, View viewImage) {

        //Here you have to set your image with an external library
        Picasso.with(getContext())
               .load(imageURL)
               .into((ImageView)viewImage);

        viewImage.getLayoutParams().width = 200;
        viewImage.getLayoutParams().height = 300;
    }
}