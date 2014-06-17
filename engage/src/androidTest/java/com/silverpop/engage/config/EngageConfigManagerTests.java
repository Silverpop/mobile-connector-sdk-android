package com.silverpop.engage.config;

import android.test.AndroidTestCase;

import com.silverpop.engage.config.EngageConfigManager;

/**
 * Created by jeremydyer on 6/12/14.
 */
public class EngageConfigManagerTests
        extends AndroidTestCase {

    public void testexpireLocalEventsAfterNumDays() {
        EngageConfigManager man = EngageConfigManager.get(getContext());
        int value = man.expireLocalEventsAfterNumDays();
        assertTrue(value > 0);
    }

    public void testubfEventCacheSize() {
        EngageConfigManager man = EngageConfigManager.get(getContext());
        int value = man.ubfEventCacheSize();
        assertTrue(value > 0);
    }
}
