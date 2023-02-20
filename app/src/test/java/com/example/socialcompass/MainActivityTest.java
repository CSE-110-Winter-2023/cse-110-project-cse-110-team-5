package com.example.socialcompass;


import static android.content.Context.MODE_PRIVATE;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.rule.GrantPermissionRule;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void init() {
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    @Test
    public void testIfArrowDisplayed() {
        scenario.onActivity(activity -> {
            ImageView arrow = (ImageView) activity.findViewById(R.id.arrow);
            assertTrue(arrow.isShown());
        });
    }

    @Test
    public void testIfCircleDisplayed(){
        scenario.onActivity(activity -> {
            ImageView circle = (ImageView) activity.findViewById(R.id.circle);
            assertTrue(circle.isShown());
        });
    }

    @Test
    public void testArrowOrientatedNorth(){
        scenario.onActivity(activity -> {
            ImageView arrow = (ImageView) activity.findViewById(R.id.arrow);
            assertEquals((int)arrow.getRotation(), 90);
        });
    }

    @Test
    public void testIfFamilyDisplayed() {
        scenario.onActivity(activity -> {
            SharedPreferences preferences = activity.getSharedPreferences("shared", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("familyLatitude", 30f);
            editor.putFloat("familyLongitude", -120f);
            editor.apply();
            MockLocationService locationService = new MockLocationService(activity);
            activity.setMarkerAngles(locationService);
            TextView family = (TextView) activity.findViewById(R.id.familyHouse);
            assertTrue(family.isShown());
        });
    }

    @Test
    public void testFamilyNotDisplayed() {
        scenario.onActivity(activity -> {
            SharedPreferences preferences = activity.getSharedPreferences("shared", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            if (preferences.contains("familyLatitude")) {
                editor.remove("familyLatitude");
            }
            if (preferences.contains("familyLongitude")) {
                editor.remove("familyLongitude");
            }
            editor.apply();
            MockLocationService locationService = new MockLocationService(activity);
            activity.setMarkerAngles(locationService);
            TextView family = (TextView) activity.findViewById(R.id.familyHouse);
            assertFalse(family.isShown());
        });
    }

    @Test
    public void testFamilyAngle() {
        scenario.onActivity(activity -> {
            SharedPreferences preferences = activity.getSharedPreferences("shared", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            MockLocationService locationService = new MockLocationService(activity);
            TextView family = (TextView) activity.findViewById(R.id.familyHouse);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) family.getLayoutParams();
            editor.putFloat("familyLatitude", 30f);
            editor.putFloat("familyLongitude", 0f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            assertEquals(0f, layoutParams.circleAngle, 0);
            editor.putFloat("familyLatitude", 0f);
            editor.putFloat("familyLongitude", 30f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            assertEquals(90f, layoutParams.circleAngle, 0);
            editor.putFloat("familyLatitude", -30f);
            editor.putFloat("familyLongitude", 0f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            assertEquals(180f, layoutParams.circleAngle, 0);
            editor.putFloat("familyLatitude", 0f);
            editor.putFloat("familyLongitude", -30f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            assertEquals(270f, layoutParams.circleAngle, 0);
        });
    }

    @Test
    public void testFamilyIconMovesWithUserLocation() {
        scenario.onActivity(activity -> {
            SharedPreferences preferences = activity.getSharedPreferences("shared", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            MockLocationService locationService = new MockLocationService(activity);
            TextView family = (TextView) activity.findViewById(R.id.familyHouse);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) family.getLayoutParams();
            editor.putFloat("familyLatitude", 30f);
            editor.putFloat("familyLongitude", 0f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            float initialX = family.getX();
            float initialY = family.getY();
            System.out.println(layoutParams.circleAngle);
            locationService.setLocation(100, 100);

            /**
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) family.getLayoutParams();
            editor.putFloat("familyLatitude", 30f);
            editor.putFloat("familyLongitude", 0f);
            editor.apply();
            activity.setMarkerAngles(locationService);
            System.out.println(family.getX());
             **/
        });
    }
}