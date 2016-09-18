package com.android.example.movie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by yahia on 8/11/16.
 */

public class PosterArrayAdapter extends ArrayAdapter<String> {

    // using this constant for logging
    private final String LOG_TAG = PosterArrayAdapter.class.getSimpleName();


    /**
     * @param context of the application
     * @param posterUrls to get images that will be loaded in images views
     */
    public PosterArrayAdapter(Context context, List<String> posterUrls) {
        // we won't use the second parameter so we will pass it with zero
        super(context, 0, posterUrls);
    }

    /**
     *
     * @param position of the element in the grid view
     * @param convertView the recycled view
     * @param parent of the recycled view (view group contain this view)
     * @return view after updates
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // extracting the corresponding url to the image
        String posterUrl = getItem(position);

        // if convertView null, we create a new one to set our image
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.list_item_grid, parent, false);
        }

        // getting the image view lying inside convertView layout
        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_grid_imageView);

        // testing my logic by setting image to imageView offline without depending on picasso library
        //imageView.setImageResource(R.mipmap.ic_launcher);

        // image link for testing: http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
        Picasso.with(getContext())
                .load(posterUrl)
                .into(imageView);


        return convertView;
    }
}
