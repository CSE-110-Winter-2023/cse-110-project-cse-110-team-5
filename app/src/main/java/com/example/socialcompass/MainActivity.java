/**
 * Main Activity: Basic app functionality. Defines how markers on the main page should behave.
 * Contributors:
 * Emails:
 */
package com.example.socialcompass;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.socialcompass.builders.MarkerBuilder;
import com.example.socialcompass.model.Location;
import com.example.socialcompass.model.LocationAPI;
import com.example.socialcompass.viewmodel.MainActivityViewModel;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Constants
    private static final int NO_LOCATION = -1;
    private static final int ANIMATION_DURATION = 210;
    private static final int MAX_CIRCLE_RADIUS = 615;
    private static final int OUTSIDE_CIRCLE_RADIUS = 670;

    // SharedPreferences keys
    private static final String NAME_KEY = "name";
    private static final String UID_KEY = "uid";
    private static final int RING1_DIST = 1;
    private static final int RING2_DIST = 10;
    private static final int RING3_DIST = 500;
    private static final int RING4_DIST = 12450;

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
    private Hashtable<String, String> markerLabels;
    private MainActivityViewModel viewModel;
    private Location userLocation;
    private LocationAPI locationAPI;
    private ImageView connectionMarker;
    private TextView disconnectionTime;
    static final int MAX_RING_WIDTH_DP = 404;
    static final int MAX_RING_HEIGHT_DP = 388;
    private int ring = 2;

    private void addMarker(Location location) {
        MarkerBuilder builder = new MarkerBuilder(this);
        TextView marker = builder.setText(location.label).getNewMarker();
        markers.put(location.publicCode, marker);
        markerDegrees.put(location.publicCode, 0f);
        markerOffsets.put(location.publicCode, 0f);
        markerDistances.put(location.publicCode, 0f);
        markerLabels.put(location.publicCode, location.label);
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
        checkOverlapLabels();
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

    protected void updateGPSIndicator(Pair<Boolean, Long> status) {
        if (status.first) {
            connectionMarker.setImageDrawable(getResources().getDrawable(R.drawable.circle_green));
            disconnectionTime.setText("");
        } else {
            connectionMarker.setImageDrawable(getResources().getDrawable(R.drawable.circle_red));
            float seconds = status.second / 1000f;
            float minutes = seconds / 60f;
            float hours = minutes / 60f;
            String time = "";
            time += (int)hours != 0 ? (int)hours + "hr " : "";
            time += (int)minutes != 0 ? (int)minutes + "m" : "";
            disconnectionTime.setText(time);
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

        // View initialization
        setContentView(R.layout.activity_main);

        // Instance variable initialization
        preferences = getSharedPreferences("shared", MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        markers = new Hashtable<>();
        markerDegrees = new Hashtable<>();
        markerOffsets = new Hashtable<>();
        markerDistances = new Hashtable<>();
        invisibleLabels = new Hashtable<>();
        markerLabels = new Hashtable<>();
        ring = 2;
        connectionMarker = findViewById(R.id.connectionImageView);
        disconnectionTime = findViewById(R.id.disconnectionTimeTextView);

        // View Model and Observers
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        Observer<List<Location>> locationsObserver = this::updateMarkers;
        Observer<Pair<Boolean, Long>> gpsStatusObserver = this::updateGPSIndicator;
        viewModel.getLocations().observe(this, locationsObserver);

        // Set permissions if not already set
        setPermissions();
        locationService = LocationService.singleton(this);

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
        locationService.startSignalTimer();
        locationService.getGPSStatus().observe(this, gpsStatusObserver);

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
        this.updateRings(0);
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
        int maxDist;
        switch (this.ring) {
            case 1: {
                maxDist = RING1_DIST;
                break;
            }
            case 2: {
                maxDist = RING2_DIST;
                break;
            }
            case 3: {
                maxDist = RING3_DIST;
                break;
            }
            case 4: {
                maxDist = RING4_DIST; // circumference of the earth
                break;
            }
            default: throw new RuntimeException("Impossible.");
        }
        Log.d("DIST TEST", "dist " + distance);
        if (distance >= maxDist) {
            if (!invisibleLabels.containsKey(key)) {
                layoutParams.circleRadius = OUTSIDE_CIRCLE_RADIUS;
                invisibleLabels.put(key, (String)((TextView)markers.get(key)).getText());
                ((TextView)view).setText("⬤");
            }
            return;
        }
        double radiusMultiplier = calculateRadiusMultiplier(distance, this.ring);
        if (invisibleLabels.containsKey(key)) {
            ((TextView)view).setText(invisibleLabels.get(key));
            invisibleLabels.remove(key);
        }
        int finalRadius = (int)( radiusMultiplier * MAX_CIRCLE_RADIUS);
        // Log.d("RADIUS DEBUG", "radius " + finalRadius);
        ValueAnimator anim = ValueAnimator.ofInt(initialRadius, finalRadius);
        anim.addUpdateListener(valueAnimator -> {
            layoutParams.circleRadius = (Integer) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layoutParams.circleRadius = finalRadius;
                view.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private static double calculateRadiusMultiplier(double markerDist, int maxRing) {
        double ringDist;
        int ringLocation;
        if (markerDist <= RING1_DIST) {
            ringDist = markerDist;
            ringLocation = 1;
        } else if (markerDist <= RING2_DIST) {
            ringDist = (markerDist - RING1_DIST) / (RING2_DIST - RING1_DIST);
            ringLocation = 2;
        } else if (markerDist <= RING3_DIST) {
            ringDist = (markerDist - RING2_DIST) / (RING3_DIST - RING2_DIST);
            ringLocation = 3;
        } else {
            ringDist = (markerDist - RING3_DIST) / (RING4_DIST - RING3_DIST);
            ringLocation = 4;
        }

        double multiplierDisplace = ((double) (ringLocation - 1)) / maxRing;
        return multiplierDisplace + (ringDist / maxRing);
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

    public void onMockUrlSaveButtonClicked(View view) {
        locationAPI = new LocationAPI();
        TextView newURL = findViewById(R.id.mockUrl);
        String url = newURL.getText().toString();
        if(url.equals("") || url.equals(null)){
            return;
        }
        locationAPI.ChangeBaseUrl(url);
    }

    public void onMockResetButtonClicked(View view) {
        locationAPI = new LocationAPI();
        locationAPI.ResetBaseUrl();
    }

    private Hashtable<String, TextView> getVisibleMarkers() {
        Hashtable<String, TextView> visibleLabels = new Hashtable<>();
        markers.forEach((k, v) -> {
            if(((TextView)v).getText() != "⬤") {
                visibleLabels.put(k, (TextView)v);
            }
        });
        return visibleLabels;
    }

    private void setOverlappingLabels(Hashtable<String, TextView> visible) {
        Enumeration<String> e1 = visible.keys();
        while (e1.hasMoreElements()) {
            String key1 = e1.nextElement();
            TextView val1 = visible.get(key1);
            boolean overlaps = false;
            Rect rect1 = new Rect();
            val1.getGlobalVisibleRect(rect1);
            Enumeration<String> e2 = visible.keys();
            while (e2.hasMoreElements()) {
                String key2 = e2.nextElement();
                if (key1.equals(key2))
                    continue;
                TextView val2 = visible.get(key2);
                Rect rect2 = new Rect();
                val2.getGlobalVisibleRect(rect2);
                if (rect1.intersect(rect2)) {
                    truncateOverlapLabels(key1, val1, val2);
                    overlaps = true;
                }
            }
            if (!overlaps) {
                val1.setText(markerLabels.get(key1));
            }
        }
    }
    public void checkOverlapLabels() {
        Hashtable<String, TextView> visibleLabels = getVisibleMarkers();
        setOverlappingLabels(visibleLabels);
    }

    public void truncateOverlapLabels(String key1, TextView val1, TextView val2) {
        int[] pos1 = new int[2];
        int[] pos2 = new int[2];
        val1.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        val2.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        val1.getLocationOnScreen(pos1);
        val2.getLocationOnScreen(pos2);
        if (pos1[0] < pos2[0]) {
            String label = markerLabels.get(key1);
            int length = label.length();
            int endIndex = (int)Math.ceil(length * (pos2[0] - pos1[0]) / (float) (val1.getMeasuredWidth() * 2));
            val1.setText(label.substring(0, endIndex));
        }
    }
}

    public void onZoomIn(View view) {
        if (this.ring > 1) {
            this.ring--;
            this.updateRings(this.ring + 1);
            this.updateButtons();
            this.updateAllMarkers();
        }
    }

    public void onZoomOut(View view) {
        if (this.ring < 4) {
            this.ring++;
            this.updateRings(this.ring - 1);
            this.updateButtons();
            this.updateAllMarkers();
        }
    }

    public void updateAllMarkers() {
        for (String markerKey : this.markers.keySet()) {
            this.setMarkerDistance(markerKey);
        }
    }

    public void updateButtons() {
        Button zoomIn = findViewById(R.id.btn_zoom_in);
        zoomIn.setClickable(ring > 1);
        Button zoomOut = findViewById(R.id.btn_zoom_out);
        zoomOut.setClickable(ring < 4);
    }

    public void updateRings(int previousRing) {
        ImageView ring4 = findViewById(R.id.circle);
        ImageView ring3 = findViewById(R.id.circle2);
        ImageView ring2 = findViewById(R.id.circle3);
        ImageView ring1 = findViewById(R.id.circle4);

        var ring4Layout = ring4.getLayoutParams();
        ring4Layout.width = Util.dpToPx(MAX_RING_WIDTH_DP, this);
        ring4Layout.height = Util.dpToPx(MAX_RING_HEIGHT_DP, this);

        updateSingleRing(ring1, 1, previousRing);
        updateSingleRing(ring2, 2, previousRing);
        updateSingleRing(ring3, 3, previousRing);
    }

    public void updateSingleRing(ImageView ring, int ringNum, int previousRing) {
        float oldScale = 0;
        if (previousRing != 0) {
            oldScale = Math.min(1, ((float) ringNum) / previousRing);
        }
        var newScale = Math.min(1, ((float) ringNum) / this.ring);
        var animator = ValueAnimator.ofFloat(oldScale, newScale);
        var params = ring.getLayoutParams();
        animator.addUpdateListener(anim -> {
            var val = (float) anim.getAnimatedValue();
            params.width = Util.dpToPx((int) (val * MAX_RING_WIDTH_DP), this);
            params.height = Util.dpToPx((int) (val * MAX_RING_HEIGHT_DP), this);
            ring.setLayoutParams(params);
        });
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}