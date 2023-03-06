package com.example.socialcompass.model;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LocationAPI {

    private final static String BASE_URL = "https://socialcompass.goto.ucsd.edu/";
    private final static String LOCATION_ENDPOINT = "location/";
    private final static String LOCATIONS_ENDPOINT = "locations";

    private volatile static LocationAPI instance = null;

    private OkHttpClient client;

    private Gson gson;

    public LocationAPI() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public static LocationAPI provide() {
        if (instance == null) {
            instance = new LocationAPI();
        }
        return instance;
    }

    /**
     *  Method to get ALL locations found on the server in JSON
     */
    public String getAll() {
        var body = "";
        var request = new Request.Builder()
                .url(BASE_URL + LOCATIONS_ENDPOINT)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            body = response.body().string();
            Log.i("LocationAPI: getAll ", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    /**
     *  Method to get friend's location on the server in JSON
     */
    public String get(String publicCode) {
        // URLs cannot contain spaces, so we replace them with %20.
        publicCode = publicCode.replace(" ", "%20");
        var body = "";

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            body = response.body().string();
            Log.i("LocationAPI: get ", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    /**
     * Method to put our location to the server
     */
    public void put(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "label", location.label,
                        "latitude", location.latitude,
                        "longitude", location.longitude
                ))
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .put(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("LocationAPI: put ", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
