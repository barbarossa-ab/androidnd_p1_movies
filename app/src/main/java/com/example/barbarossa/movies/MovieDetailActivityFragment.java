package com.example.barbarossa.movies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    final String IMG_BASE_URL = "http://image.tmdb.org/t/p/";
    final String IMG_RES = "w780/";


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

            int movieId = Integer.parseInt(intent.getStringExtra(Intent.EXTRA_TEXT));

            MoviesDataHolder.MovieData md = MoviesDataHolder.getInstance().getMovies().get(movieId);

            ImageView movieBackdrop = (ImageView)rootView.findViewById(R.id.movie_backdrop);
            TextView movieTitle = (TextView)rootView.findViewById(R.id.movie_title);
            TextView rating = (TextView)rootView.findViewById(R.id.movie_rating);
            TextView usersRated = (TextView)rootView.findViewById(R.id.movie_nr_users);
            TextView overview = (TextView)rootView.findViewById(R.id.movie_overview);
            TextView originalTitle = (TextView)rootView.findViewById(R.id.movie_original_title);
            TextView releaseDate = (TextView)rootView.findViewById(R.id.movie_release_date);

            String imgUrl = IMG_BASE_URL + IMG_RES + md.backdropPath;

            Picasso.with(getActivity()).load(imgUrl).into(movieBackdrop);

            movieTitle.setText(md.title);
            rating.setText("Score : " + md.voteAverage);
            usersRated.setText("Users : " + md.voteCount);
            overview.setText(md.overview);
            originalTitle.setText("Original title : " + md.originalTitle);
            releaseDate.setText("Release date : " + md.releaseDate);

            getActivity().setTitle(md.title);
        }

        return rootView;
    }
}
