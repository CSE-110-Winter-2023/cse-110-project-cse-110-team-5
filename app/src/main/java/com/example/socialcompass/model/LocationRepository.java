package com.example.socialcompass.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationRepository {
    private LocationDao dao;
    //private LocationAPI api;
    private ScheduledExecutorService executor;

    public LocationRepository(LocationDao dao) {
        this.dao = dao;
        //this.api = LocationAPI.provide();
        executor = Executors.newSingleThreadScheduledExecutor();
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

    public Location getRemote(String publicCode) {
        /* need api here
        Location location = null;
        var pair = api.getLocation(publicCode) // or something like this
        if (pair.first == Location.SUCCESS_CODE) {
           location = Location.fromJson(pair.second); // or something like this; check in Locations.java plz
        }
        return location;
        */
        return null;
    }

    public void addLocation(Location location) {
        // this will automatically update the live data object
        // in main and call the ui change
        // dao.insertLocation(location) // check
    }

    public void updateRemote(Location location) {
        // api.updateLocationCoordinates(location) // check
    }

    public void pollForUpdates() {
        executor.scheduleAtFixedRate(() -> {
            List<Location> locationsInDatabase = dao.getAllLocations();
            for (Location location: locationsInDatabase) {
                Location remoteLocation = getRemote(location.publicCode);
                dao.updateLocation(remoteLocation);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
}
