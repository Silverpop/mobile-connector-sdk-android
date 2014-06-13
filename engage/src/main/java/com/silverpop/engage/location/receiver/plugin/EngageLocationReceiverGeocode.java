package com.silverpop.engage.location.receiver.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.silverpop.engage.R;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.location.manager.EngageLocationManager;
import com.silverpop.engage.util.EngageExpirationParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jeremydyer on 6/12/14.
 */
public class EngageLocationReceiverGeocode
        extends EngageLocationReceiverBase {

    private static final String TAG = EngageLocationManager.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Reverse Geocoding with " + EngageLocationReceiverGeocode.class.getName());
        onLocationReceived(context, (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED));
    }

    protected void onLocationReceived(Context context, Location loc) {

        if (Geocoder.isPresent()) {
            if (EngageConfig.addressCacheExpired(context)) {
                Log.d(TAG, "Address cache is expired. ReverseGeocoding Location Lat: "
                        + loc.getLatitude() + " - Long: " + loc.getLongitude());

                GeocoderAsyncTask geocoderAsyncTask = new GeocoderAsyncTask(context);
                geocoderAsyncTask.execute(loc.getLongitude(), loc.getLatitude());
            }
        }
    }

    private class GeocoderAsyncTask
            extends AsyncTask<Double, Void, Address> {

        private Context context;

        public GeocoderAsyncTask(Context context) {
            this.context = context;
        }

        protected Address doInBackground(Double... loc) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            Double longitude = loc[0];
            Double latitude = loc[1];
            try {
                List<android.location.Address> geoCodeAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (geoCodeAddresses != null && geoCodeAddresses.size() > 0) {
                    EngageConfig.storeCurrentAddressCache(geoCodeAddresses.get(0));

                    Date currentAddressBirthDay = new Date();
                    EngageConfig.storeCurrentAddressCacheBirthday(currentAddressBirthDay);

                    String locAcqTimeout = EngageConfigManager.get(context).locationCacheLifespan();
                    EngageExpirationParser exp = new EngageExpirationParser(locAcqTimeout, currentAddressBirthDay);
                    EngageConfig.storeCurrentAddressCacheExpiration(exp.expirationDate());

                    updateUserLastKnownLocation(context);

                    return EngageConfig.currentAddressCache();

                } else {
                    Log.w(TAG, "Unable to Geocode address for Lat: "
                            + latitude + " - Long: " + longitude);
                }
            } catch (IOException e) {
                Log.d(TAG, "Geocoder network service offline");
            }

            return null;
        }
    }

    protected void updateUserLastKnownLocation(Context context) {
        Resources r = context.getResources();
        String lastKnownLocationColumn = EngageConfigManager.get(context).lastKnownLocationColumn();
        String lastKnownLocationTimestampColumn = EngageConfigManager.get(context).lastKnownLocationTimestampColumn();

        String lastKnownLocationTimeFormat = EngageConfigManager.get(context).lastKnownLocationDateFormat();
        SimpleDateFormat sdf = new SimpleDateFormat(lastKnownLocationTimeFormat);

        //Make XMLAPI request to update the last known location.
        Map<String, Object> bodyElements = new HashMap<String, Object>();
        bodyElements.put("LIST_ID", EngageConfigManager.get(context).engageListId());
        bodyElements.put("CREATED_FROM", "1");
        XMLAPI updateLastKnownLocation = new XMLAPI("UpdateRecipient", bodyElements);
        Map<String, Object> syncFields = new HashMap<String, Object>();
        syncFields.put("EMAIL", EngageConfig.primaryUserId(context));
        updateLastKnownLocation.addSyncFields(syncFields);
        Map<String, Object> cols = new HashMap<String, Object>();
        cols.put(lastKnownLocationColumn, sdf.format(new Date()));
        cols.put(lastKnownLocationTimestampColumn, EngageConfig.buildLocationAddress());
        updateLastKnownLocation.addColumns(cols);

        String env = updateLastKnownLocation.envelope();
        Log.d(TAG, env);
        XMLAPIManager.get().postXMLAPI(updateLastKnownLocation, null, null);
    }
}
