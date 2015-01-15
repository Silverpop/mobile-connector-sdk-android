package com.silverpop.engage.util.uuid.plugin;

import com.silverpop.engage.util.uuid.UUIDGenerator;

import java.util.UUID;

/**
 * Created by Lindsay Thurmond on 12/31/14.
 */
public class DefaultUUIDGenerator implements UUIDGenerator {

    /**
     * Generates a unique id using the {@link java.util.UUID} class
     *
     * @return unique id formatted like "0398e44c-fcef-4521-a7c4-2b85b5d92a74"
     */
    @Override
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
