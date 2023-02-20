/**
 * Main Activity: Basic app functionality. Defines how markers on the main page should behave.
 * Contributors:
 * Emails:
 */
package com.example.socialcompass;

import static com.example.socialcompass.LocationEntryActivity.UI_DEGREES;
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

    /**
     * Sets the angles of all the markers according to their coordinates and the device orientation
     * @param locationService: the LocationService of the device, or a mock object
     */
    protected void setMarkerAngles(LocationService locationService) {
        for (int i = 1; i < NUM_MARKERS; i++) {
            String latKey = KEYS[i][0];
            String longKey = KEYS[i][1];
            if (preferences.contains(latKey) && preferences.contains(longKey)) {
                double markerLongitude = Double.parseDouble(Util.getFloatAsString(preferences, longKey));
                double markerLatitude = Double.parseDouble(Util.getFloatAsString(preferences, latKey));
                double angle = calculateAngle(locationService, markerLatitude, markerLongitude);
                if (angle == NO_LOCATION) {
                    markers[i].setVisibility(View.GONE);
                }
                else {
                    initialDegrees[i] = (float)angle;
                    rotateView(markers[i], currentDegrees[i], currentDegrees[i], initialDegrees[i]);
                    markers[i].setVisibility(View.VISIBLE);
                }
            }
            else {
                markers[i].setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets the labels of each marker according to the entry in LocationEntryActivity
     */
    protected void setMarkerLabels() {
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
        if (preferences.contains(UI_DEGREES)) {
            float orientation = preferences.getFloat(UI_DEGREES, 0f);
            updateOrientation(orientation);
        }
    }

    /**
     * Calculates the angle between the device and a given set of coordinates
     * @param locationService: The LocationService for the device, or a mock object
     * @param latitude: The latitude of the target coordinates
     * @param longitude: The longitude of the target coordinates
     * @return The angle between the device and the target coordinates as a double
     */
    protected double calculateAngle(LocationService locationService, double latitude, double longitude) {
        Pair<Double, Double> location = locationService.getLocation().getValue();
        if (location != null) {
            double devLatitude = Math.toRadians(location.first);
            latitude = Math.toRadians(latitude);
            double devLongitude = location.second;
            double longDiff = Math.toRadians(longitude-devLongitude);
            double y = Math.sin(longDiff)*Math.cos(latitude);
            double x = Math.cos(devLatitude)*Math.sin(latitude)-Math.sin(devLatitude)*Math.cos(latitude)*Math.cos(longDiff);
            return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        }
        return NO_LOCATION;
    }

    /**
     * Asks user to grant location permissions to the app
     */
    private void setPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    /**
     * App behavior when launched
     * @param savedInstanceState: The saved state of the app
     */
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
        locationService.getLocation().observe(this, location -> setMarkerAngles(locationService));

        // View initialization
        setContentView(R.layout.activity_main);
        markers[0] = findViewById(R.id.arrow);
        markers[1] = findViewById(R.id.familyHouse);

        // Set permissions if not already set
        setPermissions();

        // Set initial angles and labels for all markers
        setMarkerAngles(locationService);
        setMarkerLabels();
    }

    /**
     * Behavior of Location button
     * @param view: The ButtonView that triggered the method
     */
    public void onLocationsButtonClick(View view) {
        Intent intent = new Intent(this, LocationEntryActivity.class);
        startActivity(intent);
    }

    /**
     * App behavior when resumed
     */
    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Set all markers
        setMarkerAngles(locationService);
        setMarkerLabels();
    }

    /**
     * App behavior when paused
     */
    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Rotates a given View according to orientation and location
     * @param view: The view to rotate
     * @param startAngle: The current angle of the view
     * @param endAngle: The ending angle of the view
     * @param initialDegree: The angle of the coordinates of the view with respect to 0 degrees
     */
    private void rotateView(View view, float startAngle, float endAngle, float initialDegree) {
        // Change circle constraint angle
        ValueAnimator anim = ValueAnimator.ofFloat(startAngle, endAngle);
        anim.addUpdateListener(valueAnimator -> {
            float val = (Float) valueAnimator.getAnimatedValue();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.circleAngle = val + initialDegree;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(new LinearInterpolator());
        // Change marker orientation
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

    /**
     * App behavior when there is a change in device orientation
     * @param sensorEvent: the change in device orientation
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = (float) Math.toDegrees(Math.atan2(sensorEvent.values[0], sensorEvent.values[1]));
            // check if ui mock is on
            var preferences = getSharedPreferences("shared", MODE_PRIVATE);
            if (preferences.contains(UI_DEGREES)) {
                return;
            }
            updateOrientation(degree);
        }
    }

    /**
     * Updates the orientation of each marker
     * @param degree: how much to rotate each marker
     */
    private void updateOrientation(float degree) {
        for (int i = 0; i < NUM_MARKERS; i++) {
            rotateView(markers[i], currentDegrees[i], degree, initialDegrees[i]);
            currentDegrees[i] = degree;
        }
    }

    /**
     * Partner method to onLocationChange
     * @param sensor: sensor that changed
     * @param i: how much the sensor changed
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //nothing
    }
}
