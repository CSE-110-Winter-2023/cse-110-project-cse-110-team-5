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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
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

import com.example.socialcompass.model.Location;
import com.example.socialcompass.viewmodel.MainActivityViewModel;
import com.example.socialcompass.builders.MarkerBuilder;

import java.util.List;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Constants
    private static final int NO_LOCATION = -1;
    private static final int ANIMATION_DURATION = 210;

    // SharedPreferences keys
    private static final String NAME_KEY = "name";
    private static final String UID_KEY = "uid";

    // Instance variables
    private LocationService locationService;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private SharedPreferences preferences;
    private Hashtable<String, Float> markerDegrees;
    private Hashtable<String, Float> markerOffsets;
    private Hashtable<String, View> markers;
    private MainActivityViewModel viewModel;


    private void addMarker(Location location) {
        MarkerBuilder builder = new MarkerBuilder(this);
        TextView marker = builder.setText(location.label).getNewMarker();
        markers.put(location.publicCode, marker);
        markerDegrees.put(location.publicCode, 0f);
        markerOffsets.put(location.publicCode, 0f);
    }

    protected void updateAngles(List<Location> locations) {
        if (locations == null) {
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            Location currLocation = locations.get(i);
            String key = currLocation.publicCode;
            if (!markers.containsKey(key)) {
                addMarker(currLocation);
            }
            double latitude = currLocation.latitude;
            double longitude = currLocation.longitude;
            double angle = calculateAngle(latitude, longitude);
            if (angle != NO_LOCATION) {
                markerOffsets.replace(key, (float)angle);
                rotateView(markers.get(key), markerDegrees.get(key), markerDegrees.get(key), markerOffsets.get(key));
                markers.get(key).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Calculates the angle between the device and a given set of coordinates
     * @param latitude: The latitude of the target coordinates
     * @param longitude: The longitude of the target coordinates
     * @return The angle between the device and the target coordinates as a double
     */
    protected double calculateAngle(double latitude, double longitude) {
        Pair<Double, Double> location = this.locationService.getLocation().getValue();
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
        // MainActivityBuilder mainBuilder = new MainActivityBuilder(this);

        // Instance variable initialization
        preferences = getSharedPreferences("shared", MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        markers = new Hashtable<String, View>();
        markerDegrees = new Hashtable<String, Float>();
        markerOffsets = new Hashtable<String, Float>();

        // Set permissions if not already set
        setPermissions();
        locationService = LocationService.singleton(this);
        locationService.getLocation().observe(this, location -> updateAngles(viewModel.getLocations().getValue()));

        // View initialization
        setContentView(R.layout.activity_main);

        // check if name has been saved
        String name = preferences.getString(NAME_KEY, null);
        if (name == null) {
            SharedPreferences.Editor editor = preferences.edit();
            Util.showNamePrompt(this, this, editor); // prompt to enter name
        }

        // -------------------------------------------------------------------------------------- //
        //                                     MS2 Stuff Below                                    //
        // -------------------------------------------------------------------------------------- //

        // assign viewModel to safely hold MainActivity state outside it's lifecycle
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // create an Observer which updates the UI whenever the list of locations changes
        // which should happen everytime we update our local database because
        // we are using something known as observable queries
        //
        // search up "Write asynchronous DAO queries" and "LiveData" and "ViewModel" if
        // you haven't already
        Observer<List<Location>> locationsObserver = this::updateAngles;
        viewModel.getLocations().observe(this, locationsObserver);
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
        anim.start();
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
        if (markers.size() == 0) {
            return;
        }
        markers.forEach((k, v) -> {
            rotateView(v, markerDegrees.get(k), degree, markerOffsets.get(k));
            markerDegrees.replace(k, degree);
        });
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
