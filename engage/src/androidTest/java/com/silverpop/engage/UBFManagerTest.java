package com.silverpop.engage;

import android.test.AndroidTestCase;

import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.UBFManager;


/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBFManagerTest
        extends AndroidTestCase {

//    //TODO: Move these values to external configuration file
//    private static final String host = "apipilot.silverpop.com";
//    private static final String clientId = "02eb567b-3674-4c48-8418-dbf17e0194fc";
//    private static final String clientSecret = "9c650c5b-bcb8-4eb3-bf0a-cc8ad9f41580";
//    private static final String refreshToken = "676476e8-2d1f-45f9-9460-a2489640f41a";
//    private static final String list_id = "23949";
//
//    private UBFManager ubfManager;
//
//    @Override
//    public void setUp() throws Exception {
//        ubfManager = new UBFManager(getContext(), clientId, clientSecret, refreshToken, host);
//    }
//
//    public void testUBfManagerCreation() {
//        assertTrue(ubfManager != null);
//    }
//
//    public void testPostEvent() {
//        UBF installed = UBF.installed(getContext(), null);
//        long eventId = ubfManager.postEvent(installed);
//        assertTrue(eventId > 0);
//        assertTrue(ubfManager.statusForEventById(eventId) != -1);   //Make sure the event is in the DB
//    }
//
//    public void testHandleLocalNotificationReceivedEvents() {
//        assertTrue(false);
//    }
//
//    public void testPushNotificationReceivedEvents() {
//        assertTrue(false);
//    }
//
//    public void testHandleNotificationOpenedEvents() {
//        assertTrue(false);
//    }
//
//    public void testHandleExternalURLOpenedEvents() {
//        assertTrue(false);
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        ubfManager = null;
//    }
}
