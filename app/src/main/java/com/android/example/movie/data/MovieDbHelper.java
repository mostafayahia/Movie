package com.android.example.movie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.example.movie.data.MovieContract.FavoriteMovieEntry;

/**
 * Created by yahia on 9/7/16.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create movie table
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoriteMovieEntry.COLUMN_DATE + " TEXT NOT NULL, " +

                FavoriteMovieEntry.COLUMN_REVIEWS + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_TRAILER1 + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_TRAILER2 + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                FavoriteMovieEntry.COLUMN_RUNNING_TIME + " INTEGER NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next line
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(db);

    }
}
