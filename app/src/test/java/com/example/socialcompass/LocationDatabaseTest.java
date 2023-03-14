package com.example.socialcompass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.socialcompass.model.Location;
import com.example.socialcompass.model.LocationBuilder;
import com.example.socialcompass.model.LocationDao;
import com.example.socialcompass.model.LocationDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class LocationDatabaseTest {
    private LocationDao dao;
    private LocationDatabase db;

    private Location testLocation1;

    @Before
    public void CreateDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, LocationDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.locationDao();
    }

    @Before
    public void CreateLocation() {
        testLocation1 = new LocationBuilder()
                .setPublicCode("public_code")
                .setPrivateCode("private_code")
                .setLabel("label")
                .setLatitude(101)
                .setLongitude(110)
                .setListedPublicly(true)
                .setCreatedAt(0)
                .setUpdatedAt(0)
                .build();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testInsert() {
        Location testLocation2 = LocationBuilder
                .copyLocationData(testLocation1)
                .setPublicCode("public_code_2")
                .build();

        long id1 = dao.insertLocation(testLocation1);
        long id2 = dao.insertLocation(testLocation2);

        assertNotEquals(id1, id2);
    }

    @Test
    public void testGet() {
        long id = dao.insertLocation(testLocation1);
        Location item = dao.getLocation(testLocation1.publicCode);
        assertEquals(testLocation1.publicCode, item.publicCode);
        assertEquals(testLocation1.privateCode, item.privateCode);
        assertEquals(testLocation1.label, item.label);
        assertEquals(testLocation1.latitude , item.latitude, 0);
        assertEquals(testLocation1.longitude, item.longitude, 0);
        assertEquals(testLocation1.createdAt, item.createdAt);
        assertEquals(testLocation1.updatedAt, item.updatedAt);
    }

    @Test
    public void testUpdate() {
        long id = dao.insertLocation(testLocation1);
        String publicCode = testLocation1.publicCode;

        Location updatedLocation = LocationBuilder
                .copyLocationData(testLocation1)
                .setLabel("new_label")
                .build();

        int itemsUpdated = dao.updateLocation(updatedLocation);
        assertEquals(1, itemsUpdated);

        updatedLocation = dao.getLocation(publicCode);
        assertNotNull(testLocation1);
        assertEquals(updatedLocation.label, "new_label");
    }

    @Test
    public void testExists() {
        long id = dao.insertLocation(testLocation1);

        boolean itemFound = dao.exists(testLocation1.publicCode);
        assertTrue(itemFound);

        dao.deleteLocation(testLocation1);
        boolean itemNotFound = dao.exists(testLocation1.publicCode);
        assertTrue(!itemNotFound);
    }

    @Test
    public void testClear() {
        dao.insertLocation(testLocation1);
        boolean itemFound = dao.exists(testLocation1.publicCode);
        assertTrue(itemFound);

        dao.clear();
        boolean itemNotFound = dao.exists(testLocation1.publicCode);
        assertTrue(!itemNotFound);

        var stuff = dao.getAllLocations();
        assertNotNull(stuff);
        assertTrue(stuff.isEmpty());
    }

    @Test
    public void testSize() {
        dao.clear();
        assertTrue(dao.size() == 0);

        dao.insertLocation(testLocation1);
        boolean itemFound = dao.exists(testLocation1.publicCode);
        assertTrue(itemFound);
        assertTrue(dao.size() == 1);
    }

    @Test
    public void testDelete() {
        long id = dao.insertLocation(testLocation1);
        int itemsDeleted = dao.deleteLocation(testLocation1);
        assertEquals(1, itemsDeleted);
        assertNull(dao.getLocation(testLocation1.publicCode));
    }
}
