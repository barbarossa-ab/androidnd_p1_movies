package com.example.barbarossa.movies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.barbarossa.movies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    final static String IMG_BASE_URL = "http://image.tmdb.org/t/p/";
    final static String IMG_RES = "w185/";

    MoviesDataHolder.MovieData mMovieData;

//    String mMovieId;
    LinearLayout mTrailersList;
    LinearLayout mReviewsList;

    private static class Trailer {
        String id;
        String key;
        String name;
        String site;

        View view;
    }

    private static class Review {
        String author;
        String url;

        View view;
    }


    private ArrayList<Trailer> mTrailers = new ArrayList<>();
    private ArrayList<Review> mReviews = new ArrayList<>();


//    private ArrayAdapter<String> mTrailersAdapter;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            int movieIndex = Integer.parseInt(intent.getStringExtra(Intent.EXTRA_TEXT));

            mMovieData = MoviesDataHolder.getInstance().getMovies().get(movieIndex);

            ImageView moviePoster = (ImageView)rootView.findViewById(R.id.movie_detail_poster);
            TextView movieTitle = (TextView)rootView.findViewById(R.id.movie_detail_title);
            TextView releaseDate = (TextView)rootView.findViewById(R.id.release_date_text);
            TextView duration = (TextView) rootView.findViewById(R.id.duration_text);

            TextView rating = (TextView)rootView.findViewById(R.id.rating_text);
            TextView overview = (TextView)rootView.findViewById(R.id.overview);

            mTrailersList = (LinearLayout) rootView.findViewById(R.id.trailers_list);
            mReviewsList = (LinearLayout) rootView.findViewById(R.id.reviews_list);

            String imgUrl = IMG_BASE_URL + IMG_RES + mMovieData.posterPath;

            Picasso.with(getActivity()).load(imgUrl).into(moviePoster);

            movieTitle.setText(mMovieData.title);
            rating.setText(mMovieData.voteAverage + "/10");
            overview.setText(mMovieData.overview);
            releaseDate.setText(mMovieData.releaseDate);

            Button favButton = (Button)rootView.findViewById(R.id.favourite_button);
            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addMovieToFavourites();
                }
            });

            getActivity().setTitle(mMovieData.originalTitle);

            new FetchTrailersTask().execute();
            new FetchReviewsTask().execute();

            // show all favourite movies
            Cursor favouriteCursor = getActivity().getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    new String[]{MoviesContract.MovieEntry.COLUMN_TITLE},
                    null,
                    null,
                    null);

//            while(favouriteCursor.moveToNext()) {
//                Log.e("DBG", favouriteCursor.getString(0));
//
//            }

            String cursorString = DatabaseUtils.dumpCursorToString(favouriteCursor);

            Log.e("DBG", cursorString);

        }

        return rootView;
    }

    public long addMovieToFavourites() {
        long movieId = -1;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = getActivity().getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry._ID},
                MoviesContract.MovieEntry.COLUMN_API_ID + " = ?",
                new String[]{mMovieData.id},
                null);

        if(!movieCursor.moveToFirst()) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MovieEntry.COLUMN_API_ID, mMovieData.id);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovieData.title);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovieData.originalTitle);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, mMovieData.posterPath);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW,mMovieData.overview);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, mMovieData.releaseDate);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, Float.parseFloat(mMovieData.voteAverage));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, Integer.parseInt(mMovieData.voteCount));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_DURATION, 120);

            // Finally, insert location data into the database.
            Uri insertedUri = getActivity().getContentResolver().insert(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            movieId = ContentUris.parseId(insertedUri);
        } else {
            int movieIdIndex = movieCursor.getColumnIndex(MoviesContract.MovieEntry._ID);
            movieId = movieCursor.getLong(movieIdIndex);
        }

        return movieId;
    }


    public class FetchTrailersTask extends AsyncTask<Void, Void, Void>
    {
        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            String jsonStr = Utility.makeDetailsQuery(mMovieData.id, "videos");

            try {
                getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            Log.e(LOG_TAG, jsonStr);

            return null;
        }

        private void getTrailersDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";

            final String OWM_ID = "id";
            final String OWM_KEY = "key";
            final String OWM_NAME = "name";
            final String OWM_SITE = "site";


            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

            mTrailers.clear();

            for(int i = 0; i < moviesArray.length(); i++) {

                Trailer trailer = new Trailer();
                JSONObject trailerJson = moviesArray.getJSONObject(i);

                trailer.id = trailerJson.getString(OWM_ID);
                trailer.key = trailerJson.getString(OWM_KEY);
                trailer.name = trailerJson.getString(OWM_NAME);
                trailer.site = trailerJson.getString(OWM_SITE);

                mTrailers.add(trailer);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mTrailers.size() != 0) {
                for (Trailer t : mTrailers) {
                    t.view = getActivity().getLayoutInflater()
                            .inflate(R.layout.movie_trailer_item, null);

                    ((TextView)((t.view).findViewById(R.id.trailers_text))).setText(t.name);

                    final String key = t.key;

                    t.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String videoPath = "https://www.youtube.com/watch?v=" + key;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
                            startActivity(intent);
                        }
                    });

                    mTrailersList.addView(t.view);
                }
            } else {
                mTrailersList.setVisibility(View.GONE);
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<Void, Void, Void>
    {
        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            String jsonStr = Utility.makeDetailsQuery(mMovieData.id, "reviews");
            try {
                getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            Log.e(LOG_TAG, jsonStr);

            return null;
        }

        private void getReviewsDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";

            final String OWM_AUTHOR = "author";
            final String OWM_URL = "url";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

            mReviews.clear();

            for(int i = 0; i < moviesArray.length(); i++) {

                Review review = new Review();
                JSONObject trailerJson = moviesArray.getJSONObject(i);

                review.author = trailerJson.getString(OWM_AUTHOR);
                review.url = trailerJson.getString(OWM_URL);

                mReviews.add(review);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mReviews.size() != 0) {
                for (Review r : mReviews) {
                    r.view = getActivity().getLayoutInflater()
                            .inflate(R.layout.movie_review_item, null);

                    ((TextView)((r.view).findViewById(R.id.review_text))).setText(r.author);

                    final String url = r.url;

                    r.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    });

                    mReviewsList.addView(r.view);
                }
            } else {
                mReviewsList.setVisibility(View.GONE);
            }
        }
    }

}
