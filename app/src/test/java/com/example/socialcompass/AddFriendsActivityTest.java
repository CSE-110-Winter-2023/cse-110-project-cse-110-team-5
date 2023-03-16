package com.example.socialcompass;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

@RunWith(RobolectricTestRunner.class)
public class AddFriendsActivityTest {

    private ActivityScenario<AddFriendActivity> scenario;

    @Before
    public void init() {
        scenario = ActivityScenario.launch(AddFriendActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    @Test
    public void checkUIDisplayed(){
        scenario.onActivity(activity -> {
            TextView addFriend = activity.findViewById(R.id.addFriendTextView);
            EditText uuidEdit = activity.findViewById(R.id.addFriendEditText);
            Button backButton = activity.findViewById(R.id.backButton);
            Button addButton = activity.findViewById(R.id.addButton);

            assertEquals(addFriend.isShown(), true);
            assertEquals(uuidEdit.isShown(), true);
            assertEquals(backButton.isShown(), true);
            assertEquals(addButton.isShown(), true);
        });
    }
}
