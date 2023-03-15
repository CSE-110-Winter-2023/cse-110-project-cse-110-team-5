/**
 * Main Activity: Basic app functionality. Defines how markers on the main page should behave.
 * Contributors:
 * Emails:
 */
package com.example.socialcompass;

import static com.example.socialcompass.AddFriendActivity.UI_DEGREES;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.socialcompass.builders.MarkerBuilder;
import com.example.socialcompass.model.Location;
import com.example.socialcompass.viewmodel.MainActivityViewModel;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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
    private int zoomScale; // in miles
    private Bitmap circleBitMap;

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

    // update the distance of the corresponding marker
    // with its new long + lat
    // then update the UI component
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
        var imgSource = ImageDecoder.createSource(
                getResources(),
                R.drawable.circle_removebg_preview
        );

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

        // lazy init cuz i cant call decodeBitmap()
        // on main thread
        // hacky as fuck but idc anymore -o-
        CompletableFuture.supplyAsync(() -> { // fork join pool
                    try {
                        return ImageDecoder.decodeBitmap(imgSource);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAcceptAsync(bitmap -> {
                    this.circleBitMap = bitmap;
                    Log.d("BITMAP LOAD", "is not null: " + (bitmap != null));
                    this.updateCircles(false);
                }, Runnable::run); // run on main thread


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
        /* ImageView circ = findViewById(R.id.circle);
        ImageView circ2 = findViewById(R.id.circle2);
        ImageView circ3 = findViewById(R.id.circle3);
        circ.setVisibility(View.INVISIBLE);
        circ2.setVisibility(View.INVISIBLE);
        circ3.setVisibility(View.INVISIBLE); */
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

    /**
     * Update the UI component of the marker
     * corresponding to the given key using internal
     * state of the activity
     * @param key the public code of the marker
     */
    private void setMarkerDistance(String key) {
        if (!markerDistances.containsKey(key))
            return;
        double distance = this.markerDistances.get(key);
        View view = markers.get(key);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)view.getLayoutParams();
        int initialRadius = layoutParams.circleRadius;
        double radiusMultiplier = (distance / this.zoomScale);
        // if marker distance > our max zoom scale
        // indicate that marker is too far away to be displayed
        if (radiusMultiplier >= 1) {
            if (!invisibleLabels.containsKey(key)) {
                layoutParams.circleRadius = MAX_CIRCLE_RADIUS;
                invisibleLabels.put(key, (String)((TextView)markers.get(key)).getText());
                ((TextView)view).setText("⬤");
            }
            return;
        }
        // if it's invisible set it to invisible
        if (invisibleLabels.containsKey(key)) {
            ((TextView)view).setText(invisibleLabels.get(key));
            invisibleLabels.remove(key);
        }
        // animate movement of the marker to new
        // radius in circle constraint
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

    private static final int BITMAP_MAX_SIZE = 1300;

    public void onZoomInClick(View view) {
        this.zoomScale = Math.max(1, this.zoomScale - getZoomInStep(this.zoomScale));
        this.updateZoomButtons();
        this.updateCircles(true);
        this.updateAllMarkers();
    }

    public void onZoomOutClick(View view) {
        this.zoomScale = this.zoomScale + getZoomOutStep(this.zoomScale);
        this.updateZoomButtons();
        this.updateCircles(true);
        this.updateAllMarkers();
    }

    public static int getZoomOutStep(int currZoomScale) {
        if (currZoomScale < 10) {
            return 1;
        } else if (currZoomScale < 100) {
            return 10;
        } else if (currZoomScale < 500) {
            return 25;
        } else {
            return 50;
        }
    }

    public static int getZoomInStep(int currZoomScale) {
        if (currZoomScale <= 10) {
            return 1;
        } else if (currZoomScale <= 100) {
            return 10;
        } else if (currZoomScale <= 500) {
            return 25;
        } else {
            return 50;
        }
    }

    private void updateZoomButtons() {
        Button btnZoomIn = findViewById(R.id.btn_zoom_in);
        btnZoomIn.setClickable(this.zoomScale > 1);
        // Button btnZoomOut = findViewById(R.id.btn_zoom_out);
        // btnZoomOut.setClickable(this.zoomScale < 500);
    }

    public void updateAllMarkers() {
        for (String key : this.markers.keySet()) {
            this.setMarkerDistance(key);
        }
    }

    public void updateCircles(boolean animate) {
        this.setTitle("zoom scale: " + this.zoomScale);
        ImageView circ = findViewById(R.id.circle); // 500-inf
        ImageView circ2 = findViewById(R.id.circle2); // 10-500
        ImageView circ3 = findViewById(R.id.circle3); // 1-10
        ImageView circ4 = findViewById(R.id.circle4); // 0-1
        var circ1BitMap = Bitmap.createScaledBitmap(this.circleBitMap, BITMAP_MAX_SIZE, BITMAP_MAX_SIZE, true);
        circ.setImageBitmap(circ1BitMap);
        updateCircleBitmap(circ2, 500, animate);
        updateCircleBitmap(circ3, 10, animate);
        updateCircleBitmap(circ4, 1, animate);
    }

    public void updateCircleBitmap(ImageView circ, int maxDist, boolean animate) {
        var scalingFactor = ((double) maxDist) / this.zoomScale;
        scalingFactor = Util.clamp(scalingFactor, 0, 1);
        var scaledSize = (int) (scalingFactor * BITMAP_MAX_SIZE);
        if (scaledSize <= 10) {
            circ.setVisibility(View.INVISIBLE);
        } else {
            circ.setVisibility(View.VISIBLE);
            var currBitmap = ((BitmapDrawable)circ.getDrawable()).getBitmap();
            var oldSize = currBitmap.getWidth();
            if (animate) {
                var animator = ValueAnimator.ofInt(oldSize, scaledSize);
                animator.addUpdateListener(anim -> {
                    var val = (int) anim.getAnimatedValue();
                    var scaledBitmap = Bitmap.createScaledBitmap(this.circleBitMap, val, val, true);
                    circ.setImageBitmap(scaledBitmap);
                });
                animator.setDuration(700);
                animator.setInterpolator(new LinearInterpolator());
                animator.start();
            } else {
                var scaledBitmap = Bitmap.createScaledBitmap(this.circleBitMap, scaledSize, scaledSize, true);
                circ.setImageBitmap(scaledBitmap);
            }
        }
    }
}
