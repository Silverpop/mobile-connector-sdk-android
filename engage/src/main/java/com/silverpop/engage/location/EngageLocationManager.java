package com.silverpop.engage.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by jeremydyer on 6/2/14.
 */
public class EngageLocationManager {

    private static final String TAG = EngageLocationManager.class.getName();
    public static final String ACTION_LOCATION = "com.silverpop.engage.location.ACTION_LOCATION";

    private static EngageLocationManager sLocationManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    private EngageLocationManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static EngageLocationManager get(Context c) {
        if (sLocationManager == null) {
            //Use the application context to avoid leaking activities.
            sLocationManager = new EngageLocationManager(c.getApplicationContext());
        }
        return sLocationManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            PendingIntent pi = getLocationPendingIntent(true);

            //Get the last known location and broadcast it if you have one.
            Location lastKnown = null;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnown = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, pi);
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
        mAppContext.sendBroadcast(broadcast);
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
