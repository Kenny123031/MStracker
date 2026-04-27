package com.example.mstracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVDetailsResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("first_air_date")
    private String firstAirDate;
    @SerializedName("episode_run_time")
    private List<Integer> episodeRunTime;
    @SerializedName("vote_average")
    private float voteAverage;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("created_by")
    private List<Creator> createdBy;
    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;
    @SerializedName("genres")
    private List<Genre> genres;
    @SerializedName("production_countries")
    private List<Country> productionCountries;

    public static class Creator {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }

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
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getFirstAirDate() { return firstAirDate; }
    public List<Integer> getEpisodeRunTime() { return episodeRunTime; }
    public float getVoteAverage() { return voteAverage; }
    public String getPosterPath() { return posterPath; }
    public String getOriginalLanguage() { return originalLanguage; }
    public List<Creator> getCreatedBy() { return createdBy; }
    public int getNumberOfEpisodes() { return numberOfEpisodes; }
    public List<Genre> getGenres() { return genres; }
    public List<Country> getProductionCountries() { return productionCountries; }
}
