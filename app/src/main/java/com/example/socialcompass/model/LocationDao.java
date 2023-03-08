package com.example.socialcompass.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.DeleteTable;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDao {

    @Delete
    public int deleteLocation(Location location);

    @Update
    public int updateLocation(Location location);

    @Insert
    public long insertLocation(Location location);

    // if we store our own location in the database,
    // we will need to ignore it. this can be done
    // by ignoring our location within these queries
    // which it currently doesn't do :v
    @Query("SELECT * FROM `locations`")
    public List<Location> getAllLocations();

    @Query("SELECT * FROM `locations`")
    public LiveData<List<Location>> getAllLocationsLive();

    @Query("SELECT * FROM `locations` WHERE `public_code` = :publicCode")
    public Location getLocation(String publicCode);

    @Query("SELECT * FROM `locations` WHERE `public_code` = :publicCode")
    public LiveData<Location> getLocationLive(String publicCode);

    @Query("SELECT EXISTS(SELECT 1 FROM `locations` WHERE `public_code` = :publicCode)")
    public boolean exists(String publicCode);

    @Query("DELETE FROM `locations`")
    public void clear();

    @Query("SELECT COUNT(*) from `locations`")
    public int size();

    /*
    @Query("SELECT * FROM `locations` WHERE `public_code` = {our uuid goes here})
    public Location getOurLocation()
    */
}
