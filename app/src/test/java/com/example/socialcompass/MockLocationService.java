package com.example.socialcompass;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MockLocationService extends LocationService{
    private MutableLiveData<Pair<Double, Double>> location;

    protected MockLocationService (Activity activity) {
        super(activity);
        this.location = new MutableLiveData<Pair<Double, Double>>();
        this.setLocation(0d, 0d);
    }

    @Override
    public LiveData<Pair<Double, Double>> getLocation() {
        return this.location;
    }

    public void setLocation(double latitude, double longitude) {
        this.location.setValue(new Pair<>(latitude, longitude));
    }
}
