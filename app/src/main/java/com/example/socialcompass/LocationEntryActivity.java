package com.example.socialcompass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LocationEntryActivity extends AppCompatActivity {
    // shared preferences data keys :)
    public static final String FAMILY_LONGITUDE = "familyLongitude";
    public static final String FAMILY_LATITUDE = "familyLatitude";
    public static final String FAMILY_LABEL = "familyLabel";

    public static final String YOU_LONGITUDE = "youLongitude";
    public static final String YOU_LATITUDE = "youLatitude";
    public static final String YOU_LABEL = "youLabel";

    public static final String FRIEND_LONGITUDE = "friendLongitude";
    public static final String FRIEND_LATITUDE = "friendLatitude";
    public static final String FRIEND_LABEL = "friendLabel";

    private EditText longitudeFamily;
    private EditText latitudeFamily;
    private EditText labelFamily;

    private EditText longitudeYou;
    private EditText latitudeYou;
    private EditText labelYou;

    private EditText longitudeFriend;
    private EditText latitudeFriend;
    private EditText labelFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_entry);

        longitudeFriend = findViewById(R.id.friendLongEditText);
        latitudeFriend = findViewById(R.id.friendLatEditText);
        labelFriend = findViewById(R.id.friendLabelEditText);

        longitudeFamily = findViewById(R.id.familyLongEditText);
        latitudeFamily = findViewById(R.id.familyLongEditText);
        labelFamily = findViewById(R.id.familyLongEditText);

        longitudeYou = findViewById(R.id.houseLongEditText);
        latitudeYou = findViewById(R.id.houseLatEditText);
        labelYou = findViewById(R.id.houseLabelEditText);
    }

    public void onBackButtonClick(View view) {

    }
}