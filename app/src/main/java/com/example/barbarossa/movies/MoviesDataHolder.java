package com.example.barbarossa.movies;

import java.util.ArrayList;

/**
 * Created by barbarossa on 08/07/15.
 */
public class MoviesDataHolder
{
    private static MoviesDataHolder mInstance = null;

    private ArrayList<MovieData> movies;

    private MoviesDataHolder(int nrMovies)
    {
        this.movies = new ArrayList<>(nrMovies);
    }

    public ArrayList<MovieData> getMovies()
    {
        return movies;
    }

    public static void init(int n)
    {
        mInstance = new MoviesDataHolder(n);
    }

    public static MoviesDataHolder getInstance()
    {
        return mInstance;
    }

    public static class MovieData
    {
        public String id;
        public String posterPath;
        public String backdropPath;
        public String title;
        public String overview;
        public String releaseDate;
        public String voteAverage;
        public String voteCount;
        public String originalTitle;
        public String duration;
    }

}
