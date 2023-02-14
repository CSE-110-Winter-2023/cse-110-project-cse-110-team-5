package com.example.socialcompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;


import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.util.DisplayMetrics;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final int NO_FAMILY_LOCATION = -1;
    public static final String FAMILY_LONGITUDE = "familyLongitude";
    public static final String FAMILY_LATITUDE = "familyLatitude";
    public static final String YOU_LONGITUDE = "youLongitude";
    public static final String YOU_LATITUDE = "youLatitude";
    private ImageView arrowImage;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private SharedPreferences preferences;
    private float [] currentDegree = {0f, 0f};
    private float [] initialDegree = {0f, 0f};
    private int ANGLE = 1;
    private TextView familyHouse;
    double angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrowImage = findViewById(R.id.arrow);
        familyHouse = findViewById(R.id.familyHouse);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        LocationService locationService = LocationService.singleton(this);
        preferences = getSharedPreferences("shared", MODE_PRIVATE);
        // Set family home location/angle
        if (preferences.contains(FAMILY_LONGITUDE) && preferences.contains(FAMILY_LATITUDE)) {
            double famLongitude = Double.parseDouble(Util.getFloatAsString(preferences, FAMILY_LONGITUDE));
            double famLatitude = Math.toRadians(Double.parseDouble(Util.getFloatAsString(preferences, FAMILY_LATITUDE)));
            angle = calculateAngle(locationService, famLatitude, famLongitude);
            if (angle == NO_FAMILY_LOCATION) {
                familyHouse = findViewById(R.id.familyHouse);
                familyHouse.setVisibility(View.INVISIBLE);
            }
            else {
                initialDegree[1] = (float)angle;
                familyHouse = findViewById(R.id.familyHouse);
                familyHouse.setVisibility(View.VISIBLE);
            }
        }
    }

    public double calculateAngle(LocationService locationService, double latitude2, double longitude2) {
        Pair<Double, Double> location = locationService.getLocation().getValue();
        if (location != null) {
            double latitude = Math.toRadians(location.first);
            double longitude = Math.toRadians(location.second);
            double longDiff = Math.toRadians(longitude2-longitude);
            double y = Math.sin(longDiff)*Math.cos(latitude2);
            double x = Math.cos(latitude)*Math.sin(latitude2)-Math.sin(latitude)*Math.cos(latitude2)*Math.cos(longDiff);
            double angle = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
            return angle;
        }
        return -1;
    }

    public void onLocationsButtonClick(View view) {
        Intent intent = new Intent(this, LocationEntryActivity.class);
        startActivity(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//        // check if the request code is same as what is passed
//        if(requestCode==1) {
//            angle = data.getDoubleExtra(String.valueOf(ANGLE), 0f);
//        }
//    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        LocationService locationService = LocationService.singleton(this);
        preferences = getSharedPreferences("shared", MODE_PRIVATE);
        // Set family home location/angle
        if (preferences.contains(FAMILY_LONGITUDE) && preferences.contains(FAMILY_LATITUDE)) {
            double famLongitude = Double.parseDouble(Util.getFloatAsString(preferences, FAMILY_LONGITUDE));
            double famLatitude = Math.toRadians(Double.parseDouble(Util.getFloatAsString(preferences, FAMILY_LATITUDE)));
            angle = calculateAngle(locationService, famLatitude, famLongitude);
            if (angle == NO_FAMILY_LOCATION) {
                familyHouse = findViewById(R.id.familyHouse);
                familyHouse.setVisibility(View.INVISIBLE);
            }
            else {
                initialDegree[1] = (float)angle;
                familyHouse = findViewById(R.id.familyHouse);
                familyHouse.setVisibility(View.VISIBLE);
            }
        }
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
        anim.setDuration(210);
        anim.setInterpolator(new LinearInterpolator());
        RotateAnimation ra = new RotateAnimation(
                startAngle,
                endAngle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        ra.setDuration(210);
        ra.setFillAfter(true);
        anim.start();
        view.startAnimation(ra);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = -1f * (float) Math.toDegrees(Math.atan2(sensorEvent.values[0], sensorEvent.values[1]));
            familyHouse = findViewById(R.id.familyHouse);
            arrowImage = findViewById(R.id.arrow);
            View [] rotationElements = {arrowImage, familyHouse};
            for (int i = 0; i < rotationElements.length; i++) {
                rotateView(rotationElements[i], currentDegree[i], -degree, initialDegree[i]);
                currentDegree[i] = -degree;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //nothing
    }
}
