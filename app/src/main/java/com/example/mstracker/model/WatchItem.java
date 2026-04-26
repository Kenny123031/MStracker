package com.example.mstracker.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "watch_items")
public class WatchItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String type; // Movie / Series
    private String status; // Watching / Completed / Plan to Watch
    private float rating;
    private String posterPath;
    private String year;
    private String genre;
    private long addedTimestamp;
    private long completedDate;
    private int tmdbId;
    private String tmdbType; // "movie" or "tv"

    public WatchItem(String title, String type, String status, float rating, String posterPath, String year) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.rating = rating;
        this.posterPath = posterPath;
        this.year = year;
        this.addedTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }

    public long getCompletedDate() { return completedDate; }
    public void setCompletedDate(long completedDate) { this.completedDate = completedDate; }

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }
    public String getTmdbType() { return tmdbType; }
    public void setTmdbType(String tmdbType) { this.tmdbType = tmdbType; }
}
