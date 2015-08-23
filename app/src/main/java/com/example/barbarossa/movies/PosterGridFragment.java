package com.example.barbarossa.movies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.barbarossa.movies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PosterGridFragment extends Fragment {

    public static final String JSON_STRING_KEY = "JSON_STRING_KEY";

    public interface Callback {
        void onItemSelected(int movieIndex);
    }

    PosterAdapter mPosterAdapter;
    String  mJsonString;

    public PosterGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(mJsonString != null) {
            savedInstanceState.putString(JSON_STRING_KEY, mJsonString);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_postergrid, container, false);

        mPosterAdapter = new PosterAdapter();

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mPosterAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
//                        .putExtra(Intent.EXTRA_TEXT, Integer.toString(position));
//
//                startActivity(intent);

                ((Callback) getActivity()).onItemSelected(position);
            }
        });

        if(savedInstanceState == null){
            updateMovies();
        } else {
            mJsonString = savedInstanceState.getString(JSON_STRING_KEY);
            try {
                getMoviesDataFromJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mPosterAdapter.notifyDataSetChanged();
        }

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

//                vh.image.setImageResource(R.drawable.interstellar_poster);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView image;
        TextView text;
    }


    private void updateMovies() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        String discoverCrit = sharedPrefs.getString(
                Utility.PREF_DISC,
                Utility.DISC_DEFAULT);

        if(discoverCrit.equals(Utility.DISC_FAVOURITES)) {
            updateMoviesFromFavourites();
        } else {
            FetchMoviesTask updateMoviesTask = new FetchMoviesTask();
            updateMoviesTask.execute(discoverCrit);
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Void>
    {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            mJsonString = Utility.makeDiscoveyQuery(params[0]);

            try {
                getMoviesDataFromJson();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            Log.e(LOG_TAG, mJsonString);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mPosterAdapter.notifyDataSetChanged();
        }

    }

    private void getMoviesDataFromJson()
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";

        final String OWM_ID = "id";
        final String OWM_POSTER_PATH = "poster_path";
        final String OWM_BACKDROP_PATH = "backdrop_path";
        final String OWM_TITLE = "title";
        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_VOTE_COUNT = "vote_count";
        final String OWM_ORIGINAL_TITLE = "original_title";


        JSONObject moviesJson = new JSONObject(mJsonString);
        JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

        MoviesDataHolder.init(moviesArray.length());

        for(int i = 0; i < moviesArray.length(); i++) {
            MoviesDataHolder.MovieData md = new MoviesDataHolder.MovieData();
            JSONObject movie = moviesArray.getJSONObject(i);

            md.id = movie.getString(OWM_ID);
            md.posterPath = movie.getString(OWM_POSTER_PATH);
            md.backdropPath = movie.getString(OWM_BACKDROP_PATH);
            md.title = movie.getString(OWM_TITLE);
            md.overview = movie.getString(OWM_OVERVIEW);
            md.releaseDate = movie.getString(OWM_RELEASE_DATE);
            md.voteAverage = movie.getString(OWM_VOTE_AVERAGE);
            md.voteCount = movie.getString(OWM_VOTE_COUNT);
            md.originalTitle = movie.getString(OWM_ORIGINAL_TITLE);
            md.releaseDate = movie.getString(OWM_RELEASE_DATE);

            MoviesDataHolder.getInstance().getMovies().add(i, md);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String discoverCrit = sharedPrefs.getString(
                Utility.PREF_DISC,
                Utility.DISC_DEFAULT);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_popular) {
            if (discoverCrit != Utility.DISC_POPULAR)
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.PREF_DISC,
                        Utility.DISC_POPULAR);
                editor.commit();

                updateMovies();
            }

            return true;
        }

        if (id == R.id.action_show_best_rated) {
            if (discoverCrit != Utility.DISC_VOTE)
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.PREF_DISC,
                        Utility.DISC_VOTE);
                editor.commit();

                updateMovies();
            }

            return true;
        }

        if (id == R.id.action_show_favourites) {
            if (discoverCrit != Utility.DISC_FAVOURITES)
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.PREF_DISC,
                        Utility.DISC_FAVOURITES);
                editor.commit();

                updateMovies();
            }

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    private void updateMoviesFromFavourites() {
        final String[] FAVOURITES_COLUMNS = {
                MoviesContract.MovieEntry.COLUMN_API_ID,
                MoviesContract.MovieEntry.COLUMN_TITLE,
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
                MoviesContract.MovieEntry.COLUMN_OVERVIEW,
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT,
                MoviesContract.MovieEntry.COLUMN_DURATION
        };

        final int COL_API_ID = 0;
        final int COL_TITLE = 1;
        final int COL_ORIGINAL_TITLE = 2;
        final int COL_POSTER_PATH = 3;
        final int COL_OVERVIEW = 4;
        final int COL_RELEASE_DATE = 5;
        final int COL_VOTE_AVERAGE = 6;
        final int COL_VOTE_COUNT = 7;
        final int COL_DURATION = 8;

        // show all favourite movies
        Cursor favouriteCursor = getActivity().getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                FAVOURITES_COLUMNS,
                null,
                null,
                null);

        MoviesDataHolder.init(favouriteCursor.getCount());

        while(favouriteCursor.moveToNext()) {
            MoviesDataHolder.MovieData md = new MoviesDataHolder.MovieData();

            md.id = Integer.toString(favouriteCursor.getInt(COL_API_ID));
            md.title = favouriteCursor.getString(COL_TITLE);
            md.originalTitle = favouriteCursor.getString(COL_ORIGINAL_TITLE);
            md.posterPath = favouriteCursor.getString(COL_POSTER_PATH);
            md.overview = favouriteCursor.getString(COL_OVERVIEW);
            md.releaseDate = favouriteCursor.getString(COL_RELEASE_DATE);
            md.voteAverage = Float.toString(favouriteCursor.getFloat(COL_VOTE_AVERAGE));
            md.voteCount = Integer.toString(favouriteCursor.getInt(COL_VOTE_COUNT));
            md.duration = Integer.toString(favouriteCursor.getInt(COL_DURATION));

            MoviesDataHolder.getInstance().getMovies().add(md);
        }

        mPosterAdapter.notifyDataSetChanged();
    }
}