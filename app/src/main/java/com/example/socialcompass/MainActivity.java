package com.example.socialcompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ImageView arrowImage;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private float currentDegree = 0;
    private int ANGLE = 1;
    private TextView familyHouse;
    double angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrowImage = findViewById(R.id.arrow);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    public double calculateAngle(LocationService me) {
        EditText ETFamLongitude = findViewById(R.id.familyLongEditText);
        EditText ETFamLatitude = findViewById(R.id.familyLatEditText);
        double famLongitude = Double.parseDouble(String.valueOf(ETFamLongitude.getText()));
        double famLatitude = Double.parseDouble(String.valueOf(ETFamLatitude.getText()));
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        Log.e("longitude error", String.valueOf(longitude));
        Log.e("latitude error", String.valueOf(latitude));
        Log.e("famLong error", String.valueOf(famLongitude));
        Log.e("famLat error", String.valueOf(famLatitude));

        double longDiff = Math.toRadians(famLongitude-longitude);
        double y = Math.sin(longDiff) * Math.cos(famLatitude);
        double x = Math.cos(latitude) * Math.sin(famLatitude) - Math.sin(latitude) * Math.cos(famLatitude) * Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public void onLocationsButtonClick(View view) {
        Intent intent = new Intent(this, LocationEntryActivity.class);
        startActivityForResult(intent, ANGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed
        if(requestCode==1) {
            angle = data.getDoubleExtra(ANGLE, defaultValue)
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = -1f * (float) Math.toDegrees(Math.atan2(sensorEvent.values[0], sensorEvent.values[1]));

            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.48f,
                    Animation.RELATIVE_TO_SELF, 1.35f
            );
            ra.setDuration(210);
            ra.setFillAfter(true);
            arrowImage.startAnimation(ra);
            currentDegree = -degree;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //nothing
    }
}
