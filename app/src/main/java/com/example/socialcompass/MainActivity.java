/**
 * Main Activity: Basic app functionality. Defines how markers on the main page should behave.
 * Contributors:
 * Emails:
 */
package com.example.socialcompass;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.socialcompass.builders.MarkerBuilder;
import com.example.socialcompass.model.Location;
import com.example.socialcompass.viewmodel.MainActivityViewModel;

import java.util.Hashtable;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Constants
    private static final int NO_LOCATION = -1;
    private static final int ANIMATION_DURATION = 210;
    private static final int MAX_CIRCLE_RADIUS = 543;

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
    private Hashtable<String, Float> markerDistances;
    private Hashtable<String, View> markers;
    private Hashtable<String, String> invisibleLabels;
    private MainActivityViewModel viewModel;
    private Location userLocation;
    private int zoomScale;

    private void addMarker(Location location) {
        MarkerBuilder builder = new MarkerBuilder(this);
        TextView marker = builder.setText(location.label).getNewMarker();
        markers.put(location.publicCode, marker);
        markerDegrees.put(location.publicCode, 0f);
        markerOffsets.put(location.publicCode, 0f);
        markerDistances.put(location.publicCode, 0f);
    }

    protected void updateMarkers(List<Location> locations) {
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
            updateAngle(key, latitude, longitude);
            updateDistance(key, latitude, longitude);
        }
    }

    protected void updateAngle(String key, double latitude, double longitude) {
            double angle = calculateAngle(latitude, longitude);
            if (angle != NO_LOCATION) {
                markerOffsets.replace(key, (float)angle);
                rotateView(markers.get(key), markerDegrees.get(key), markerDegrees.get(key), markerOffsets.get(key));
            }
    }

    protected void updateDistance(String key, double latitude, double longitude) {
            double distance = calculateDistance(latitude, longitude);
            if (distance != NO_LOCATION) {
                markerDistances.replace(key, (float)distance);
                setMarkerDistance(key);
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
        if (location == null) {
            return NO_LOCATION;
        }
        double devLatitude = Math.toRadians(location.first);
        latitude = Math.toRadians(latitude);
        double devLongitude = location.second;
        double longDiff = Math.toRadians(longitude-devLongitude);
        double y = Math.sin(longDiff)*Math.cos(latitude);
        double x = Math.cos(devLatitude)*Math.sin(latitude)-Math.sin(devLatitude)*Math.cos(latitude)*Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    protected double calculateDistance(double latitude, double longitude) {
        Pair<Double, Double> location = this.locationService.getLocation().getValue();
        if (location == null) {
            return NO_LOCATION;
        }
        double R = 3963.1676; // Radius of the earth in mi
        double devLatitude = location.first;
        double devLongitude = location.second;
        double dLat = Math.toRadians(latitude-devLatitude);  // deg2rad below
        double dLon = Math.toRadians(longitude-devLongitude);
        double angle = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(devLatitude)) *
                Math.cos(Math.toRadians(latitude)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));
        return R * c; // Distance in mi
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
        markers = new Hashtable<>();
        markerDegrees = new Hashtable<>();
        markerOffsets = new Hashtable<>();
        markerDistances = new Hashtable<>();
        invisibleLabels = new Hashtable<>();
        zoomScale = 10;


        // View Model and Observers
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        Observer<List<Location>> locationsObserver = this::updateMarkers;
        viewModel.getLocations().observe(this, locationsObserver);

        // Set permissions if not already set
        setPermissions();
        locationService = LocationService.singleton(this);

        // View initialization
        setContentView(R.layout.activity_main);
        // check if name has been saved
        String nameCheck = preferences.getString(NAME_KEY, null);
        if (nameCheck == null) {
            SharedPreferences.Editor editor = preferences.edit();
            Util.showNamePrompt(this, this, editor); // prompt to enter name
        }


        locationService.getLocation().observe(this, location -> {
            String uid = preferences.getString(UID_KEY, null);
            String name = preferences.getString(NAME_KEY, null);
            if (name != null) {
                updateMarkers(viewModel.getLocations().getValue());
                double latitude = location.first;
                double longitude = location.second;
                this.userLocation = new Location(uid, uid, name, (float) latitude, (float) longitude, false, 0, 0);
                viewModel.pushLocation(this.userLocation);
            }
        });
        // -------------------------------------------------------------------------------------- //
        //                                     MS2 Stuff Below                                    //
        // -------------------------------------------------------------------------------------- //

        // assign viewModel to safely hold MainActivity state outside it's lifecycle

        // create an Observer which updates the UI whenever the list of locations changes
        // which should happen everytime we update our local database because
        // we are using something known as observable queries
        //
        // search up "Write asynchronous DAO queries" and "LiveData" and "ViewModel" if
        // you haven't already

    }

    public void onAddFriendButtonClick(View view) {
        Intent intent = new Intent(this, AddFriendActivity.class);
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
     * @param degreeOffset: The angle of the coordinates of the view with respect to 0 degrees
     */
    private void rotateView(View view, float startAngle, float endAngle, float degreeOffset) {
        // Change circle constraint angle
        ValueAnimator anim = ValueAnimator.ofFloat(startAngle, endAngle);
        anim.addUpdateListener(valueAnimator -> {
            float val = (Float) valueAnimator.getAnimatedValue();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.circleAngle = val + degreeOffset;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private void setMarkerDistance(String key) {
        if (!markerDistances.containsKey(key))
            return;
        double distance = this.markerDistances.get(key);
        View view = markers.get(key);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)view.getLayoutParams();
        int initialRadius = layoutParams.circleRadius;
        double radiusMultiplier = (distance / this.zoomScale);
        if (radiusMultiplier >= 1) {
            if (!invisibleLabels.containsKey(key)) {
                layoutParams.circleRadius = MAX_CIRCLE_RADIUS;
                invisibleLabels.put(key, (String)((TextView)markers.get(key)).getText());
                ((TextView)view).setText("⬤");
            }
            return;
        }
        if (invisibleLabels.containsKey(key)) {
            ((TextView)view).setText(invisibleLabels.get(key));
            invisibleLabels.remove(key);
        }
        int finalRadius = (int)( radiusMultiplier * MAX_CIRCLE_RADIUS);
        ValueAnimator anim = ValueAnimator.ofInt(initialRadius, finalRadius);
        anim.addUpdateListener(valueAnimator -> {
            layoutParams.circleRadius = (Integer) valueAnimator.getAnimatedValue();
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
