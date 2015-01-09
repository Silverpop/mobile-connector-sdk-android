package com.silverpop.engage;

import android.content.Context;
import android.util.Log;
import com.silverpop.engage.util.uuid.UUIDGenerator;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public class AutoMobileConnectorManager extends BaseManager {

    private static final String TAG = AutoMobileConnectorManager.class.getName();

    private static final String DEFAULT_UUID_GENERATOR_CLASS = "com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator";

    private static AutoMobileConnectorManager instance = null;

    private AutoMobileConnectorManager(Context context) {
        super(context);
    }

    public static synchronized AutoMobileConnectorManager init(Context context) {
        if (instance == null) {
            instance = new AutoMobileConnectorManager(context);
        }
        return instance;
    }

    public static AutoMobileConnectorManager get() {
        if (instance == null) {
            final String error = AutoMobileConnectorManager.class.getName() + " must be initialized before it can be retrieved";
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
