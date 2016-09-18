package com.android.example.movie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yahia on 9/6/16.
 */
public class MovieContract {

    public static final class FavoriteMovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite_movie";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_RATING = "rate";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_RUNNING_TIME = "running_time";

        public static final String COLUMN_TRAILER1 = "trailer1";

        public static final String COLUMN_TRAILER2 = "trailer2";

        public static final String COLUMN_REVIEWS = "reviews";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER = "poster";
    }
}
