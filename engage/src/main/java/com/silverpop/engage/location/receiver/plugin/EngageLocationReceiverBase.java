package com.silverpop.engage.location.receiver.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.location.receiver.EngageLocationReceiver;

import java.util.Date;

/**
 * Created by jeremydyer on 6/12/14.
 */
public abstract class EngageLocationReceiverBase
    extends BroadcastReceiver
    implements EngageLocationReceiver {

    private static final String TAG = EngageLocationReceiverBase.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Location loc = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (loc != null) {
            EngageConfig.storeCurrentLocation(loc);
            EngageConfig.storeCurrentLocationCacheBirthday(new Date());
        }
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
            boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }
    }

    protected void onProviderEnabledChanged(boolean enabled) {
        Log.i(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
    }
}
