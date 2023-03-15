package com.example.socialcompass.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDao {

    @Delete
    int deleteLocation(Location location);

    @Update
    int updateLocation(Location location);

    @Insert
    long insertLocation(Location location);

    // if we store our own location in the database,
    // we will need to ignore it. this can be done
    // by ignoring our location within these queries
    // which it currently doesn't do :v
    @Query("SELECT * FROM `locations`")
    List<Location> getAllLocations();

    @Query("SELECT * FROM `locations`")
    LiveData<List<Location>> getAllLocationsLive();

    @Query("SELECT * FROM `locations` WHERE `public_code` = :publicCode")
    Location getLocation(String publicCode);

    @Query("SELECT * FROM `locations` WHERE `public_code` = :publicCode")
    LiveData<Location> getLocationLive(String publicCode);

    @Query("SELECT EXISTS(SELECT 1 FROM `locations` WHERE `public_code` = :publicCode)")
    boolean exists(String publicCode);

    @Query("DELETE FROM `locations`")
    void clear();

    @Query("SELECT COUNT(*) from `locations`")
    int size();

    /*
    @Query("SELECT * FROM `locations` WHERE `public_code` = {our uuid goes here})
    public Location getOurLocation()
    */
}
