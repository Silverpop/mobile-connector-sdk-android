package com.silverpop.engage.location.manager;

import android.app.PendingIntent;
import android.location.Location;

import com.silverpop.engage.EngageApplication;

/**
 * Created by jeremydyer on 6/2/14.
 */
public interface EngageLocationManager {

    /**
     * Inject EngageApplication instance into the plugin.
     *
     * @param engageApplication
     */
    public void setEngageApplication(EngageApplication engageApplication);

    /**
     * Begin monitoring for location updates.
     */
    public void startLocationUpdates();

    /**
     * Stops location updates.
     */
    public void stopLocationUpdates();

    /**
     * Is the location currently being tracked by this plugin instance.
     *
     * @return
     */
    public boolean isTrackingLocation();
}
