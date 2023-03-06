package com.example.socialcompass.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Location.class}, version = 1)
public abstract class LocationDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();
}
