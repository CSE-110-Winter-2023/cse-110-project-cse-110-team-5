package com.example.socialcompass;


import static android.content.Context.MODE_PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

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
            ImageView arrow = activity.findViewById(R.id.arrow);
            assertTrue(arrow.isShown());
        });
    }

    @Test
    public void testIfCircleDisplayed(){
        scenario.onActivity(activity -> {
            ImageView circle = activity.findViewById(R.id.circle);
            assertTrue(circle.isShown());
        });
    }

    @Test
    public void testArrowOrientatedNorth(){
        scenario.onActivity(activity -> {
            ImageView arrow = activity.findViewById(R.id.arrow);
            assertEquals(0, (int)arrow.getRotation());
        });
    }

    @Test
    public void testAddFriendsButtonOpensActivity(){
        scenario.onActivity(activity -> {
            Button button = activity.findViewById(R.id.addFriendButton);
            button.performClick();

            Intent expectedIntent = new Intent(activity, AddFriendActivity.class);
            ShadowActivity shadowActivity = Shadows.shadowOf(activity);
            Intent actualIntent = shadowActivity.getNextStartedActivity();

            assertEquals(expectedIntent.getComponent(), actualIntent.getComponent());
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

    @Test
    public void testConnectionImageViewDisplayed(){
        scenario.onActivity(activity -> {
            ImageView connectionSym = activity.findViewById(R.id.connectionImageView);
            assertTrue(connectionSym.isShown());
        });
    }

    @Test
    public void testDisconnectedTextViewVisibilityWhenConnected(){
        scenario.onActivity(activity -> {
            ImageView connectionSym = activity.findViewById(R.id.connectionImageView);
            connectionSym.setBackgroundColor(Color.parseColor("#3CB043"));
            TextView minutesInactive = activity.findViewById(R.id.disconnectionTimeTextView);
            assertEquals(minutesInactive.getText(), "");
        });
    }
}