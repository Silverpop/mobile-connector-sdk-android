package com.silverpop.engage.augmentation.plugin;

import android.content.Context;
import android.location.Location;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/12/14.
 */
public class UBFCoordinatesAugmentationPlugin
    implements UBFAugmentationPlugin {

    private static final String TAG = UBFCoordinatesAugmentationPlugin.class.getName();

    private Context mContext;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public boolean isSupplementalDataReady() {
        if (EngageConfig.currentLocationCache() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean processSyncronously() {
        return true;
    }

    @Override
    public UBF process(UBF ubfEvent) {
        Location location = EngageConfig.currentLocationCache();

        EngageConfigManager cm = EngageConfigManager.get(mContext);

        //Sets the Longitude and Latitude
        if (EngageConfig.currentLocationCache() != null) {
            if (!ubfEvent.getParams().containsKey(cm.ubfLongitudeFieldName())) {
                ubfEvent.addParam(cm.ubfLongitudeFieldName(), location.getLongitude());
            }
            if (!ubfEvent.getParams().containsKey(cm.ubfLatitudeFieldName())) {
                ubfEvent.addParam(cm.ubfLatitudeFieldName(), location.getLatitude());
            }
        }

        return ubfEvent;
    }
}
