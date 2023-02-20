package com.example.socialcompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Pair;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Constants
    private static final int NUM_MARKERS = 2;
    private static final int NO_LOCATION = -1;
    private static final int ANIMATION_DURATION = 210;

    // SharedPreferences keys
    private static final String [] FAMILY_KEYS = {"familyLatitude", "familyLongitude", "familyLabel", "Parents"};
    private static final String [][] KEYS = {{}, FAMILY_KEYS};

    // Instance variables
    private LocationService locationService;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private SharedPreferences preferences;
    private float [] currentDegrees;
    private float [] initialDegrees;
    private View [] markers;

    private void setMarkerAngles() {
        for (int i = 1; i < NUM_MARKERS; i++) {
            String latKey = KEYS[i][0];
            String longKey = KEYS[i][1];
            if (preferences.contains(latKey) && preferences.contains(longKey)) {
                double markerLongitude = Double.parseDouble(Util.getFloatAsString(preferences, longKey));
                double markerLatitude = Double.parseDouble(Util.getFloatAsString(preferences, latKey));
                double angle = calculateAngle(locationService, markerLatitude, markerLongitude);
                if (angle == NO_LOCATION) {
                    markers[i].setVisibility(View.INVISIBLE);
                }
                else {
                    initialDegrees[i] = (float)angle;
                    markers[i].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setMarkerLabels() {
        for (int i = 1; i < NUM_MARKERS; i++) {
            String labelKey = KEYS[i][2];
            if (preferences.contains(labelKey)) {
                String label = preferences.getString(labelKey, null);
                if (label == null || label.equals("")) {
                    ((TextView) markers[i]).setText(KEYS[i][3]);
                    continue;
                }
                ((TextView) markers[i]).setText(label);
            }
        }
    }

    /*
     * Takes in latitude and longitude in DEGREES
     */
    public double calculateAngle(LocationService locationService, double latitude2, double longitude2) {
        Pair<Double, Double> location = locationService.getLocation().getValue();
        if (location != null) {
            double latitude = Math.toRadians(location.first);
            latitude2 = Math.toRadians(latitude2);
            double longitude = location.second;
            double longDiff = Math.toRadians(longitude2-longitude);
            double y = Math.sin(longDiff)*Math.cos(latitude2);
            double x = Math.cos(latitude)*Math.sin(latitude2)-Math.sin(latitude)*Math.cos(latitude2)*Math.cos(longDiff);
            return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        }
        return NO_LOCATION;
    }

    private void setPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instance variable initialization
        preferences = getSharedPreferences("shared", MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        currentDegrees = new float[NUM_MARKERS];
        initialDegrees = new float[NUM_MARKERS];
        markers = new View[NUM_MARKERS];
        // Set permissions if not already set
        setPermissions();
        locationService = LocationService.singleton(this);
        locationService.getLocation().observe(this, location -> setMarkerAngles());

        // View initialization
        setContentView(R.layout.activity_main);
        markers[0] = findViewById(R.id.arrow);
        markers[1] = findViewById(R.id.familyHouse);

        // Set permissions if not already set
        setPermissions();

        // Set initial angles and labels for all markers
        setMarkerAngles();
        setMarkerLabels();
    }

    public void onLocationsButtonClick(View view) {
        Intent intent = new Intent(this, LocationEntryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Set all markers
        setMarkerAngles();
        setMarkerLabels();
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void rotateView(View view, float startAngle, float endAngle, float initialDegree) {
        ValueAnimator anim = ValueAnimator.ofFloat(startAngle, endAngle);
        anim.addUpdateListener(valueAnimator -> {
            float val = (Float) valueAnimator.getAnimatedValue();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.circleAngle = val + initialDegree;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(new LinearInterpolator());
        RotateAnimation ra = new RotateAnimation(
                startAngle,
                endAngle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        ra.setDuration(ANIMATION_DURATION);
        ra.setFillAfter(true);
        anim.start();
        view.startAnimation(ra);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = -1f * (float) Math.toDegrees(Math.atan2(sensorEvent.values[0], sensorEvent.values[1]));
            for (int i = 0; i < NUM_MARKERS; i++) {
                rotateView(markers[i], currentDegrees[i], -degree, initialDegrees[i]);
                currentDegrees[i] = -degree;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //nothing
    }
}
