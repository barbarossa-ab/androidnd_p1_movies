package com.example.barbarossa.movies;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Utility {

    public static final String PREF_DISC = "pref_disc";
    public static final String DISC_POPULAR = "popularity.desc";
    public static final String DISC_VOTE = "vote_average.desc";
    public static final String DISC_FAVOURITES = "favourites";
    public static final String DISC_DEFAULT = DISC_POPULAR;


    public static final String API_KEY_PARAM = "api_key";
    public static final String API_KEY = "bb2a38f64c6a5af584f22ef6c4ed416d";

    final static String MOVIES_DETAIL_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    final static String MOVIES_BASE_URL =
            "https://api.themoviedb.org/3/discover/movie?";
    final static String SORT_BY_PARAM = "sort_by";

    final private static String LOG_TAG = Utility.class.toString();

    public static String makeDiscoveyQuery(String discoveryCriteria) {
        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendQueryParameter(SORT_BY_PARAM, discoveryCriteria)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            URL url = new URL(builtUri.toString());
            return makeQuery(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String makeDetailsQuery(String movieId, String details) {
        String detailsURL = MOVIES_DETAIL_BASE_URL + movieId + "/" + details;

        Uri builtUri = Uri.parse(detailsURL).buildUpon()
                .appendQueryParameter(Utility.API_KEY_PARAM, Utility.API_KEY)
                .build();
        try {
            URL url = new URL(builtUri.toString());
            return makeQuery(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }


    private static String makeQuery(URL url) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {

//            Uri builtUri = Uri.parse(baseUrl).buildUpon()
//                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
//                    .build();
//
//            URL url = new URL(builtUri.toString());

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
            // If the code didn't successfully get the weather data, there's no point in attemping
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


}
