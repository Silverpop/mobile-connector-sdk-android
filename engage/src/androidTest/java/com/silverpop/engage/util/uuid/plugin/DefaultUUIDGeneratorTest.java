package com.silverpop.engage.util.uuid.plugin;

import android.test.AndroidTestCase;

public class DefaultUUIDGeneratorTest extends AndroidTestCase {

    public void testGenerateUUID() throws Exception {

        DefaultUUIDGenerator defaultUUIDGenerator = new DefaultUUIDGenerator();
        String uuid = defaultUUIDGenerator.generateUUID();
        assertNotNull(uuid);
        assertFalse(uuid.isEmpty());

    }
}