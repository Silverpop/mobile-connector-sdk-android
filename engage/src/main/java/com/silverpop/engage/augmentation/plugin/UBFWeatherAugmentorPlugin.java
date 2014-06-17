package com.silverpop.engage.augmentation.plugin;

import android.content.Context;
import android.location.Location;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/13/14.
 */
public class UBFWeatherAugmentorPlugin
        implements UBFAugmentationPlugin {

    private static final String TAG = UBFCoordinatesAugmentationPlugin.class.getName();

    private Context mContext;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public boolean isSupplementalDataReady() {
        return true;
    }

    @Override
    public boolean processSyncronously() {
        return false;
    }

    @Override
    public UBF process(UBF ubfEvent) {

        EngageConfigManager cm = EngageConfigManager.get(mContext);

        ubfEvent.addParam("Temperature", "80");
        ubfEvent.addParam("wizometer", "5");

        return ubfEvent;
    }
}
