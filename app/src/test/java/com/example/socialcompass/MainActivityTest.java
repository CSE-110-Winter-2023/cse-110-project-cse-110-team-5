package com.example.socialcompass;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.rule.GrantPermissionRule;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void testIfArrowDisplayed(){
        var scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            ImageView arrow = (ImageView) activity.findViewById(R.id.arrow);
            assertEquals(arrow.isShown(), true);
        });
    }

    @Test
    public void testIfCircleDisplayed(){
        var scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            ImageView circle = (ImageView) activity.findViewById(R.id.circle);
            assertEquals(circle.isShown(), true);
        });
    }

    @Test
    public void testArrowOrientation(){
        var scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            ImageView arrow = (ImageView) activity.findViewById(R.id.arrow);
            assertEquals((int)arrow.getRotation(), 90);
        });
    }
}