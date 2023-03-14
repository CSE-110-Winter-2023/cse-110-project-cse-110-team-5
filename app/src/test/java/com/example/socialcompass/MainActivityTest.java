package com.example.socialcompass;


import static android.content.Context.MODE_PRIVATE;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import static org.junit.Assert.*;

import android.app.AlertDialog;
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
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
    public void testNoNameEntered() {
        scenario.onActivity(activity -> {
            SharedPreferences preferences = activity.getSharedPreferences("shared", MODE_PRIVATE);
            String name = preferences.getString("name", null);
            assertNull(name);
            String uid = preferences.getString("uid", null);
            assertNull(uid);
        });
    }

    @Test
    public void testAlertDisplayedWhenNoName() {
        scenario.onActivity(activity -> {
            //Haven't set a name, so popup should appear
            AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
            assertNotNull(alertDialog);
        });
    }
}