package com.silverpop.engage;

import android.content.Context;
import android.util.Log;
import com.silverpop.engage.util.uuid.UUIDGenerator;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public class MobileConnectorManagerImpl extends BaseManager {

    private static final String TAG = MobileConnectorManagerImpl.class.getName();

    private static final String DEFAULT_UUID_GENERATOR_CLASS = "com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator";

    private static MobileConnectorManagerImpl instance = null;

    private MobileConnectorManagerImpl(Context context) {
        super(context);
    }

    public static synchronized MobileConnectorManagerImpl init(Context context) {
        if (instance == null) {
            instance = new MobileConnectorManagerImpl(context);
        }
        return instance;
    }

    public static MobileConnectorManagerImpl get() {
        if (instance == null) {
            final String error = MobileConnectorManagerImpl.class.getName() + " must be initialized before it can be retrieved";
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
        return instance;
    }


    public String generateMobileUserId() {
        String uuidClassFullPackageName = getEngageConfigManager().mobileUserIdGeneratorClassName();

        UUIDGenerator uuidGenerator;
        try {
            Class uuidClassName = Class.forName(uuidClassFullPackageName);
            uuidGenerator = (UUIDGenerator) uuidClassName.newInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Unable to initialize UUID generator class '" + uuidClassFullPackageName +
                    ".' Using default implementation of " + DEFAULT_UUID_GENERATOR_CLASS + ": " + ex.getMessage());

            uuidGenerator = new DefaultUUIDGenerator();
        }

        String mobileUserId = uuidGenerator.generateUUID();
        return mobileUserId;
    }


}
