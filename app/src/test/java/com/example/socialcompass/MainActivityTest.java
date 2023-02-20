package com.example.socialcompass;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;
import android.widget.ImageView;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.rule.GrantPermissionRule;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    ActivityScenario<MainActivity> scenario;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void init() {
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    @Test
    public void testIfArrowDisplayed(){
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
}