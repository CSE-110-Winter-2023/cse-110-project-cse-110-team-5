package com.example.socialcompass.builders;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;

import com.example.socialcompass.LocationService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainActivityBuilder {
    private SharedPreferences preferences;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private Hashtable<String, View> markers;
    private final Hashtable<String, Float> markerDegrees;
    private final Hashtable<String, Float> markerOffsets;
    private LocationService locationService;

    public MainActivityBuilder(Context context) {
        preferences = context.getSharedPreferences("shared", MODE_PRIVATE);
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        markers = new Hashtable<>();
        markerDegrees = new Hashtable<>();
        markerOffsets = new Hashtable<>();
        locationService = LocationService.singleton(context);
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setMagneticFieldSensor(Sensor sensor) {
        this.magneticFieldSensor = sensor;
    }

    public void setMarkers(Hashtable<String, View> markers) {
        this.markers = markers;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public List<Object> getConfig() {
        ArrayList<Object> output = new ArrayList<>();
        output.add(this.preferences);
        output.add(this.sensorManager);
        output.add(this.magneticFieldSensor);
        output.add(this.markers);
        output.add(this.markerDegrees);
        output.add(this.markerOffsets);
        output.add(this.locationService);
        return output;
    }
}
