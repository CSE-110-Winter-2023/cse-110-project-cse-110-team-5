package com.example.socialcompass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * An implementation of a location service using the singleton design pattern.
 * Handles location permission checking and updating.
 */
public class LocationService implements LocationListener {
    private static LocationService instance;
    private MutableLiveData<Pair<Double, Double>> locationValue;
    private final LocationManager locationManager;

    /**
     * Constructor
     *
     * Ensures only one instance of locationManager is ever present
     * by controlling the LocationService constructor call
     * @param context The context of the app when singleton is called
     */
    public static LocationService singleton(Context context) {
        if (instance == null) {
            instance = new LocationService(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Constructor
     *
     * Called in singleton method
     * @param context The activity of the app when LocationService is called
     */
    protected LocationService (Context context) {
        this.locationValue = new MutableLiveData<>();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Register sensor listeners
        this.registerLocationListener(context);
    }


    /**
     *  Check for location permissions before registering location manager.
     */
    private void registerLocationListener(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new IllegalStateException("App needs location permission to get latest location");
        }

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.locationValue.postValue(new Pair<>(location.getLatitude(),
                location.getLongitude()));
    }

    private void unregisterLocationListener() { locationManager.removeUpdates(this);}

    public LiveData<Pair<Double, Double>> getLocation() { return this.locationValue;}

    public void setMockOrientationSource(MutableLiveData<Pair<Double, Double>> mockDataSource) {
        unregisterLocationListener();
        this.locationValue = mockDataSource;
    }
}
