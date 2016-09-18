package com.android.example.movie;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yahia on 9/8/16.
 */
public class FetchMovieInfoTask extends AsyncTask<String, Void, MovieInfo[]> {

    // constant used for logging
    private final String LOG_TAG = FetchMovieInfoTask.class.getSimpleName();
    private final Context mContext;
    private final ArrayAdapter<String> mPosterArrayAdapter;


    public FetchMovieInfoTask(Context context, ArrayAdapter<String> posterArrayAdapter) {
        mContext = context;
        mPosterArrayAdapter = posterArrayAdapter;
    }

    @Override
    protected MovieInfo[] doInBackground(String... params) {

        // know sorting by what by retrieving data stored in sorting preference
        String sortingBy = params[0];
        // Construct the URL for the theMovieDB query
        final String MOVIE_URL;

        if (sortingBy.equals(mContext.getString(R.string.pref_sorting_value_popular))) {
            MOVIE_URL = "http://api.themoviedb.org/3/movie/popular?";
        } else if (sortingBy.equals(mContext.getString(R.string.pref_sorting_value_top_rated))) {
            MOVIE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
        } else {
            Log.e(LOG_TAG, sortingBy + " is not accepted value for sorting preference");
            throw new RuntimeException(sortingBy + " is not accepted value for sorting preference");
        }

        String moviesJsonStr = Utility.getJsonStr(MOVIE_URL);

        try {
            return getMoviesInfoFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the json response.
        return null;
    }

    private MovieInfo[] getMoviesInfoFromJson(String moviesJsonStr) throws JSONException {

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        // the names of json objects
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_ID = "id";

        // testing if I'm getting poster path correct or not from json string
//            Log.v(LOG_TAG, "poster_path: " +moviesJson
//                    .getJSONArray(TMDB_RESULTS)
//                    .getJSONObject(0)
//                    .get(TMDB_POSTER_PATH)
//            );

        // choosing width:185px as recommendation (suitable for most phones)
        String posterBaseUrl = "http://image.tmdb.org/t/p/w185";

        // getting json array associated with "results" string key in json
        // to extract each movie properties from each object in array
        JSONArray jsonMovieArray = moviesJson.getJSONArray(TMDB_RESULTS);
        final int NUM_MOVIES = jsonMovieArray.length();

        // creating MovieInfoArray to save movies' properties getting from json
        MovieInfo[] movieInfoArray = new MovieInfo[NUM_MOVIES];

        // looping over every json object to get & save movies' properties
        for (int i = 0 ; i < NUM_MOVIES; i++ ) {
            JSONObject jsonMovieObject = jsonMovieArray.getJSONObject(i);
            movieInfoArray[i] = new MovieInfo(
                    jsonMovieObject.getInt(TMDB_ID),
                    jsonMovieObject.getString(TMDB_ORIGINAL_TITLE),
                    posterBaseUrl +
                            jsonMovieObject.getString(TMDB_POSTER_PATH), //Note: we are only getting a relative path to the poster from this command
                    jsonMovieObject.getString(TMDB_OVERVIEW),
                    jsonMovieObject.getDouble(TMDB_VOTE_AVERAGE),
                    jsonMovieObject.getString(TMDB_RELEASE_DATE)
            );

        }

        return movieInfoArray;
    }


    @Override
    protected void onPostExecute(MovieInfo[] movieInfoArray) {
        mPosterArrayAdapter.clear();

        // looping over every movie info object to extract poster url to update grid view
        mPosterArrayAdapter.setNotifyOnChange(false);
        for (MovieInfo movieInfo : movieInfoArray)
            mPosterArrayAdapter.add(movieInfo.getPosterUrl());
        mPosterArrayAdapter.notifyDataSetChanged();

        // setting movieInfoArray to member variable to send it later to detail activity
        PosterFragment.mMovieInfoArray = movieInfoArray;

        // scroll to last movie which the user watched its details
        Log.v(LOG_TAG, "updateGridViewScrolling() called");
        PosterFragment.updateGridViewScrolling();
    }
}
