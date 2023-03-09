package com.example.socialcompass;

import static com.example.socialcompass.AddFriendActivity.UI_DEGREES;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class AddFriendActivity extends AppCompatActivity {
    private EditText degrees;

    public static final String UI_DEGREES = "degreeLabel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        SharedPreferences preferences = getSharedPreferences("shared", MODE_PRIVATE);
        degrees = findViewById(R.id.degreesEditText);
        degrees.setText(Util.getFloatAsString(preferences, UI_DEGREES));
    }


    public void onBackButtonClick(View view) {
        finish(); // back to main activity
    }

    public void onSaveButtonClick(View view) {

    }

    public void onMockDegreesButtonClick(View view) {
        // parse float
        try {
            String raw = degrees.getText().toString();
            SharedPreferences preferences = getSharedPreferences("shared", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            if (raw == null || raw.equals("")) {
                editor.remove(UI_DEGREES);
            } else {
                float parsed = Float.parseFloat(degrees.getText().toString());
                while (parsed < 0) {
                    parsed += 360;
                }
                parsed %= 360;
                editor.putFloat(UI_DEGREES, parsed);
            }
            editor.apply();
        } catch (IllegalArgumentException e) {
            return;
        }
        finish();
    }
}