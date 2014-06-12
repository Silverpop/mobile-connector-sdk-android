package com.silverpop.engage.test;

import android.test.AndroidTestCase;

import com.silverpop.engage.config.EngageConfig;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageConfigTest
    extends AndroidTestCase {

    public void testexpireLocalEventsAfterNumDays() {

    }

    public void testDeviceName() {
        String deviceName = EngageConfig.deviceName();
        assertTrue(deviceName != null);
        assertTrue(deviceName.length() > 0);
    }

    public void testDeviceId() {
        String deviceId = EngageConfig.deviceId(getContext());
        assertTrue(deviceId != null);
        assertTrue(deviceId.length() > 0);
    }

    public void testPrimaryUserId() {
        String primUser = "EngageTestPrimaryUserId@gmail.com";
        EngageConfig.storePrimaryUserId(getContext(), primUser);
        assertEquals(primUser, EngageConfig.primaryUserId(getContext()));
    }

    public void testAnonymousUserId() {
        String anonUser = "AnonymousTestPrimaryUserId@gmail.com";
        EngageConfig.storeAnonymousUserId(getContext(), anonUser);
        assertEquals(anonUser, EngageConfig.anonymousUserId(getContext()));
    }


    public void testCurrentCampaign() {
        String currentCampaign = "EngageTestCurrentCampaign";

        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, 1);
        currentDate = c.getTime();

        EngageConfig.storeCurrentCampaignWithExpirationTimestamp(getContext(), currentCampaign, currentDate.getTime());
        assertEquals(currentCampaign, EngageConfig.currentCampaign(getContext()));
    }

    public void testLastCampaign() {
        String currentCampaign = "EngageTestCurrentCampaign";
        EngageConfig.storeCurrentCampaignWithExpirationTimestamp(getContext(), currentCampaign, -1); // Effectively expired.
        assertEquals(currentCampaign, EngageConfig.lastCampaign(getContext()));
    }

    public void testStoreCurrentCampaignWithExpiration() {
        String currentCampaign = "EngageTestCurrentCampaign";

        Date currentDate = new Date();
        currentDate.setTime(currentDate.getTime() + 86400000);  //Set for 1 day from now
        EngageConfig.storeCurrentCampaignWithExpirationTimestamp(getContext(), currentCampaign, currentDate.getTime());
        assertEquals(currentCampaign, EngageConfig.currentCampaign(getContext()));
    }

//    public void testDeviceVersion() {
//        assertFalse(true);
//    }
//
//    public void testOsName() {
//        assertFalse(true);
//    }
//
//    public void testOsVersion() {
//        assertFalse(true);
//    }
//
//    public void testAppName() {
//        assertFalse(true);
//    }
//
//    public void testAppVersion() {
//        assertFalse(true);
//    }
}
