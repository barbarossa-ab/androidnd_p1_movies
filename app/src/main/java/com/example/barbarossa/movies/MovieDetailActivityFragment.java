package com.example.barbarossa.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    String mMovieId;
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

            MoviesDataHolder.MovieData md = MoviesDataHolder.getInstance().getMovies().get(movieIndex);
            mMovieId = md.id;

            ImageView moviePoster = (ImageView)rootView.findViewById(R.id.movie_detail_poster);
            TextView movieTitle = (TextView)rootView.findViewById(R.id.movie_detail_title);
            TextView releaseDate = (TextView)rootView.findViewById(R.id.release_date_text);
            TextView duration = (TextView) rootView.findViewById(R.id.duration_text);

            TextView rating = (TextView)rootView.findViewById(R.id.rating_text);
            TextView overview = (TextView)rootView.findViewById(R.id.overview);

            mTrailersList = (LinearLayout) rootView.findViewById(R.id.trailers_list);
            mReviewsList = (LinearLayout) rootView.findViewById(R.id.reviews_list);

            String imgUrl = IMG_BASE_URL + IMG_RES + md.posterPath;

            Picasso.with(getActivity()).load(imgUrl).into(moviePoster);

            movieTitle.setText(md.title);
            rating.setText(md.voteAverage + "/10");
            overview.setText(md.overview);
            releaseDate.setText(md.releaseDate);

            getActivity().setTitle(md.originalTitle);

            new FetchTrailersTask().execute();
            new FetchReviewsTask().execute();


        }

        return rootView;
    }


    public class FetchTrailersTask extends AsyncTask<Void, Void, Void>
    {
        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            String jsonStr = Utility.makeDetailsQuery(mMovieId, "videos");

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
            String jsonStr = Utility.makeDetailsQuery(mMovieId, "reviews");
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
