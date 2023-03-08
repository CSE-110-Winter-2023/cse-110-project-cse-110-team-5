package com.example.socialcompass.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Location.class}, version = 1)
public abstract class LocationDatabase extends RoomDatabase {
    private static LocationDatabase singleton = null;
    public abstract LocationDao locationDao();

    public synchronized static LocationDatabase getSingleton(Context context) {
        if (singleton == null) {
            singleton = LocationDatabase.makeDatabase(context);
        }
        return singleton;
    }

    @VisibleForTesting
    public static void injectTestDatabase(LocationDatabase testDatabase) {
        if (singleton != null) {
            singleton.close();
        }

        singleton = testDatabase;
    }

    private static LocationDatabase makeDatabase(Context context) {
        return Room.databaseBuilder(context, LocationDatabase.class, "locations.db")
                .allowMainThreadQueries().build();
    }
}
