package com.silverpop.engage.location.bak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.silverpop.engage.R;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.util.EngageExpirationParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jeremydyer on 6/2/14.
 */
public class EngageLocationReceiver
        extends BroadcastReceiver {

    private static final String TAG = com.silverpop.engage.location.manager.EngageLocationManager.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Location loc = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (loc != null) {
            onLocationReceived(context, loc);
        }
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
            boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }
    }

    protected void onLocationReceived(Context context, Location loc) {

        Log.d(TAG, this + " Got location from " + loc.getProvider() + " : " + loc.getLatitude() + ", " + loc.getLongitude());
        EngageConfig.storeCurrentLocation(loc);
        EngageConfig.storeCurrentLocationCacheBirthday(new Date());

        if (Geocoder.isPresent()) {
            if (EngageConfig.addressCacheExpired(context)) {
                Log.d(TAG, "Address cache is expired. ReverseGeocoding Location Lat: "
                        + loc.getLatitude() + " - Long: " + loc.getLongitude());
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                try {
                    List<Address> geoCodeAddresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                    if (geoCodeAddresses != null && geoCodeAddresses.size() > 0) {
                        EngageConfig.storeCurrentAddressCache(geoCodeAddresses.get(0));

                        Date currentAddressBirthDay = new Date();
                        EngageConfig.storeCurrentAddressCacheBirthday(currentAddressBirthDay);

                        String locAcqTimeout = EngageConfigManager.get(context).locationCacheLifespan();
                        EngageExpirationParser exp = new EngageExpirationParser(locAcqTimeout, currentAddressBirthDay);
                        EngageConfig.storeCurrentAddressCacheExpiration(exp.expirationDate());

                        Resources r = context.getResources();
                        String lastKnownLocationColumn = r.getString(R.string.lastKnownLocationTimestampColumn);
                        String lastKnownLocationTimestampColumn = r.getString(R.string.lastKnownLocationColumn);

                        String lastKnownLocationTimeFormat = r.getString(R.string.lastKnownLocationDateFormat);
                        SimpleDateFormat sdf = new SimpleDateFormat(lastKnownLocationTimeFormat);

                        //Make XMLAPI request to update the last known location.
                        Map<String, Object> bodyElements = new HashMap<String, Object>();
                        bodyElements.put("LIST_ID", "listid");
                        bodyElements.put("VISITOR_KEY", "example visitor id");
                        bodyElements.put("CREATED_FROM", "1");
                        XMLAPI updateLastKnownLocation = new XMLAPI("UpdateRecipient", bodyElements);
                        Map<String, Object> syncFields = new HashMap<String, Object>();
                        syncFields.put("EMAIL", "jeremy.dyer@makeandbuild.com");
                        updateLastKnownLocation.addSyncFields(syncFields);
                        Map<String, Object> cols = new HashMap<String, Object>();
                        cols.put(lastKnownLocationColumn, sdf.format(new Date()));
                        cols.put(lastKnownLocationTimestampColumn, EngageConfig.buildLocationAddress());
                        updateLastKnownLocation.addColumns(cols);

                        String env = updateLastKnownLocation.envelope();
                        Log.d(TAG, env);
                        XMLAPIManager.get().postXMLAPI(updateLastKnownLocation, null, null);

                    } else {
                        Log.w(TAG, "Unable to Geocode address for Lat: "
                                + loc.getLatitude() + " - Long: " + loc.getLongitude());
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Geocoder network service offline");
                }
            }
        }
    }

    protected void onProviderEnabledChanged(boolean enabled) {
        Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
    }
}
