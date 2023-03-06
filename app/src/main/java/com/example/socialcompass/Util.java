package com.example.socialcompass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Util class in order to save each float in the activities.
 * Also parses floats into strings as necessary.
 */
public final class Util {
    private Util() { }

    /*
    Saves ui components
     */
    public static void saveFloat(SharedPreferences.Editor editor, EditText uiComponent, String key) {
        String str = uiComponent.getText().toString();
        try {
            if (str != null && !str.equals("")) {
                float parsed = Float.parseFloat(str);
                editor.putFloat(key, parsed);
            } else {
                editor.remove(key);
            }
        } catch (NumberFormatException e) {
            Log.e("error", "Could not parse float " + str + ".");
        }
    }

    /*
    Returns string in float format
     */
    public static String getFloatAsString(SharedPreferences map, String key) {
        if (map.contains(key)) {
            return String.valueOf(map.getFloat(key, 0F));
        } else {
            return "";
        }
    }

    public static void showNamePrompt(Activity activity, Context context, SharedPreferences.Editor editor) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        final EditText enter_name = new EditText(context);
        alertBuilder.setView(enter_name);

        alertBuilder
                .setTitle("Enter Name")
                .setPositiveButton("Save", null)
                .setCancelable(true);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setOnShowListener(dialog -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String name = enter_name.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show();
                    //saveButton.setEnabled(false);
                } else {
                    editor.putString("name", enter_name.getText().toString());
                    editor.apply();
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.show();
    }
}
