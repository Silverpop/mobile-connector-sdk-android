package com.silverpop.engage.location.plugin;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.silverpop.engage.EngageApplication;
import com.silverpop.engage.location.EngageLocationManager;

/**
 * Created by jeremydyer on 6/12/14.
 */
public class EngageLocationManagerDefault
    implements EngageLocationManager {

    private EngageApplication engageApplicationInstance = null;

    private static final String TAG = EngageLocationManager.class.getName();
    public static final String ACTION_LOCATION = "com.silverpop.engage.location.ACTION_LOCATION";

    private static EngageLocationManagerDefault sLocationManager;
    private LocationManager mLocationManager;

    @Override
    public void setEngageApplication(EngageApplication engageApplication) {
        engageApplicationInstance = engageApplication;
        mLocationManager = (LocationManager) engageApplicationInstance.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public EngageLocationManagerDefault() {}

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(engageApplicationInstance.getApplicationContext(), 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            PendingIntent pi = getLocationPendingIntent(true);

            //Get the last known location and broadcast it if you have one.
            Location lastKnown = null;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnown = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, pi);
            }

            if (lastKnown != null) {
                //Reset the time to now.
                lastKnown.setTime(System.currentTimeMillis());
                broadcastLocation(lastKnown);
            }

        } else {
            Log.w(TAG, "Neither GPS or Network provider is available to update location");
        }
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        engageApplicationInstance.getApplicationContext().sendBroadcast(broadcast);
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingLocation() {
        return getLocationPendingIntent(false) != null;
    }
}
