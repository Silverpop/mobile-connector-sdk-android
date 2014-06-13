package com.silverpop.engage.augmentation.plugin;

import android.content.Context;
import android.location.Address;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/12/14.
 */
public class UBFAddressAugmentationPlugin
    implements UBFAugmentationPlugin {

    private static final String TAG = UBFAddressAugmentationPlugin.class.getName();

    private Context mContext;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public boolean isSupplementalDataReady() {
        if (EngageConfig.currentAddressCache() == null
                || EngageConfig.addressCacheExpired(mContext)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean processSyncronously() {
        return false;
    }

    @Override
    public UBF process(UBF ubfEvent) {
        Address address = EngageConfig.currentAddressCache();

        EngageConfigManager cm = EngageConfigManager.get(mContext);

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
                ubfEvent.addParam(cm.ubfLocationAddressFieldName(), EngageConfig.buildLocationAddress());
            }
        }

        return ubfEvent;
    }
}
