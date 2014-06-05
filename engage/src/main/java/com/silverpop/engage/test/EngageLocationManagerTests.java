package com.silverpop.engage.test;

import android.test.AndroidTestCase;

import com.silverpop.engage.location.EngageLocationManager;

/**
 * Created by jeremydyer on 6/2/14.
 */
public class EngageLocationManagerTests
    extends AndroidTestCase {

    public void testSomething() {
        EngageLocationManager locationManager = EngageLocationManager.get(getContext());
        locationManager.startLocationUpdates();
        assertTrue(locationManager.isTrackingLocation());
    }
}
