package com.example.mstracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.mstracker.model.WatchItem;

@Database(entities = {WatchItem.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract WatchDao watchDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "mstracker_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
