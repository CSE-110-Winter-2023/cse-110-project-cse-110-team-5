package com.example.socialcompass;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LocationEntryActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void testIfLocationSaved() {
        var scenario = ActivityScenario.launch(LocationEntryActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            String dummyFamilyLabel = "WOOHOOO!! MESSI!!!";
            var familyLabel = (EditText) activity.findViewById(R.id.familyLabelEditText);
            familyLabel.setText(dummyFamilyLabel);
            var saveBtn = (Button) activity.findViewById(R.id.saveButton);
            saveBtn.performClick(); // we do a little saving >:)
            // check preferences
            var preferences = activity.getSharedPreferences("shared", Context.MODE_PRIVATE);
            String defaultValue = "no dice :(";
            String actualFamilyLabel = preferences.getString(LocationEntryActivity.FAMILY_LABEL, defaultValue);
            // testing
            assertEquals("Preferences should be updated", dummyFamilyLabel, actualFamilyLabel);
        });
    }

    @Test
    public void testLocationEntryOpens() {
        var scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            var enterLocationsBtn = (Button) activity.findViewById(R.id.locationsButton);
            enterLocationsBtn.performClick();
            // check that the next started intent is our LocationEntryActivity
            // https://stackoverflow.com/questions/5896088/testing-that-button-starts-an-activity-with-robolectric
            var nextIntent = shadowOf(activity).getNextStartedActivity();
            var shadowIntent = shadowOf(nextIntent);
            assertEquals(
                    "Next started intent should be LocationEntryActivity.class",
                    LocationEntryActivity.class,
                    shadowIntent.getIntentClass()
            );
        });
    }

    @Test
    public void testMockUIDegreesSaved() {
        var scenario = ActivityScenario.launch(LocationEntryActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onActivity(activity -> {
            float dummyDegrees = 25;
            var degreeLabel = (EditText) activity.findViewById(R.id.degreesEditText);
            degreeLabel.setText(Float.toString(dummyDegrees));
            var saveBtn = (Button) activity.findViewById(R.id.mockDegreesButton);
            saveBtn.performClick();
            // check preferences
            var preferences = activity.getSharedPreferences("shared", Context.MODE_PRIVATE);
            float defaultValue = 0;
            float actualDegreesLabel = preferences.getFloat(LocationEntryActivity.UI_DEGREES, defaultValue);
            // testing
            double DELTA = 0.01;
            assertEquals("Preferences should be updated", dummyDegrees, actualDegreesLabel, DELTA);
        });
    }
}
