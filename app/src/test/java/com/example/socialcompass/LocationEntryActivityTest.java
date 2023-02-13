package com.example.socialcompass;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LocationEntryActivityTest {
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
            var preferences = activity.getPreferences(Context.MODE_PRIVATE);
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
}
