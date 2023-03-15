package com.example.socialcompass.viewmodel;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.socialcompass.model.Location;
import com.example.socialcompass.model.LocationDatabase;
import com.example.socialcompass.model.LocationRepository;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivityViewModel extends AndroidViewModel {
    private final LiveData<List<Location>> locations;
    private MutableLiveData<Pair<Boolean, Long>> connectionInfo;

    private final LocationRepository repo;
    private Timer timer;
    private long startTime = 0;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = LocationDatabase.getSingleton(context);
        var dao = db.locationDao();
        this.repo = new LocationRepository(dao);
        this.locations = repo.getAllLocal();
        this.connectionInfo = new MutableLiveData<Pair<Boolean, Long>>();
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

    public LiveData<Pair<Boolean,Long>> getConnectionInfo() {
        return connectionInfo;
    }

    public void startCheckingInternetConnectivity(ConnectivityManager connectivityManager) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        startTime = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var connected = checkInternetConnectivity(connectivityManager);
                var timeElapsed = calculateElapsedTime();

                if (connected) startTime = System.currentTimeMillis();
                connectionInfo.postValue(new Pair<>(connected, timeElapsed));
            }
        }, 0, 1000);
    }

    private boolean checkInternetConnectivity(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private long calculateElapsedTime() {
        long timeElapsed = System.currentTimeMillis() - startTime;
        return timeElapsed / 60000;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}