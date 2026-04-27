package com.example.mstracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieDetailsResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("overview")
    private String overview;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("runtime")
    private int runtime;
    @SerializedName("vote_average")
    private float voteAverage;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("genres")
    private List<Genre> genres;
    @SerializedName("production_countries")
    private List<Country> productionCountries;

    public static class Genre {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }

    public static class Country {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public int getRuntime() { return runtime; }
    public float getVoteAverage() { return voteAverage; }
    public String getPosterPath() { return posterPath; }
    public String getOriginalLanguage() { return originalLanguage; }
    public List<Genre> getGenres() { return genres; }
    public List<Country> getProductionCountries() { return productionCountries; }
}
