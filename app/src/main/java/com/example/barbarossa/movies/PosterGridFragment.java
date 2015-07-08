package com.example.barbarossa.movies;

import android.app.Activity;
import android.content.Context;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class PosterGridFragment extends Fragment {

    PosterAdapter mPosterAdapter;

    public PosterGridFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_postergrid, container, false);

        mPosterAdapter = new PosterAdapter();

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mPosterAdapter);

        return rootView;
    }

    private class PosterAdapter extends BaseAdapter {
        private Context             mContext;

        public PosterAdapter() {
            mContext = PosterGridFragment.this.getActivity();
        }

        public int getCount() {
            if(MoviesDataHolder.getInstance() == null){
                return 0;
            } else {
                return MoviesDataHolder.getInstance().getMovies().size();
            }
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder vh;

            if (convertView == null) {
                convertView = ((Activity) mContext).getLayoutInflater()
                        .inflate(R.layout.movie_small, parent, false);

                vh = new ViewHolder();
                vh.text = (TextView) convertView.findViewById(R.id.movie_small_text);
                vh.image = (ImageView) convertView.findViewById(R.id.movie_small_iv);

                convertView.setTag(vh);

            } else {
                vh = (ViewHolder) convertView.getTag();

                vh.text = (TextView) convertView.findViewById(R.id.movie_small_text);
                vh.image = (ImageView) convertView.findViewById(R.id.movie_small_iv);
            }

            // set text and image
            if (MoviesDataHolder.getInstance() != null)
            {
                final String IMG_BASE_URL = "http://image.tmdb.org/t/p/";
                final String IMG_RES = "w185/";

                MoviesDataHolder.MovieData md = MoviesDataHolder.getInstance().getMovies().get(position);

                vh.text.setText(md.title);

                String imgUrl = IMG_BASE_URL + IMG_RES + md.posterPath;

                Picasso.with(mContext).load(imgUrl).fit().centerCrop().into(vh.image);

//                vh.image.setImageResource(R.drawable.interstellar_120);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView image;
        TextView text;
    }


    private void updateMovies() {
        FetchMoviesTask updateMoviesTask = new FetchMoviesTask();

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String fetchCriteria = prefs.getString(getString(R.string.pref_fetch_criteria),
//                getString(R.string.pref_fetch_criteria_default));
//
        updateMoviesTask.execute("popularity.desc");
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Void>
    {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                final String MOVIES_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                final String API_KEY = "bb2a38f64c6a5af584f22ef6c4ed416d";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

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
                moviesJsonStr = buffer.toString();
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

            try {
                getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            Log.e(LOG_TAG, moviesJsonStr);

            return null;
        }

        private void getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";

            final String OWM_ID = "id";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_TITLE = "title";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_VOTE_AVERAGE = "vote_average";
            final String OWM_VOTE_COUNT = "vote_count";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

            MoviesDataHolder.init(moviesArray.length());

            for(int i = 0; i < moviesArray.length(); i++) {
                MoviesDataHolder.MovieData md = new MoviesDataHolder.MovieData();
                JSONObject movie = moviesArray.getJSONObject(i);

                md.id = movie.getString(OWM_ID);
                md.posterPath = movie.getString(OWM_POSTER_PATH);
                md.title = movie.getString(OWM_TITLE);
                md.overview = movie.getString(OWM_OVERVIEW);
                md.releaseDate = movie.getString(OWM_RELEASE_DATE);
                md.voteAverage = movie.getString(OWM_VOTE_AVERAGE);
                md.voteCount = movie.getString(OWM_VOTE_COUNT);

                MoviesDataHolder.getInstance().getMovies().add(i, md);
            }
        }


        @Override
        protected void onPostExecute(Void result) {
            mPosterAdapter.notifyDataSetChanged();
        }


    }


}