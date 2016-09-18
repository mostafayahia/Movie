package com.android.example.movie;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yahia on 9/8/16.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     * check if the device is connected to the internet or not
     * @return true if the device is connected to the internet, false otherwise
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * fetch json string for a given url
     * @param urlStr url string which will be used to make api request
     * @return json string after making api request
     */
    static String getJsonStr(String urlStr) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {

            final String API_KEY_PARAM = "api_key";


            Uri uri =
                    Uri.parse(urlStr).buildUpon()
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVE_DB_API_KEY)
                            .build();


            URL url = new URL(uri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movies data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return jsonStr;
    }

    /**
     * show a toast message according to resource id
     * @param resid represent a reference for a string in xml
     */
    static void showToastMessage(Context context, int resid) {
        Toast.makeText(context, context.getString(resid), Toast.LENGTH_SHORT)
                .show();
    }

    static String getSortingPref(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_sorting_key),
                        context.getString(R.string.pref_sorting_default));
    }
}
