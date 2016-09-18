package com.android.example.movie;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.movie.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.example.movie.data.MovieContract.FavoriteMovieEntry;

/**
 * Created by yahia on 9/8/16.
 */
public class DetailFragment extends Fragment implements View.OnClickListener {

    private MovieInfo mMovieInfo;
    private MoreMovieInfo mMoreMovieInfo;
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private Button mFavoriteButton;
    private SQLiteOpenHelper mOpenHelper;
    private View mRootView;
    private boolean mExistInFavorites = false;
    private static final String MORE_MOVIE_INFO_KEY = "MMIKEY";
    private static final String EXIST_IN_FAVORITES_KEY = "EIFKEY";


    // colors constants used for favorite button style
    // NOTE: you must specify alpha and write the color in the form "alpha red green blue"
    private static final int COLOR_WHITE = 0xffffffff;
    private static final int COLOR_GREEN = 0xff009688;
    private static final int COLOR_BLACK = 0xff000000;
    private static final int COLOR_YELLOW = 0xffffff00;

    private static final String[] SELECTION_COLUMNS = {
        FavoriteMovieEntry.COLUMN_RUNNING_TIME,
        FavoriteMovieEntry.COLUMN_REVIEWS,
        FavoriteMovieEntry.COLUMN_TRAILER1,
        FavoriteMovieEntry.COLUMN_TRAILER2
    };

    private static final int COLUMN_RUNNING_TIME = 0;
    private static final int COLUMN_REVIEWS = 1;
    private static final int COLUMN_TRAILER1 = 2;
    private static final int COLUMN_TRAILER2 = 3;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // inflate our detail fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mRootView = rootView;

        mOpenHelper = new MovieDbHelper(getActivity());

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieInfo = arguments.getParcelable(PosterFragment.MOVIE_INFO_DATA);
        } else {
            // in this case I decide to NOT display any movie detail until the user clicks to a
            // certain movie in the poster grid
            getFragmentManager().beginTransaction()
                    .remove(getFragmentManager().findFragmentByTag(MainActivity.DETAILFRAGMENT_TAG))
                    .commit();
            // no point for continue
            return null;
        }

        // getting movie release date which has the following format (yyyy-mm-dd)
        final String RELEASE_DATE = mMovieInfo.getReleaseDate();

        // loading data from movie info into our text views in the detail activity
        ((TextView) rootView.findViewById(R.id.detail_title_textview))
                .setText(mMovieInfo.getOriginalTitle());
        ((TextView) rootView.findViewById(R.id.detail_release_year_textview))
                .setText(RELEASE_DATE.substring(0, RELEASE_DATE.indexOf("-"))); // we get just a year only from the release date
        ((TextView) rootView.findViewById(R.id.detail_rate_textview))
                .setText(mMovieInfo.getVoteAverage() + "/10");
        ((TextView) rootView.findViewById(R.id.detail_plot_synopsis_textview))
                .setText(mMovieInfo.getOverview());


        // loading poster thumbnail into imageView lying in detail fragment
        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.detail_poster_image);
        Picasso.with(getActivity()).load(mMovieInfo.getPosterUrl()).into(posterImageView);

        // set event handlers for the buttons
        mFavoriteButton = (Button) rootView.findViewById(R.id.detail_favorite_button);
        mFavoriteButton.setOnClickListener(this);
        // mMoreMovieInfo may be null, so we can't insert movie info into the database using
        // mFavoriteButton
        mFavoriteButton.setVisibility(View.INVISIBLE);

        // set event handlers for trailers buttons (remember we dispaly 2 trailers as max)
        Button trailer1Button = (Button) rootView.findViewById(R.id.detail_trailer1_button);
        Button trailer2Button = (Button) rootView.findViewById(R.id.detail_trailer2_button);
        trailer1Button.setOnClickListener(this);
        trailer2Button.setOnClickListener(this);

        // check if the this movie is already exist in database or not
        if (savedInstanceState != null && savedInstanceState.containsKey(EXIST_IN_FAVORITES_KEY)) {
            mExistInFavorites = savedInstanceState.getBoolean(EXIST_IN_FAVORITES_KEY);
        } else {
            // I save this value in a member value to save the process of query the database every
            // time I need to know if this movie in favorites table or not
            mExistInFavorites = existInDB(mMovieInfo.getId());
        }

        // decorate favorite button based on mExistInFavorites state
        decorateFavoriteButton(mExistInFavorites);

        if (savedInstanceState != null && savedInstanceState.containsKey(MORE_MOVIE_INFO_KEY)) {
            mMoreMovieInfo = savedInstanceState.getParcelable(MORE_MOVIE_INFO_KEY);
            bindViewsAfterFetching();

        } else if (mExistInFavorites) {
            // retrieving movie info from the database instead of making api request
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    FavoriteMovieEntry.TABLE_NAME,
                    SELECTION_COLUMNS,
                    FavoriteMovieEntry._ID + " = ?",
                    new String[]{Integer.toString(mMovieInfo.getId())},
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            mMoreMovieInfo = new MoreMovieInfo(
                    cursor.getInt(COLUMN_RUNNING_TIME),
                    cursor.getString(COLUMN_TRAILER1),
                    cursor.getString(COLUMN_TRAILER2),
                    cursor.getString(COLUMN_REVIEWS)
            );
            cursor.close();
            db.close();
            // now the info is exist in mMoreMovieInfo so we can bind related views
            bindViewsAfterFetching();

        } else {
            // the movie doesn't exist in the local database, so we will make api request to fetch
            // the more movie info
            if (Utility.isOnline(getActivity())) {
                new FetchMoreMovieInfo().execute();
            }

        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MORE_MOVIE_INFO_KEY, mMoreMovieInfo);
    }

    private void bindViewsAfterFetching() {

        ((TextView) mRootView.findViewById(R.id.detail_running_time_text_view))
                .setText(mMoreMovieInfo.runningTime + "min");
        ((TextView) mRootView.findViewById(R.id.detail_reviews_content_textview))
                .setText(mMoreMovieInfo.reviews);
        // now we make sure that mMoreMovieInfo is not null so we can insert the movie info into
        // database using favorite button
        mFavoriteButton.setVisibility(View.VISIBLE);

        // hide unnecessary trailers' buttons (if no of trailers < 2)
        // remember max number of trailers we display in this app is 2
        String trailer1 = mMoreMovieInfo.trailer1;
        String trailer2 = mMoreMovieInfo.trailer2;
        if (trailer1 == null || trailer1.length() == 0) {
            mRootView.findViewById(R.id.detail_trailers_title)
                    .setVisibility(View.GONE);
            mRootView.findViewById(R.id.detail_trailer1_button)
                    .setVisibility(View.GONE);
            mRootView.findViewById(R.id.detail_trailer1_ruler)
                    .setVisibility(View.GONE);
        }
        if (trailer2 == null || trailer2.length() == 0) {
            mRootView.findViewById(R.id.detail_trailer2_button)
                    .setVisibility(View.GONE);
            mRootView.findViewById(R.id.detail_trailer2_ruler)
                    .setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_favorite_button: {

                int movieId = mMovieInfo.getId();

                // we toggle movie state when the favorite button is pressed
                if (mExistInFavorites) {
                    // then we will remove the movie from the database and update button view
                    // based on the new state
                    deleteFromDb(movieId);
                    decorateFavoriteButton(false);
                    mExistInFavorites = false;
                } else {
                    insertInDB(movieId);
                    decorateFavoriteButton(true);
                    mExistInFavorites = true;
                }


                String sorting = Utility.getSortingPref(getActivity());
                if (sorting.equals(getString(R.string.pref_sorting_value_favorites)) &&
                        getActivity() instanceof MainActivity) {
                    // in this case we are in 2 pane mode and the user choose to sort by favorites
                    // so we want to update poster fragment
                    PosterFragment.getMoviesInfoFromDBThenUpdate();

                }
            }
            break;

            case R.id.detail_trailer1_button: {
                startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(mMoreMovieInfo.trailer1))
                );
            }
            break;

            case R.id.detail_trailer2_button: {
                startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(mMoreMovieInfo.trailer2))
                );
            }
            break;

        }
    }

    private void decorateFavoriteButton(boolean inFavorite) {
        if (inFavorite) {
            mFavoriteButton.setText(getString(R.string.state_in_favorite));
            mFavoriteButton.setTextColor(COLOR_BLACK);
            mFavoriteButton.setBackgroundColor(COLOR_YELLOW);
        } else {
            mFavoriteButton.setText(getString(R.string.state_not_in_favorite));
            mFavoriteButton.setTextColor(COLOR_WHITE);
            mFavoriteButton.setBackgroundColor(COLOR_GREEN);
        }
    }

    private boolean existInDB(int id) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(
                FavoriteMovieEntry.TABLE_NAME,
                new String[]{FavoriteMovieEntry.COLUMN_DATE}, // any column (just don't retrieve all columns)
                FavoriteMovieEntry._ID + " = ?",
                new String[]{Integer.toString(id)},
                null,
                null,
                null
        );
        int rows = cursor.getCount();
        cursor.close();
        db.close();
        return rows != 0;
    }

    private Long insertInDB(int id) {
        // getting the values from the member objects
        ContentValues values = new ContentValues();
        values.put(FavoriteMovieEntry._ID, id);
        values.put(FavoriteMovieEntry.COLUMN_DATE, mMovieInfo.getReleaseDate());
        values.put(FavoriteMovieEntry.COLUMN_OVERVIEW, mMovieInfo.getOverview());
        values.put(FavoriteMovieEntry.COLUMN_POSTER, mMovieInfo.getPosterUrl());
        values.put(FavoriteMovieEntry.COLUMN_TITLE, mMovieInfo.getOriginalTitle());
        values.put(FavoriteMovieEntry.COLUMN_RATING, mMovieInfo.getVoteAverage());
        values.put(FavoriteMovieEntry.COLUMN_RUNNING_TIME, mMoreMovieInfo.runningTime);
        values.put(FavoriteMovieEntry.COLUMN_TRAILER1, mMoreMovieInfo.trailer1);
        values.put(FavoriteMovieEntry.COLUMN_TRAILER2, mMoreMovieInfo.trailer2);
        values.put(FavoriteMovieEntry.COLUMN_REVIEWS, mMoreMovieInfo.reviews);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Long insertedRow = db.insert(FavoriteMovieEntry.TABLE_NAME, null, values);
        db.close();

        return insertedRow;
    }

    private int deleteFromDb(int id) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deletedRow = db.delete(
                FavoriteMovieEntry.TABLE_NAME,
                FavoriteMovieEntry._ID + " = ?",
                new String[]{Integer.toString(id)});
        db.close();

        return deletedRow;
    }




    private class FetchMoreMovieInfo extends AsyncTask<Void, Void, MoreMovieInfo> {

        private String LOG_TAG = FetchMoreMovieInfo.class.getSimpleName();

        @Override
        protected MoreMovieInfo doInBackground(Void... params) {

            final String BASE_URL = "http://api.themoviedb.org/3/movie/" + mMovieInfo.getId();
            String idJsonStr = Utility.getJsonStr(BASE_URL);
            String trailersJsonStr = Utility.getJsonStr(BASE_URL + "/videos");
            String reviewsJsonStr = Utility.getJsonStr(BASE_URL + "/reviews");

            try {
                return getMoreMovieInfoFromJson(idJsonStr, trailersJsonStr, reviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the json response.
            return null;
        }

        private MoreMovieInfo getMoreMovieInfoFromJson(
                String idJsonStr, String trailersJsonStr, String reviewsJsonStr)
                throws JSONException {

            // the names of json objects
            final String TMDB_RESULTS = "results";
            final String TMDB_KEY = "key";
            final String TMDB_RUNTIME = "runtime";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";

            final String YOUTUBE_VIDEO_PARAM = "v";
            final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";

            // extracting trailers' urls
            // NOTE: we only retrieve 2 trailers as max
            String trailer1 = "";
            String trailer2 = "";
            JSONArray jsonTrailers = new JSONObject(trailersJsonStr).getJSONArray(TMDB_RESULTS);
            // if the movie has trailers we get it
            if (jsonTrailers.length() > 0) {
                String youtubeVideoCode1 = jsonTrailers.getJSONObject(0).getString(TMDB_KEY);
                String youtubeVideoCode2 = null;
                if (jsonTrailers.length() > 1)
                    youtubeVideoCode2 = jsonTrailers.getJSONObject(1).getString(TMDB_KEY);
                trailer1 = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                        .appendQueryParameter(YOUTUBE_VIDEO_PARAM, youtubeVideoCode1).toString();
                trailer2 = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                        .appendQueryParameter(YOUTUBE_VIDEO_PARAM, youtubeVideoCode2).toString();
            }

            // extracting running time of the movie
            int runningTime = new JSONObject(idJsonStr).getInt(TMDB_RUNTIME);

            // extracting the reviews
            StringBuffer reviewsBuffer = new StringBuffer();
            JSONArray jsonReviewArray = new JSONObject(reviewsJsonStr).getJSONArray(TMDB_RESULTS);
            if (jsonReviewArray.length() > 0) {
                for (int i = 0; i < jsonReviewArray.length(); i++) {
                    JSONObject jsonReview = jsonReviewArray.getJSONObject(i);
                    String author = jsonReview.getString(TMDB_AUTHOR);
                    String reviewContent = jsonReview.getString(TMDB_CONTENT);
                    reviewsBuffer.append(
                            "REVIEW FROM " + author.toUpperCase() +
                                    ":\n\n" + reviewContent + "\n\n");
                }
            }



            return new MoreMovieInfo(runningTime, trailer1, trailer2, reviewsBuffer.toString());
        }

        @Override
        protected void onPostExecute(MoreMovieInfo moreMovieInfo) {

            mMoreMovieInfo = moreMovieInfo;
            bindViewsAfterFetching();
        }
    }
}
