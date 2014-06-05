package com.silverpop.engage.augmentation.plugin.impl;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.silverpop.engage.augmentation.plugin.UBFAugmentationPlugin;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public class UBFLocationAugmentationPlugin
    implements UBFAugmentationPlugin {

    private static final String TAG = UBFLocationAugmentationPlugin.class.getName();

    private Context mAppContext = null;

    public UBFLocationAugmentationPlugin(Context context) {
        mAppContext = context;
    }

    @Override
    public boolean isSupplementalDataReady() {
        if (EngageConfig.currentLocationCache() == null
                || EngageConfig.currentAddressCache() == null
                || EngageConfig.addressCacheExpired(mAppContext)) {
            return false;
        }
        return true;
    }

    @Override
    public UBF process(UBF ubfEvent) {
        Location location = EngageConfig.currentLocationCache();
        Address address = EngageConfig.currentAddressCache();

        EngageConfigManager cm = EngageConfigManager.get(mAppContext);

        //Sets the Longitude and Latitude
        if (EngageConfig.currentLocationCache() != null) {
            if (!ubfEvent.getParams().containsKey(cm.ubfLongitudeFieldName())) {
                ubfEvent.addParam(cm.ubfLongitudeFieldName(), location.getLongitude());
            }
            if (!ubfEvent.getParams().containsKey(cm.ubfLatitudeFieldName())) {
                ubfEvent.addParam(cm.ubfLatitudeFieldName(), location.getLatitude());
            }
        }

        //Sets the location name and address.
        if (EngageConfig.currentAddressCache() != null) {
            if (!ubfEvent.getParams().containsKey(cm.ubfLocationNameFieldName())) {
                String locationName = "";
                if (address.getFeatureName() != null) {
                    locationName = address.getFeatureName();
                }
                ubfEvent.addParam(cm.ubfLocationNameFieldName(), locationName);
            }
            if (!ubfEvent.getParams().containsKey(cm.ubfLocationAddressFieldName())) {
                StringBuilder builder = new StringBuilder();
                builder.append(address.getSubAdminArea());
                builder.append(", ");
                builder.append(address.getAdminArea());
                builder.append(" ");
                builder.append(address.getPostalCode());
                ubfEvent.addParam(cm.ubfLocationAddressFieldName(), builder.toString());
            }
        }

        return ubfEvent;
    }
}
