package com.silverpop.engage.util.uuid.plugin;

import com.silverpop.engage.util.uuid.UUIDGenerator;

import java.util.UUID;

/**
 * Created by Lindsay Thurmond on 12/31/14.
 */
public class DefaultUUIDGenerator implements UUIDGenerator {

    @Override
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
