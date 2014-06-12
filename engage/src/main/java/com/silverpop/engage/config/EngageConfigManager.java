package com.silverpop.engage.config;

import android.content.Context;
import android.util.Log;

import com.silverpop.engage.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manager class for reading Engage configuration values.
 *
 * Created by jeremydyer on 6/2/14.
 */
public class EngageConfigManager {

    private static final String TAG = EngageConfigManager.class.getName();

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
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                sdkDefaults =new JSONObject(responseStrBuilder.toString());
            } catch (Exception ex) {
                Log.e(TAG, "Error loading EngageSDK default configurations from : 'EngageConfigDefaults.json'");
            }

            //Loads the user defined configurations.
            try {
                InputStream is = context.getResources().getAssets().open("EngageConfig.json");
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                userDefined =new JSONObject(responseStrBuilder.toString());
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
            return configs.getJSONObject("General").getInt("ubfEventCacheSize");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find ubfEventCacheSize configuration");
            return -1;
        }
    }

    public String defaultCurrentCampaignExpiration() {
        try {
            return configs.getJSONObject("General").getString("defaultCurrentCampaignExpiration");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find defaultCurrentCampaignExpiration configuration");
            return null;
        }
    }

    public String paramCampaignValidFor() {
        try {
            return configs.getJSONObject("ParamFieldNames").getString("paramCampaignValidFor");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCampaignValidFor configuration");
            return null;
        }
    }

    public String paramCampaignExpiresAt() {
        try {
            return configs.getJSONObject("ParamFieldNames").getString("paramCampaignExpiresAt");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCampaignExpiresAt configuration");
            return null;
        }
    }

    public String paramCurrentCampaign() {
        try {
            return configs.getJSONObject("ParamFieldNames").getString("paramCurrentCampaign");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find paramCurrentCampaign configuration");
            return null;
        }
    }

    public String paramCallToAction() {
        try {
            return configs.getJSONObject("ParamFieldNames").getString("paramCallToAction");
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
            return configs.getJSONObject("Networking").getInt("maxNumRetries");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find maxNumRetries configuration");
            return -1;
        }
    }

    public String ubfSessionDurationFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFSessionDurationFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFSessionDurationFieldName configuration");
            return null;
        }
    }

    public String ubfTagsFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFTagsFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFTagsFieldName configuration");
            return null;
        }
    }

    public String ubfDisplayedMessageFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFDisplayedMessageFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFDisplayedMessageFieldName configuration");
            return null;
        }
    }

    public String ubfCallToActionFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFCallToActionFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFCallToActionFieldName configuration");
            return null;
        }
    }

    public String ubfEventNameFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFEventNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFEventNameFieldName configuration");
            return null;
        }
    }

    public String ubfGoalNameFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFGoalNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFGoalNameFieldName configuration");
            return null;
        }
    }

    public String ubfCurrentCampaignFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFCurrentCampaignFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFCurrentCampaignFieldName configuration");
            return null;
        }
    }

    public String ubfLastCampaignFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFLastCampaignFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLastCampaignFieldName configuration");
            return null;
        }
    }

    public String ubfLocationAddressFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFLocationAddressFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLocationAddressFieldName configuration");
            return null;
        }
    }

    public String ubfLocationNameFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFLocationNameFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLocationNameFieldName configuration");
            return null;
        }
    }

    public String ubfLatitudeFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFLatitudeFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLatitudeFieldName configuration");
            return null;
        }
    }

    public String ubfLongitudeFieldName() {
        try {
            return configs.getJSONObject("UBFFieldNames").getString("UBFLongitudeFieldName");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find UBFLongitudeFieldName configuration");
            return null;
        }
    }

    public int locationDistanceFilter() {
        try {
            return configs.getJSONObject("LocationServices").getInt("locationDistanceFilter");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationDistanceFilter configuration");
            return -1;
        }
    }

    public int locationMilliUpdateInterval() {
        try {
            return configs.getJSONObject("LocationServices").getInt("locationMilliUpdateInterval");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationMilliUpdateInterval configuration");
            return -1;
        }
    }

    public String locationCacheLifespan() {
        try {
            return configs.getJSONObject("LocationServices").getString("locationCacheLifespan");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationCacheLifespan configuration");
            return null;
        }
    }

    public String augmentationTimeout() {
        try {
            return configs.getJSONObject("Augmentation").getString("augmentationTimeout");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find augmentationTimeout configuration");
            return null;
        }
    }

    public boolean locationServicesEnabled() {
        try {
            return configs.getJSONObject("LocationServices").getBoolean("locationServicesEnabled");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find locationServicesEnabled configuration");
            return true;
        }
    }

    public String deepLinkScheme() {
        try {
            return configs.getJSONObject("General").getString("deepLinkScheme");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find deepLinkScheme configuration");
            return null;
        }
    }

    public String engageListId() {
        try {
            return configs.getJSONObject("General").getString("engageListId");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find engageListId configuration");
            return null;
        }
    }

    public boolean secureConnection() {
        try {
            return configs.getJSONObject("Networking").getBoolean("secureConnection");
        } catch (JSONException ex) {
            Log.w(TAG, "Unable to find secureConnection configuration");
            return true;
        }
    }
}
