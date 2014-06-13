package com.silverpop.engage.location.receiver.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.silverpop.engage.R;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.XMLAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * When the Geocode class is not present on certain devices this class
 * can be used in its place to query Google maps to determine the reverse
 * Geocode information.
 *
 * Created by jeremydyer on 6/12/14.
 */
public class EngageLocationReceiverHardcodeTest
    extends EngageLocationReceiverBase {

    private static final String TAG = EngageLocationReceiverHardcodeTest.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Reverse Geocoding with " + EngageLocationReceiverHardcodeTest.class.getName());
        onLocationReceived(context, (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED));
    }

    protected void onLocationReceived(Context context, Location loc) {
        if (loc != null && EngageConfig.addressCacheExpired(context)) {

            Resources r = context.getResources();
            Address address = new Address(Locale.US);
            address.setLongitude(loc.getLongitude());
            address.setLatitude(loc.getLatitude());
            address.setLocality(r.getString(R.string.locality));
            address.setAdminArea(r.getString(R.string.adminArea));
            address.setPostalCode(r.getString(R.string.postalCode));
            address.setCountryName(r.getString(R.string.countryName));
            address.setCountryCode(r.getString(R.string.countryCode));
            address.setFeatureName(r.getString(R.string.featureName));

            EngageConfig.storeCurrentAddressCache(address);
            EngageConfig.storeCurrentAddressCacheExpiration(new Date());

            updateUserLastKnownLocation(context);
        } else {
            Log.d(TAG, "Using current Address cache value");
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
