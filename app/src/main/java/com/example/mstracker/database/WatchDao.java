package com.example.mstracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mstracker.model.WatchItem;

import java.util.List;

@Dao
public interface WatchDao {
    @Query("SELECT * FROM watch_items ORDER BY addedTimestamp DESC")
    List<WatchItem> getAll();

    @Query("SELECT * FROM watch_items WHERE type = :type ORDER BY addedTimestamp DESC")
    List<WatchItem> getByType(String type);

    @Query("SELECT * FROM watch_items WHERE status = 'Watching' ORDER BY addedTimestamp DESC")
    List<WatchItem> getCurrentlyWatching();

    @Query("SELECT * FROM watch_items WHERE status = :status ORDER BY addedTimestamp DESC")
    List<WatchItem> getByStatus(String status);

    @Query("SELECT * FROM watch_items WHERE rating >= 4.0 ORDER BY rating DESC, addedTimestamp DESC LIMIT 5")
    List<WatchItem> getTopPicks();

    @Query("SELECT * FROM watch_items WHERE tmdbId = :tmdbId AND tmdbType = :tmdbType LIMIT 1")
    WatchItem getByTmdbId(int tmdbId, String tmdbType);

    @Insert
    void insert(WatchItem item);

    @Update
    void update(WatchItem item);

    @Delete
    void delete(WatchItem item);
}
