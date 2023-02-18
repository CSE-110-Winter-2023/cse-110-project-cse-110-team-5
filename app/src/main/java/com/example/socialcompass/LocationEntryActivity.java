package com.example.socialcompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    public static final String UI_DEGREES = "degreeLabel";

    private EditText longitudeFamily;
    private EditText latitudeFamily;
    private EditText labelFamily;

    private EditText longitudeYou;
    private EditText latitudeYou;
    private EditText labelYou;

    private EditText longitudeFriend;
    private EditText latitudeFriend;
    private EditText labelFriend;

    private EditText degrees;

    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_entry);

        longitudeFriend = findViewById(R.id.friendLongEditText);
        latitudeFriend = findViewById(R.id.friendLatEditText);
        labelFriend = findViewById(R.id.friendLabelEditText);

        longitudeFamily = findViewById(R.id.familyLongEditText);
        latitudeFamily = findViewById(R.id.familyLatEditText);
        labelFamily = findViewById(R.id.familyLabelEditText);

        longitudeYou = findViewById(R.id.houseLongEditText);
        latitudeYou = findViewById(R.id.houseLatEditText);
        labelYou = findViewById(R.id.houseLabelEditText);

        degrees = findViewById(R.id.degreesEditText);

        SharedPreferences preferences = getSharedPreferences("shared", MODE_PRIVATE);

        longitudeFriend.setText(Util.getFloatAsString(preferences, FRIEND_LONGITUDE));
        latitudeFriend.setText(Util.getFloatAsString(preferences, FRIEND_LATITUDE));
        labelFriend.setText(preferences.getString(FRIEND_LABEL, ""));

        longitudeFamily.setText(Util.getFloatAsString(preferences, FAMILY_LONGITUDE));
        latitudeFamily.setText(Util.getFloatAsString(preferences, FAMILY_LATITUDE));
        labelFamily.setText(preferences.getString(FAMILY_LABEL, ""));

        longitudeYou.setText(Util.getFloatAsString(preferences, YOU_LONGITUDE));
        latitudeYou.setText(Util.getFloatAsString(preferences, YOU_LATITUDE));
        labelYou.setText(preferences.getString(YOU_LABEL, ""));

        degrees.setText(Util.getFloatAsString(preferences, UI_DEGREES));
    }

    public void onBackButtonClick(View view) {
        savePreferences();
        finish(); // back to main activity
    }

    public void onSaveButtonClick(View view) {
        savePreferences();
    }

    void savePreferences() {
        SharedPreferences preferences = getSharedPreferences("shared", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // save relevant fields
        // family's house
        Util.saveFloat(editor, longitudeFamily, FAMILY_LONGITUDE);
        Util.saveFloat(editor, latitudeFamily, FAMILY_LATITUDE);
        editor.putString(FAMILY_LABEL, labelFamily.getText().toString());

        Util.saveFloat(editor, longitudeFriend, FRIEND_LONGITUDE);
        Util.saveFloat(editor, latitudeFriend, FRIEND_LATITUDE);
        editor.putString(FRIEND_LABEL, labelFriend.getText().toString());

        Util.saveFloat(editor, longitudeYou, YOU_LONGITUDE);
        Util.saveFloat(editor, latitudeYou, YOU_LATITUDE);
        editor.putString(YOU_LABEL, labelYou.getText().toString());

        Util.saveFloat(editor, degrees, UI_DEGREES);

        editor.apply();
    }


}