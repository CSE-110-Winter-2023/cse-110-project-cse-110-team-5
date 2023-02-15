package com.example.socialcompass;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;


public final class Util {
    private Util() { }

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

    public static String getFloatAsString(SharedPreferences map, String key) {
        if (map.contains(key)) {
            return String.valueOf(map.getFloat(key, 0F));
        } else {
            return "";
        }
    }
}
