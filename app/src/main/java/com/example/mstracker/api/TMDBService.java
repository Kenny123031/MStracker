package com.example.mstracker.api;

import com.example.mstracker.model.MovieDetailsResponse;
import com.example.mstracker.model.TMDBResponse;
import com.example.mstracker.model.TVDetailsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDBService {
    @GET("search/multi")
    Call<TMDBResponse> searchMulti(
        @Query("api_key") String apiKey,
        @Query("query") String query
    );

    @GET("movie/{movie_id}")
    Call<MovieDetailsResponse> getMovieDetails(
        @Path("movie_id") int movieId,
        @Query("api_key") String apiKey
    );

    @GET("tv/{tv_id}")
    Call<TVDetailsResponse> getTVDetails(
        @Path("tv_id") int tvId,
        @Query("api_key") String apiKey
    );

    @GET("trending/all/day")
    Call<TMDBResponse> getTrending(
        @Query("api_key") String apiKey
    );
}
