package com.silverpop.engage.config;

import android.content.Context;

import com.silverpop.engage.R;

/**
 * Manager class for reading Engage configuration values.
 *
 * Created by jeremydyer on 6/2/14.
 */
public class EngageConfigManager {

    private Context mAppContext = null;
    private static EngageConfigManager configManager = null;

    private EngageConfigManager(Context context) {
        mAppContext = context;
    }

    public static EngageConfigManager get(Context context) {
        if (configManager == null) {
            configManager = new EngageConfigManager(context);
        }
        return configManager;
    }

    public int expireLocalEventsAfterNumDays() {
        return mAppContext.getResources().getInteger(R.integer.expireLocalEventsAfterNumDays);
    }

    public int ubfEventCacheSize() {
        return mAppContext.getResources().getInteger(R.integer.ubfEventCacheSize);
    }

    public String defaultCurrentCampaignExpiration() {
        return mAppContext.getResources().getString(R.string.defaultCurrentCampaignExpiration);
    }

    public String paramCampaignValidFor() {
        return mAppContext.getResources().getString(R.string.paramCampaignValidFor);
    }

    public String paramCampaignExpiresAt() {
        return mAppContext.getResources().getString(R.string.paramCampaignExpiresAt);
    }

    public String paramCurrentCampaign() {
        return mAppContext.getResources().getString(R.string.paramCurrentCampaign);
    }

    public String paramCallToAction() {
        return mAppContext.getResources().getString(R.string.paramCallToAction);
    }

    public String sessionLifecycleExpiration() {
        return mAppContext.getResources().getString(R.string.sessionLifecycleExpiration);
    }

    public int maxNumRetries() {
        return mAppContext.getResources().getInteger(R.integer.maxNumRetries);
    }

    public String ubfSessionDurationFieldName() {
        return mAppContext.getResources().getString(R.string.UBFSessionDurationFieldName);
    }

    public String ubfTagsFieldName() {
        return mAppContext.getResources().getString(R.string.UBFTagsFieldName);
    }

    public String ubfDisplayedMessageFieldName() {
        return mAppContext.getResources().getString(R.string.UBFDisplayedMessageFieldName);
    }

    public String ubfCallToActionFieldName() {
        return mAppContext.getResources().getString(R.string.UBFCallToActionFieldName);
    }

    public String ubfEventNameFieldName() {
        return mAppContext.getResources().getString(R.string.UBFEventNameFieldName);
    }

    public String ubfGoalNameFieldName() {
        return mAppContext.getResources().getString(R.string.UBFGoalNameFieldName);
    }

    public String ubfCurrentCampaignFieldName() {
        return mAppContext.getResources().getString(R.string.UBFCurrentCampaignFieldName);
    }

    public String ubfLastCampaignFieldName() {
        return mAppContext.getResources().getString(R.string.UBFLastCampaignFieldName);
    }

    public String ubfLocationAddressFieldName() {
        return mAppContext.getResources().getString(R.string.UBFLocationAddressFieldName);
    }

    public String ubfLocationNameFieldName() {
        return mAppContext.getResources().getString(R.string.UBFLocationNameFieldName);
    }

    public String ubfLatitudeFieldName() {
        return mAppContext.getResources().getString(R.string.UBFLatitudeFieldName);
    }

    public String ubfLongitudeFieldName() {
        return mAppContext.getResources().getString(R.string.UBFLongitudeFieldName);
    }

    public int locationDistanceFilter() {
        return mAppContext.getResources().getInteger(R.integer.locationDistanceFilter);
    }

    public int locationMilliUpdateInterval() {
        return mAppContext.getResources().getInteger(R.integer.locationMilliUpdateInterval);
    }

    public String locationCacheLifespan() {
        return mAppContext.getResources().getString(R.string.locationCacheLifespan);
    }

    public String augmentationTimeout() {
        return mAppContext.getResources().getString(R.string.augmentationTimeout);
    }

//    public String coordinatesAddressAcquisitionTimeout() {
//        return mAppContext.getResources().getString(R.string.coordinatesAddressAcquisitionTimeout);
//    }
//
//    public String coordinatesLongLatAcquisitionTimeout() {
//        return mAppContext.getResources().getString(R.string.coordinatesLongLatAcquisitionTimeout);
//    }

    public boolean locationServicesEnabled() {
        return mAppContext.getResources().getBoolean(R.bool.locationServicesEnabled);
    }

    public String deepLinkScheme() {
        return mAppContext.getResources().getString(R.string.deepLinkScheme);
    }
}
