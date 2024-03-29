package com.example.socialcompass.model;


import android.util.Pair;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LocationAPI {
    private static String BASE_URL = "https://socialcompass.goto.ucsd.edu/";
    private final static String ORIGINAL_URL = "https://socialcompass.goto.ucsd.edu/";
    private final static String LOCATION_ENDPOINT = "location/";
    private final static String LOCATIONS_ENDPOINT = "locations";
    public final static int SUCCESS_CODE = 200;
    private volatile static LocationAPI instance = null;
    private final OkHttpClient client;
    private final Gson gson;

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
     *
     * @return response code (int) and response json array body (string)
     */
    public Pair<Integer, String> getAll() {
        Pair<Integer, String> bodyAndCode = null;
        var request = new Request.Builder()
                .url(BASE_URL + LOCATIONS_ENDPOINT)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     *  Method to get ALL locations found on the server in JSON
     *
     * @return response code (int) and response json array body (string)
     */
    public CompletableFuture<Pair<Integer, String>> getAllAsync() {
        var request = new Request.Builder()
                .url(BASE_URL + LOCATIONS_ENDPOINT)
                .method("GET", null)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                // no response of any kind
                return null;
            }
        });
    }

    /**
     *  Method to get friend's location on the server in JSON
     *
     * @param publicCode string of location object we want
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> get(String publicCode) {
        // URLs cannot contain spaces, so we replace them with %20.
        publicCode = publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .get()
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     *  Method to get friend's location on the server in JSON
     *  asynchronously
     *
     * @param publicCode string of location object we want
     * @return response code (int) and response json body (string) wrapped in a Future<>
     */
    public CompletableFuture<Pair<Integer, String>> getAsync(String publicCode) {
        // URLs cannot contain spaces, so we replace them with %20.
        publicCode = publicCode.replace(" ", "%20");
        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .get()
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                // no response of any kind
                return null;
            }
        });
    }

    /**
     * Method to put our location to the server
     *
     * @param location object that represents our location
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> put(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                                "private_code", location.privateCode,
                                "label", location.label,
                                "latitude", location.latitude,
                                "longitude", location.longitude
                        )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .put(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     * Method to put our location to the server asynchronously
     *
     * @param location object that represents our location
     * @return response code (int) and response json body (string) wrapped in a Future<>
     */
    public CompletableFuture<Pair<Integer, String>> putAsync(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "label", location.label,
                        "latitude", location.latitude,
                        "longitude", location.longitude
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .put(requestBody)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                // no response of any kind
                return null;
            }
        });
    }

    /**
     * Method to delete our location from the server
     *
     * @param location object; we just need it's private code
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> delete(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .delete(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     * Method to delete our location from the server asynchronously
     *
     * @param location object; we just need it's private code
     * @return response code (int) and response json body (string)
     */
    public CompletableFuture<Pair<Integer, String>> deleteAsync(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .delete(requestBody)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                // no response of any kind
                return null;
            }
        });
    }

    /**
     * Method to publish our location
     *
     * @param location object with updated is_listed_publicly field
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> publish(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "is_listed_publicly", location.listedPublicly
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     * Method to publish our location asynchronously
     *
     * @param location object with updated is_listed_publicly field
     * @return response code (int) and response json body (string)
     */
    public CompletableFuture<Pair<Integer, String>> publishAsync(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "is_listed_publicly", location.listedPublicly
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Method to rename our location
     *
     * @param location object with updated label
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> relabel(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "label", location.label
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     * Method to rename our location
     *
     * @param location object with updated label
     * @return response code (int) and response json body (string)
     */
    public CompletableFuture<Pair<Integer, String>> relabelAsync(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "label", location.label
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Method to update our location coordinates
     *
     * @param location object with updated coordinates
     * @return response code (int) and response json body (string)
     */
    public Pair<Integer, String> updateCoordinates(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        Pair<Integer, String> bodyAndCode = null;
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "latitude", location.latitude,
                        "longitude", location.longitude
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.code() == SUCCESS_CODE;
            assert response.body() != null;
            bodyAndCode = new Pair<>(response.code(), response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyAndCode;
    }

    /**
     * Method to update our location coordinates
     *
     * @param location object with updated coordinates
     * @return response code (int) and response json body (string)
     */
    public CompletableFuture<Pair<Integer, String>> updateCoordinatesAsync(@NonNull Location location) {
        // URLs cannot contain spaces, so we replace them with %20.
        String publicCode = location.publicCode.replace(" ", "%20");
        var requestBody = RequestBody.create(
                gson.toJson(Map.of(
                        "private_code", location.privateCode,
                        "latitude", location.latitude,
                        "longitude", location.longitude
                )),
                MediaType.parse("application/json")
        );

        var request = new Request.Builder()
                .url(BASE_URL + LOCATION_ENDPOINT + publicCode)
                .patch(requestBody)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (var response = client.newCall(request).execute()) {
                assert response.code() == SUCCESS_CODE;
                assert response.body() != null;
                return new Pair<>(response.code(), response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // method to change the BASE_URL field to what is inputted for the mock
    public void ChangeBaseUrl(String newBaseUrl) {
        this.BASE_URL = newBaseUrl;
    }

    public void ResetBaseUrl(){
        this.BASE_URL = ORIGINAL_URL;
    }
}
