package com.silverpop.engage.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

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
        String provider = LocationManager.GPS_PROVIDER;

        //Get the last known location and broadcast it if you have one.
        Location lastKnown = mLocationManager.getLastKnownLocation(provider);

        if (lastKnown != null) {
            //Reset the time to now.
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

        //Start updates from the location manager.
        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
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


//    /**
//     * Takes a UBF event and EngageEvent and attempts to add location information to that UBF event.
//     *
//     * @param ubfEvent
//     * @param engageEvent
//     * @return
//     */
//    public UBF addLocationToUBFEvent(UBF ubfEvent, EngageEvent engageEvent) {
//        if (EngageConfig.currentLocationCache() == null) {
//            //Place a timeout for the coordinates being acquired before the event is posted without them.
//            Handler coordinateHandlerTimeout = new Handler();
//
//            Runnable coordTimeoutRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    //Make sure that the placemark hasn't been acquired since the expiration timer was set for this callback.
//                    if (EngageConfig.currentAddressCache() == null || EngageConfig.addressCacheExpired(mAppContext)) {
//                        UBFClient.get(mAppContext).postUBFEngageEvents(null, null);
//                    }
//                }
//            };
//
//            EngageConfigManager cm = EngageConfigManager.get(mAppContext);
//            EngageExpirationParser parser = new EngageExpirationParser(cm.augmentationTimeout(), null);
//
//            coordinateHandlerTimeout.postDelayed(coordTimeoutRunnable, parser.secondsParsedFromExpiration());
//            return null;
//        } else {
//
//            if (EngageConfig.currentAddressCache() == null || EngageConfig.addressCacheExpired(mAppContext)) {
//
//                //Place a timeout on how long it takes from the address to be acquired.
//                //If that timeout is reached then we want to post the UBF event without location information.
//                String locAcqTimeout = EngageConfigManager.get(mAppContext).augmentationTimeout();
//                EngageExpirationParser parser = new EngageExpirationParser(locAcqTimeout, new Date());
//                long expirationSeconds = parser.secondsParsedFromExpiration();
//
//                Handler addressHandlerTimeout = new Handler();
//
//                Runnable addressTimeoutRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        //Make sure that the placemark hasn't been acquired since the expiration timer was set for this callback.
//                        if (EngageConfig.currentAddressCache() == null || EngageConfig.addressCacheExpired(mAppContext)) {
//                            UBFClient.get(mAppContext).postUBFEngageEvents(null, null);
//                        }
//                    }
//                };
//
//                EngageConfigManager cm = EngageConfigManager.get(mAppContext);
//                EngageExpirationParser exp = new EngageExpirationParser(cm.augmentationTimeout(), new Date());
//
//                addressHandlerTimeout.postDelayed(addressTimeoutRunnable, exp.secondsParsedFromExpiration());
//
//                return null;
//            } else {
//
//                EngageConfigManager cm = EngageConfigManager.get(mAppContext);
//
//                //Sets the Longitude and Latitude
//                if (EngageConfig.currentLocationCache() != null) {
//                    if (!ubfEvent.getParams().containsKey(cm.ubfLongitudeFieldName())) {
//                        ubfEvent.addParam(cm.ubfLongitudeFieldName(), EngageConfig.currentLocationCache().getLongitude());
//                    }
//                    if (!ubfEvent.getParams().containsKey(cm.ubfLatitudeFieldName())) {
//                        ubfEvent.addParam(cm.ubfLatitudeFieldName(), EngageConfig.currentLocationCache().getLatitude());
//                    }
//                }
//
//                //Sets the location name and address.
//                if (EngageConfig.currentAddressCache() != null) {
//                    if (!ubfEvent.getParams().containsKey(cm.ubfLocationNameFieldName())) {
//                        Address address = EngageConfig.currentAddressCache();
//                        String locationName = "";
//                        if (address.getFeatureName() != null) {
//                            locationName = address.getFeatureName();
//                        }
//                        ubfEvent.addParam(cm.ubfLocationNameFieldName(), locationName);
//                    }
//                    if (!ubfEvent.getParams().containsKey(cm.ubfLocationAddressFieldName())) {
//                        Address address = EngageConfig.currentAddressCache();
//                        StringBuilder builder = new StringBuilder();
//                        builder.append(address.getSubAdminArea());
//                        builder.append(", ");
//                        builder.append(address.getAdminArea());
//                        builder.append(" ");
//                        builder.append(address.getPostalCode());
//                        ubfEvent.addParam(cm.ubfLocationAddressFieldName(), builder.toString());
//                    }
//                }
//
//                return ubfEvent;
//            }
//        }
//    }
}
