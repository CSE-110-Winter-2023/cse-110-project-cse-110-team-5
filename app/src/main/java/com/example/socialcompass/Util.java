package com.example.socialcompass;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;

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
}
