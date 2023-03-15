package com.example.socialcompass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.socialcompass.model.Location;
import com.example.socialcompass.model.LocationAPI;
import com.example.socialcompass.model.LocationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocationAPITest {
    private LocationAPI testAPI;
    private Location testLocation1;

    @Before
    public void CreateAPI() {
        testAPI = new LocationAPI();
    }

    @Before
    public void CreateLocation() {
        testLocation1 = new LocationBuilder()
                .setPublicCode("ttt")
                .setPrivateCode("ppp")
                .setLabel("this_is_a_label")
                .setLatitude(101)
                .setLongitude(110)
                .setListedPublicly(true)
                .setCreatedAt(0)
                .setUpdatedAt(0)
                .build();
    }

    @Test
    public void testPut() {
        var response = testAPI.put(testLocation1);
        assertNotNull(response);
        assertNotNull(response.first);
        assertNotNull(response.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
    }

    @Test
    public void testPutAsync() {
        var future = testAPI.putAsync(testLocation1);
        try {
            var response = future.get();
            assertNotNull(response);
            assertNotNull(response.first);
            assertNotNull(response.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGet() {
        testAPI.put(testLocation1);
        var response = testAPI.get(testLocation1.publicCode);
        assertNotNull(response);
        assertNotNull(response.first);
        assertNotNull(response.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
    }

    @Test
    public void testGetAsync() {
        testAPI.put(testLocation1);
        var future = testAPI.getAsync(testLocation1.publicCode);
        try {
            var response = future.get();
            assertNotNull(response);
            assertNotNull(response.first);
            assertNotNull(response.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetAll() {
        var response = testAPI.getAll();
        assertNotNull(response);
        assertNotNull(response.first);
        assertNotNull(response.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
    }

    @Test
    public void testGetAllAsync() {
        var future = testAPI.getAllAsync();
        try {
            var response = future.get();
            assertNotNull(response);
            assertNotNull(response.first);
            assertNotNull(response.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testUpdateCoordinate() {
        testAPI.put(testLocation1);
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setLatitude(33)
                .setLatitude(66)
                .build();

        var updateResponse = testAPI.updateCoordinates(testLocation2);
        assertNotNull(updateResponse);
        assertNotNull(updateResponse.first);
        assertNotNull(updateResponse.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

        var getResponse = testAPI.get(testLocation1.publicCode);
        var serverUpdatedLocation = Location.fromJSON(getResponse.second);

        assertEquals(serverUpdatedLocation.latitude, testLocation2.latitude, 0);
        assertEquals(serverUpdatedLocation.longitude, testLocation2.longitude, 0);
    }

    @Test
    public void testUpdateCoordinateAsync() {
        testAPI.put(testLocation1);
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setLatitude(33)
                .setLatitude(66)
                .build();

        var future = testAPI.updateCoordinatesAsync(testLocation2);
        try {
            var updateResponse = future.get();
            assertNotNull(updateResponse);
            assertNotNull(updateResponse.first);
            assertNotNull(updateResponse.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

            var getResponse = testAPI.get(testLocation1.publicCode);
            var serverUpdatedLocation = Location.fromJSON(getResponse.second);

            assertEquals(serverUpdatedLocation.latitude, testLocation2.latitude, 0);
            assertEquals(serverUpdatedLocation.longitude, testLocation2.longitude, 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRelabel() {
        testAPI.put(testLocation1);
        String newLabel = "this is the new label";
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setLabel(newLabel)
                .build();
        var updateResponse = testAPI.relabel(testLocation2);
        assertNotNull(updateResponse);
        assertNotNull(updateResponse.first);
        assertNotNull(updateResponse.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

        var getResponse = testAPI.get(testLocation1.publicCode);
        var serverUpdatedLocation = Location.fromJSON(getResponse.second);

        assertEquals(serverUpdatedLocation.label, testLocation2.label);
    }

    @Test
    public void testRelabelAsync() {
        testAPI.put(testLocation1);
        String newLabel = "this is the new label";
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setLabel(newLabel)
                .build();
        var future = testAPI.relabelAsync(testLocation2);
        try {
            var updateResponse = future.get();
            assertNotNull(updateResponse);
            assertNotNull(updateResponse.first);
            assertNotNull(updateResponse.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

            var getResponse = testAPI.get(testLocation1.publicCode);
            var serverUpdatedLocation = Location.fromJSON(getResponse.second);

            assertEquals(serverUpdatedLocation.label, testLocation2.label);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPublish() {
        testLocation1.listedPublicly = false;
        testAPI.put(testLocation1);
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setListedPublicly(true)
                .build();
        var updateResponse = testAPI.publish(testLocation2);
        assertNotNull(updateResponse);
        assertNotNull(updateResponse.first);
        assertNotNull(updateResponse.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

        var getResponse = testAPI.get(testLocation1.publicCode);
        var serverUpdatedLocation = Location.fromJSON(getResponse.second);

        assertEquals(serverUpdatedLocation.listedPublicly, testLocation2.listedPublicly);
    }

    @Test
    public void testPublishAsync() {
        testLocation1.listedPublicly = false;
        testAPI.put(testLocation1);
        var testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setListedPublicly(true)
                .build();
        var future = testAPI.publishAsync(testLocation2);
        try {
            var updateResponse = future.get();
            assertNotNull(updateResponse);
            assertNotNull(updateResponse.first);
            assertNotNull(updateResponse.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) updateResponse.first);

            var getResponse = testAPI.get(testLocation1.publicCode);
            var serverUpdatedLocation = Location.fromJSON(getResponse.second);

            assertEquals(serverUpdatedLocation.listedPublicly, testLocation2.listedPublicly);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDelete() {
        testAPI.put(testLocation1);
        var response = testAPI.delete(testLocation1);
        assertNotNull(response);
        assertNotNull(response.first);
        assertNotNull(response.second);
        assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
    }

    @Test
    public void testDeleteAsync() {
        testAPI.put(testLocation1);
        var future = testAPI.deleteAsync(testLocation1);
        try {
            var response = future.get();
            assertNotNull(response);
            assertNotNull(response.first);
            assertNotNull(response.second);
            assertEquals(LocationAPI.SUCCESS_CODE, (int) response.first);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}