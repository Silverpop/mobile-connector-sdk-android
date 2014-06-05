package com.silverpop.engage.domain;

import org.json.JSONObject;

/**
 * Created by jeremydyer on 5/19/14.
 */
public interface JSONable {

    /**
     * Must return a JSON-compliant representation of this object,
     * in the form of a JSONObject.
     *
     * @return The JSONObject representation.
     *  Implementations may throw other unchecked exceptions.
     */
    JSONObject toJSONObject();

    /**
     * Must populate this object from the given JSON source.
     *
     * @param src The source JSON data.
     * may optionally throw if the input object violates the expected structure.
     */
    void fromJSONObject( final JSONObject src );
}
