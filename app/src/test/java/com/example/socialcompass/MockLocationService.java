package com.example.socialcompass;

import android.app.Activity;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MockLocationService extends LocationService{
    private final MutableLiveData<Pair<Double, Double>> location;

    protected MockLocationService (Activity activity) {
        super(activity);
        this.location = new MutableLiveData<>();
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
