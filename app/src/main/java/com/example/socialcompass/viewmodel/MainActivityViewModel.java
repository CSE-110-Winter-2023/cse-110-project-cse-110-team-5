package com.example.socialcompass.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.socialcompass.model.Location;
import com.example.socialcompass.model.LocationDatabase;
import com.example.socialcompass.model.LocationRepository;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    private final LiveData<List<Location>> locations;
    private final LocationRepository repo;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = LocationDatabase.getSingleton(context);
        var dao = db.locationDao();
        this.repo = new LocationRepository(dao);
        this.locations = repo.getAllLocal();
        repo.pollForUpdates();
    }

    /**
     * Method to get all the friend locations we have in our database
     * live data getValue() may return null if database is empty
     */
    public LiveData<List<Location>> getLocations() {
        return locations;
    }

    public void pushLocation(@NonNull Location location) {
        repo.pushUserLocation(location);
    }
}
