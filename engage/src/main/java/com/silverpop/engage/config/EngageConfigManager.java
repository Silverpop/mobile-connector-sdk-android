package com.silverpop.engage.config;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manager class for reading Engage configuration values read from the
 * EngageConfigDefault.json/EngageConfig.json file.
 *
 * Created by jeremydyer on 6/2/14.
 */
public class EngageConfigManager {

    private static final String TAG = EngageConfigManager.class.getName();

    enum Config {
        UBF_FILED_NAMES("UBFFieldNames"),
        PARAM_FIELD_NAMES("ParamFieldNames"),
        LOCATION_SERVICES("LocationServices"),
        AUGMENTATION("Augmentation"),
        GENERAL("General"),
        NETWORKING("Networking"),
        PLUGGABLE_SERVICES("PluggableServices"),
        RECIPIENT("Recipient"),
        AUDIT_RECORD("AuditRecord");

        final String value;

        private Config(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    private Context mAppContext = null;
    private static EngageConfigManager configManager = null;

    private static JSONObject configs = null;

    private EngageConfigManager(Context context) {
        mAppContext = context;
    }

    public static EngageConfigManager get(Context context) {
        if (configManager == null) {
            configManager = new EngageConfigManager(context);

            //Loads the SDK default configurations.
            JSONObject sdkDefaults = null;
            JSONObject userDefined = null;
            try {
                InputStream is = context.getResources().getAssets().open("EngageConfigDefault.json");
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }

                sdkDefaults = new JSONObject(responseStrBuilder.toString());
            } catch (Exception ex) {
                Log.e(TAG, "Error loading EngageSDK default configurations from : 'EngageConfigDefaults.json'");
            }

            //Loads the user defined configurations.
            try {
                InputStream is = context.getResources().getAssets().open("EngageConfig.json");
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }

                userDefined = new JSONObject(responseStrBuilder.toString());
            } catch (Exception ex) {
                Log.e(TAG, "Error loading EngageSDK default configurations from : 'EngageConfigDefaults.json'");
            }

            try {
                if (sdkDefaults != null || userDefined != null) {
                    if (sdkDefaults != null && userDefined != null) {
                        if (sdkDefaults.names() != null) {
                            String[] defaultNames = jsonObjectNames(sdkDefaults);
                            JSONObject merged = new JSONObject(sdkDefaults, defaultNames);

                            //Merges in the user values now.
                            String[] userNames = jsonObjectNames(userDefined);

                            for (String key : userNames) {
                                merged.put(key, userDefined.get(key));
                            }

                            configs = merged;

                        } else {
                            Log.w(TAG, "No JSON values found in loaded configurations! " +
                                    "Certain operations may not operate properly!");
                        }
                    } else if (userDefined != null) {
                        configs = userDefined;
                    } else {
                        configs = sdkDefaults;
                    }

                } else {
                    Log.e(TAG, "EngageSDK - Unable to load SDK configuration values. " +
                            "Certain operations may not perform");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return configManager;
    }

    private static String[] jsonObjectNames(JSONObject object) {
        try {
            String[] names = new String[object.names().length()];
            for (int i = 0; i < object.names().length(); i++) {
                names[i] = object.names().getString(i);
            }
            return names;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[0];
        }
    }

    public int expireLocalEventsAfterNumDays() {
        try {
            return configs.getJSONObject("LocalEventStore").getInt("expireLocalEventsAfterNumDays");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find expireLocalEventsAfterNumDays configuration");
            return -1;
        }
    }

    public int ubfEventCacheSize() {
        try {
            return getGeneralConfigJson().getInt("ubfEventCacheSize");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find ubfEventCacheSize configuration");
            return -1;
        }
    }

    public String defaultCurrentCampaignExpiration() {
        try {
            return getGeneralConfigJson().getString("defaultCurrentCampaignExpiration");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find defaultCurrentCampaignExpiration configuration");
            return null;
        }
    }

    public String paramCampaignValidFor() {
        try {
            return getParamFieldNamesConfigJson().getString("paramCampaignValidFor");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCampaignValidFor configuration");
            return null;
        }
    }

    public String paramCampaignExpiresAt() {
        try {
            return getParamFieldNamesConfigJson().getString("paramCampaignExpiresAt");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCampaignExpiresAt configuration");
            return null;
        }
    }

    public String paramCurrentCampaign() {
        try {
            return getParamFieldNamesConfigJson().getString("paramCurrentCampaign");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCurrentCampaign configuration");
            return null;
        }
    }

    public String paramCallToAction() {
        try {
            return getParamFieldNamesConfigJson().getString("paramCallToAction");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCallToAction configuration");
            return null;
        }
    }

    public String sessionLifecycleExpiration() {
        try {
            return configs.getJSONObject("Session").getString("sessionLifecycleExpiration");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find sessionLifecycleExpiration configuration");
            return null;
        }
    }

    public int maxNumRetries() {
        try {
            return getNetworkingConfigJson().getInt("maxNumRetries");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find maxNumRetries configuration");
            return -1;
        }
    }

    public String ubfSessionDurationFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFSessionDurationFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFSessionDurationFieldName configuration");
            return null;
        }
    }

    public String ubfTagsFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFTagsFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFTagsFieldName configuration");
            return null;
        }
    }

    public String ubfDisplayedMessageFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFDisplayedMessageFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFDisplayedMessageFieldName configuration");
            return null;
        }
    }

    public String ubfCallToActionFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFCallToActionFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFCallToActionFieldName configuration");
            return null;
        }
    }

    public String ubfEventNameFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFEventNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFEventNameFieldName configuration");
            return null;
        }
    }

    public String ubfGoalNameFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFGoalNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFGoalNameFieldName configuration");
            return null;
        }
    }

    public String ubfCurrentCampaignFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFCurrentCampaignFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFCurrentCampaignFieldName configuration");
            return null;
        }
    }

    public String ubfLastCampaignFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFLastCampaignFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLastCampaignFieldName configuration");
            return null;
        }
    }

    public String ubfLocationAddressFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFLocationAddressFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLocationAddressFieldName configuration");
            return null;
        }
    }

    public String ubfLocationNameFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFLocationNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLocationNameFieldName configuration");
            return null;
        }
    }

    public String ubfLatitudeFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFLatitudeFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLatitudeFieldName configuration");
            return null;
        }
    }

    public String ubfLongitudeFieldName() {
        try {
            return getUBFFieldNamesConfigJson().getString("UBFLongitudeFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLongitudeFieldName configuration");
            return null;
        }
    }

    public int locationDistanceFilter() {
        try {
            return getLocationServicesConfigJson().getInt("locationDistanceFilter");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationDistanceFilter configuration");
            return -1;
        }
    }

    public int locationMilliUpdateInterval() {
        try {
            return getLocationServicesConfigJson().getInt("locationMilliUpdateInterval");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationMilliUpdateInterval configuration");
            return -1;
        }
    }

    public String locationCacheLifespan() {
        try {
            return getLocationServicesConfigJson().getString("locationCacheLifespan");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationCacheLifespan configuration");
            return null;
        }
    }

    public String lastKnownLocationColumn() {
        try {
            return getLocationServicesConfigJson().getString("lastKnownLocationColumn");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find lastKnownLocationColumn configuration");
            return null;
        }
    }

    public String lastKnownLocationTimestampColumn() {
        try {
            return getLocationServicesConfigJson().getString("lastKnownLocationTimestampColumn");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find lastKnownLocationTimestampColumn configuration");
            return null;
        }
    }

    public String lastKnownLocationDateFormat() {
        try {
            return getLocationServicesConfigJson().getString("lastKnownLocationDateFormat");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find lastKnownLocationDateFormat configuration");
            return null;
        }
    }

    public String augmentationTimeout() {
        try {
            return getAugmentationConfigJson().getString("augmentationTimeout");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find augmentationTimeout configuration");
            return null;
        }
    }

    public boolean locationServicesEnabled() {
        try {
            return getLocationServicesConfigJson().getBoolean("locationServicesEnabled");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationServicesEnabled configuration");
            return true;
        }
    }

    public String deepLinkScheme() {
        try {
            return getGeneralConfigJson().getString("deepLinkScheme");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find deepLinkScheme configuration");
            return null;
        }
    }

    public String engageListId() {
        try {
            return getGeneralConfigJson().getString("engageListId");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find engageListId configuration");
            return null;
        }
    }

    public boolean secureConnection() {
        try {
            return getNetworkingConfigJson().getBoolean("secureConnection");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find secureConnection configuration");
            return true;
        }
    }

    public String pluggableLocationManagerClassName() {
        try {
            return getPluggableServiceConfigJson().getString("pluggableLocationManagerClassName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find pluggableLocationManagerClassName configuration");
            return null;
        }
    }

    public String[] augmentationPluginClasses() {
        try {
            String[] augmentationClasses = jsonArrayToStringArray(
                    getAugmentationConfigJson().getJSONArray("ubfAugmentorClassNames"));
            return augmentationClasses;
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find augmentationPluginClasses configuration");
            return null;
        }
    }

    public boolean enableAutoAnonymousTracking() {
        String propName = "enableAutoAnonymousTracking";
        boolean enableAutoAnonymousTracking = false;
        try {
            enableAutoAnonymousTracking = getRecipientConfigJson().getBoolean(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  Defaulting to false.");
        }
        return enableAutoAnonymousTracking;
    }

    public String mobileUserIdGeneratorClassName() {
        String propName = "mobileUserIdGeneratorClassName";
        String mobileUserIdGeneratorClassName = null;
        try {
            mobileUserIdGeneratorClassName = getRecipientConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return mobileUserIdGeneratorClassName;
    }

    public String mobileUserIdColumnName() {
        String propName = "mobileUserIdColumn";
        String mobileUserIdColumnName = null;
        try {
            mobileUserIdColumnName = getRecipientConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return mobileUserIdColumnName;
    }

    public String mergedRecipientIdColumnName() {
        String propName = "mergedRecipientIdColumn";
        String mergedRecipientIdColumnName = null;
        try {
            mergedRecipientIdColumnName = getRecipientConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return mergedRecipientIdColumnName;
    }

    public String mergedDateColumnName() {
        String propName = "mergedDateColumn";
        String mergedDateColumnName = null;
        try {
            mergedDateColumnName = getRecipientConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return mergedDateColumnName;
    }

    public String auditRecordOldRecipientIdColumnName() {
        String propName = "oldRecipientIdColumnName";
        String oldRecipientIdColumnName = null;
        try {
            oldRecipientIdColumnName = getAuditRecordConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return oldRecipientIdColumnName;
    }
    public String auditRecordNewRecipientIdColumnName() {
        String propName = "newRecipientIdColumnName";
        String newRecipientIdColumnName = null;
        try {
            newRecipientIdColumnName = getAuditRecordConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return newRecipientIdColumnName;
    }
    public String auditRecordCreateDateColumnName() {
        String propName = "createDateColumnName";
        String createDateColumnName = null;
        try {
            createDateColumnName = getAuditRecordConfigJson().getString(propName);
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find " + propName + " configuration.  ");
        }
        return createDateColumnName;
    }

    private JSONObject getGeneralConfigJson() throws JSONException {
        return getConfigJson(Config.GENERAL);
    }

    private JSONObject getAugmentationConfigJson() throws JSONException {
        return getConfigJson(Config.AUGMENTATION);
    }

    private JSONObject getLocationServicesConfigJson() throws JSONException {
        return getConfigJson(Config.LOCATION_SERVICES);
    }

    private JSONObject getUBFFieldNamesConfigJson() throws JSONException {
        return getConfigJson(Config.UBF_FILED_NAMES);
    }

    private JSONObject getNetworkingConfigJson() throws JSONException {
        return getConfigJson(Config.NETWORKING);
    }

    private JSONObject getParamFieldNamesConfigJson() throws JSONException {
        return getConfigJson(Config.PARAM_FIELD_NAMES);
    }

    private JSONObject getPluggableServiceConfigJson() throws JSONException {
        return getConfigJson(Config.PLUGGABLE_SERVICES);
    }

    private JSONObject getRecipientConfigJson() throws JSONException {
        return getConfigJson(Config.RECIPIENT);
    }
    private JSONObject getAuditRecordConfigJson() throws JSONException {
        return getConfigJson(Config.AUDIT_RECORD);
    }

    private JSONObject getConfigJson(Config config) throws JSONException {
        return configs.getJSONObject(config.toString());
    }

    private String[] jsonArrayToStringArray(JSONArray jsonArray) {
        try {
            if (jsonArray != null) {
                String[] values = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    values[i] = jsonArray.getString(i);
                }
                return values;
            } else {
                return new String[0];
            }
        } catch (JSONException ex) {
            Log.w(TAG, "Error converting JSONArray to String[]. Returning empty String[0]");
            return new String[0];
        }
    }
}
