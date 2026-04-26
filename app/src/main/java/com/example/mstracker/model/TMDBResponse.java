package com.example.mstracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TMDBResponse {
    @SerializedName("results")
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public static class Movie {
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;
        @SerializedName("name")
        private String name; // For TV shows
        @SerializedName("poster_path")
        private String posterPath;
        @SerializedName("release_date")
        private String releaseDate;
        @SerializedName("first_air_date")
        private String firstAirDate; // For TV shows
        @SerializedName("media_type")
        private String mediaType;
        @SerializedName("vote_average")
        private float voteAverage;
        @SerializedName("overview")
        private String overview;
        @SerializedName("original_language")
        private String originalLanguage;

        public int getId() { return id; }
        public String getTitle() { return title != null ? title : name; }
        public String getPosterPath() { return posterPath; }
        public String getReleaseDate() { return releaseDate != null ? releaseDate : firstAirDate; }
        public String getMediaType() { return mediaType; }
        public float getVoteAverage() { return voteAverage; }
        public String getOverview() { return overview; }
        public String getOriginalLanguage() { return originalLanguage; }
    }
}
