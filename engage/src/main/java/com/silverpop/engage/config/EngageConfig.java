package com.silverpop.engage.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.silverpop.engage.util.EngageExpirationParser;

import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageConfig {

    public static final String ENGAGE_CONFIG_PREF_ID = "com.silverpop.engage.EngageSDKPrefs";
    public static final String PRIMARY_USER_ID = "PRIMARY_USER_ID";
    public static final String ANONYMOUS_ID = "ANONYMOUS_ID";
    public static final String CURRENT_CAMPAIGN = "CURRENT_CAMPAIGN";

    private static Location currentLocationCache;
    private static Date currentLocationCacheBirthday;
    private static Address currentAddressCache;
    private static Date currentAddressCacheBirthday;
    private static Date currentAddressExpirationDate = null;

    private static Date campaignExpirationDate = null;

    public static String deviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String deviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String deviceVersion() {
        return Build.PRODUCT;
    }

    public static String primaryUserId(Context context) {
        return context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE).getString(PRIMARY_USER_ID, "");
    }

    public static String osName(Context context) {
        return Build.VERSION.CODENAME;
    }

    public static String osVersion(Context context) {
        return new Integer(Build.VERSION.SDK_INT).toString();
    }

    public static String appName(Context context) {
        PackageInfo pInfo = null;
        String appName = "UNKNOWN";
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    public static String appVersion(Context context) {
        PackageInfo pInfo = null;
        int version = -1;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version > -1) {
            return new Integer(version).toString();
        } else {
            return "UNKNOWN";
        }
    }

    public static void storePrimaryUserId(Context context, String primaryUserId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRIMARY_USER_ID, primaryUserId);
        editor.commit();
    }

    public static String anonymousUserId(Context context) {
        return context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE).getString(ANONYMOUS_ID, "");
    }

    public static void storeAnonymousUserId(Context context, String anonymousUserId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ANONYMOUS_ID, anonymousUserId).commit();
    }

    public static String currentCampaign(Context context) {
        if (campaignExpirationDate != null) {
            if (campaignExpirationDate.compareTo(new Date()) > 0) {
                return context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE).getString(CURRENT_CAMPAIGN, "");
            } else {
                return "";
            }
        } else {
            //If the campaign expiration date is null that means the campaign never expires.
            return context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE).getString(CURRENT_CAMPAIGN, "");
        }
    }

    public static void storeCurrentCampaignWithExpirationTimestamp(Context context, String currentCampaign, long expirationTimestamp) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(CURRENT_CAMPAIGN, currentCampaign).commit();

        if (expirationTimestamp > 0) {
            campaignExpirationDate = new Date(expirationTimestamp);
        } else {
            campaignExpirationDate = null;
        }
    }

    public static String lastCampaign(Context context) {
        return context.getSharedPreferences(ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE).getString(CURRENT_CAMPAIGN, null);
    }

    public static void storeCurrentLocation(Location location) {
        currentLocationCache = location;
    }

    public static Location currentLocationCache() {
        return currentLocationCache;
    }

    public static void storeCurrentAddressCache(Address address) {
        currentAddressCache = address;
    }

    public static Address currentAddressCache() {
        return currentAddressCache;
    }

    public static void storeCurrentAddressCacheExpiration(Date expirationDate) {
        currentAddressExpirationDate = expirationDate;
    }

    public static Date currentAddressCacheExpiration() {
        return currentAddressExpirationDate;
    }

    public static void storeCurrentLocationCacheBirthday(Date birthday) {
        currentLocationCacheBirthday = birthday;
    }

    public static Date currentLocationCacheBirthday() {
        return currentLocationCacheBirthday;
    }

    public static void storeCurrentAddressCacheBirthday(Date birthday) {
        currentAddressCacheBirthday = birthday;
    }

    public static Date currentAddressCacheBirthday() {
        return currentAddressCacheBirthday;
    }

    /**
     * Tests if the current address cache is expired or not.
     *
     * @return
     */
    public static boolean addressCacheExpired(Context context) {
        boolean expired = false;

        if (EngageConfig.currentAddressCacheBirthday() != null) {
            if (EngageConfig.currentAddressCacheExpiration() == null) {
                String locAcqTimeout = EngageConfigManager.get(context).augmentationTimeout();
                EngageExpirationParser parser = new EngageExpirationParser(locAcqTimeout, EngageConfig.currentAddressCacheBirthday());
                EngageConfig.storeCurrentAddressCacheExpiration(parser.expirationDate());
            }

            Date now = new Date();
            if (now.compareTo(EngageConfig.currentAddressCacheExpiration()) > 0) {
                expired = true;
                EngageConfig.storeCurrentAddressCache(null);
                EngageConfig.storeCurrentAddressCacheBirthday(null);
                EngageConfig.storeCurrentAddressCacheExpiration(null);
            }
        }

        return expired;
    }
}