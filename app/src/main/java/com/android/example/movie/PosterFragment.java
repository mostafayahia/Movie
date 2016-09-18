package com.android.example.movie;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.example.movie.data.MovieContract;
import com.android.example.movie.data.MovieDbHelper;

import java.util.ArrayList;

/**
 * Created by yahia on 8/12/16.
 */
public class PosterFragment extends Fragment {



    static final String[] SELECTION_COLUMNS = {
            MovieContract.FavoriteMovieEntry._ID,
            MovieContract.FavoriteMovieEntry.COLUMN_TITLE,
            MovieContract.FavoriteMovieEntry.COLUMN_RATING,
            MovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW,
            MovieContract.FavoriteMovieEntry.COLUMN_POSTER,
            MovieContract.FavoriteMovieEntry.COLUMN_DATE

    };

    static final int COLUMN_ID = 0;
    static final int COLUMN_TITLE = 1;
    static final int COLUMN_RATING = 2;
    static final int COLUMN_OVERVIEW = 3;
    static final int COLUMN_POSTER = 4;
    static final int COLUMN_DATE = 5;

    // using this tag for logging (debugging)
    private final String LOG_TAG = PosterFragment.class.getSimpleName();

    private static PosterArrayAdapter mPosterArrayAdapter;

    // this variable is set by onPostExecute() method in FetchMovieInfoTask
    static MovieInfo[] mMovieInfoArray;

    // used as a shared key for sending and getting parcelable movieInfo object
    static final String MOVIE_INFO_DATA = "movie_info";
    private static final String POSITION_KEY = "PKEY";

    private String mSortingPref;
    private static Context mContext;
    private static GridView mGridView;
    private static int mPosition;



    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_grid, container, false);

        // first phase passing dummy data to the grid view
//        String[] testStrs = {"http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"};
//        List<String> data = new ArrayList<String>(Arrays.asList(testStrs));


        mContext = getActivity();

        // getting grid view from the fragment root view then set to it dummy data
        mGridView = (GridView)rootView.findViewById(R.id.gridView);
        mPosterArrayAdapter = new PosterArrayAdapter(
                getActivity(), // activity that contain the fragment
                new ArrayList<String>());
        mGridView.setAdapter(mPosterArrayAdapter);

        // set click item listener for each grid item
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) getActivity()).onItemSelected(position);
                mPosition = position;
            }
        });


        mSortingPref = Utility.getSortingPref(getActivity());

        if (null != savedInstanceState && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(POSITION_KEY, mPosition);
    }


    /**
     * this method should be called in onPostExecute() method in FetchMovieInfoTask and should be
     * called after finishing adding elements to mPosterArrayAdapter and stop notifying grid view
     * otherwise calling this method has no effect
     */
    static void updateGridViewScrolling() {
        mGridView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onStart() {
        super.onStart();



        String sortingPref = Utility.getSortingPref(getActivity());
        Log.v(LOG_TAG, "mPosition " + mPosition);

        if (!sortingPref.equals(mSortingPref)) {
            // this scenario happens when going to settings activity and change
            // sorting preference and getting back to the main activity
            Fragment df = getFragmentManager().findFragmentByTag(MainActivity.DETAILFRAGMENT_TAG);
            if (df != null) {
                // we clear detail fragment which now we are in 2 pane mode
                getFragmentManager().beginTransaction().remove(df).commit();
            }
            mSortingPref = sortingPref;
            // display the poster grid from the beginning in this case
            mPosition = 0;
            if (sortingPref.equals(getString(R.string.pref_sorting_value_favorites))) {
                getMoviesInfoFromDBThenUpdate();
            } else if (Utility.isOnline(getActivity())) {
                new FetchMovieInfoTask(getActivity(), mPosterArrayAdapter).execute(sortingPref);
            } else {
                Utility.showToastMessage(getActivity(), R.string.msg_err_no_internet);
                // the next line is important to clear posters grid to clear posters' grid when going
                // to settings activity and change from sorting by favorites to sorting by something
                // else then return back to the main activity
                mPosterArrayAdapter.clear();
            }
        } else if (sortingPref.equals(getString(R.string.pref_sorting_value_favorites))) {
            // we get movies info in this case from local database then update grid view
            getMoviesInfoFromDBThenUpdate();
        }
        // fetching movies info only if the device is connected to internet otherwise
        // display a message to indicate there is no internet connection
        else if (Utility.isOnline(getActivity())) {

            new FetchMovieInfoTask(getActivity(), mPosterArrayAdapter).execute(sortingPref);
        } else {
            // the next line is useful to clear grid from posters when users change settings from
            // sorting by favorites to sorting by popular (or top rated) then return back to the
            // main activity
            mPosterArrayAdapter.clear();
            Utility.showToastMessage(getActivity(), R.string.msg_err_no_internet);
        }


    }

    static void getMoviesInfoFromDBThenUpdate() {
        // reading the movies from the favorite table in the database in this case
        SQLiteDatabase db = new MovieDbHelper(mContext).getReadableDatabase();
        Cursor cursor = db.query(
                MovieContract.FavoriteMovieEntry.TABLE_NAME,
                SELECTION_COLUMNS,
                null,
                null,
                null,
                null,
                null
        );

        // the next line is important specially in case of phone mode and we had only one movie in
        // the favorite then you delete it and coming back to
        // the main activity (cursor.getCount() = 0)
        mPosterArrayAdapter.clear();

        if (cursor.getCount() > 0) {
            mMovieInfoArray = new MovieInfo[cursor.getCount()];
            int index = 0;
            while (cursor.moveToNext()) {
                mMovieInfoArray[index++] =
                        new MovieInfo(cursor.getInt(COLUMN_ID),
                                cursor.getString(COLUMN_TITLE),
                                cursor.getString(COLUMN_POSTER),
                                cursor.getString(COLUMN_OVERVIEW),
                                cursor.getDouble(COLUMN_RATING),
                                cursor.getString(COLUMN_DATE));

            }


            for (MovieInfo movieInfo : mMovieInfoArray)
                mPosterArrayAdapter.add(movieInfo.getPosterUrl());
        }
        cursor.close();
        db.close();
    }


}