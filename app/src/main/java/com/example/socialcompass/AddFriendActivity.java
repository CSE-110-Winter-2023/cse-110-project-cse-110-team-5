package com.example.socialcompass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialcompass.model.LocationDatabase;
import com.example.socialcompass.model.LocationRepository;


public class AddFriendActivity extends AppCompatActivity {
    private EditText degrees;
    private EditText addFriendEditText;

    public static final String UI_DEGREES = "degreeLabel";
    private LocationRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // set up db and api
        var db = LocationDatabase.getSingleton(this);
        repo = new LocationRepository(db.locationDao());

        addFriendEditText = findViewById(R.id.addFriendEditText);
    }


    public void onBackButtonClick(View view) {
        finish(); // back to main activity
    }

    public void onSaveButtonClick(View view) {
        Toast.makeText(getApplicationContext(), "Looking for friend...", Toast.LENGTH_SHORT).show();
        var publicCode = addFriendEditText.getText().toString();
        var location = repo.getRemote(publicCode);
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Friend doesn't exist!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(), location.label + " added!", Toast.LENGTH_SHORT).show();
        repo.addLocationToDb(location);
    }
}