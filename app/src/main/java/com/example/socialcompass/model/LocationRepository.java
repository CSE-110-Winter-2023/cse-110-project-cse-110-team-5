package com.example.socialcompass.model;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationRepository {
    private final LocationDao dao;
    private final LocationAPI api;
    private final ScheduledExecutorService getLocationExecutor;

    public LocationRepository(LocationDao dao) {
        this.dao = dao;
        this.api = LocationAPI.provide();
        getLocationExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    // this returns what is known as an observable query
    // this will automatically keep track of changes in
    // the room database everytime we update it
    //
    // All in a background thread - automatic Room feature
    // search up "Write asynchronous DAO queries"
    public LiveData<List<Location>> getAllLocal() {
        return dao.getAllLocationsLive();
    }

    /**
     *  Searches for location object in remote server and returns it as
     *  a location object. Return null if not found.
     *
     * @param publicCode code of friend your searching for
     * @return location if friend with code exists or null otherwise
     */
    public Location getRemote(String publicCode) {
        Location location = null;
        var future = api.getAsync(publicCode);
        try {
            var pair = future.get();
            if (pair != null && pair.first == LocationAPI.SUCCESS_CODE) {
                return Location.fromJSON(pair.second);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void addLocationToDb(Location location) {
        // this will automatically update the live data object
        // in main and call the ui change
        if (!dao.exists(location.publicCode))
            dao.insertLocation(location);
    }

    public void updateLocationInDb(Location location) {
        // this will automatically update the live data object
        // in main and call the ui change
        dao.updateLocation(location);
    }

    public void updateRemote(Location ourlocation) {
        api.updateCoordinates(ourlocation);
    }

    public void pollForUpdates() {
        getLocationExecutor.scheduleAtFixedRate(() -> {
            List<Location> locationsInDatabase = dao.getAllLocations();
            for (Location location: locationsInDatabase) {
                Location remoteLocation = getRemote(location.publicCode);
                dao.updateLocation(remoteLocation);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void pushUserLocation(Location location) {
        api.putAsync(location);
    }

    public void clear() {
        dao.clear();
    }
}
