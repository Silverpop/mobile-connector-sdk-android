package com.silverpop.engage.config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;

import android.text.TextUtils;
import com.silverpop.engage.util.EngageExpirationParser;

import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageConfig {

    public enum SharedProperties {
        ENGAGE_CONFIG_PREF_ID("com.silverpop.engage.EngageSDKPrefs"),
        PRIMARY_USER_ID("PRIMARY_USER_ID"),
        /**
         * Only still supported for legacy for users who still manually configure their recipients.
         * The new recipient setup now uses {@link #RECIPIENT_ID}
         */
        ANONYMOUS_ID("ANONYMOUS_ID"),
        RECIPIENT_ID("RECIPIENT_ID"),
        CURRENT_CAMPAIGN("CURRENT_CAMPAIGN"),
        CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP("CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP"),
        APP_INSTALLED("APP_INSTALLED"),
        SESSION("SESSION"),
        AUDIT_RECORD_TABLE_ID("AUDIT_RECORD_TABLE_ID");

        private final String key;

        private SharedProperties(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public static final String PRIMARY_USER_ID_SET_EVENT = "com.silverpop.engage.PRIMARY_USER_ID_SET_EVENT";

    private static Location currentLocationCache;
    private static Date currentLocationCacheBirthday;
    private static Address currentAddressCache;
    private static Date currentAddressCacheBirthday;
    private static Date currentAddressExpirationDate = null;

    private static Date campaignExpirationDate = null;

    private static boolean hasCheckedForExpirationFromDisk = false;

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

    public static String mobileUserId(Context context) {
        return getConfigSharedPrefs(context).getString(SharedProperties.PRIMARY_USER_ID.toString(), "");
    }

    public static String recipientId(Context context) {
        String recipientId = getConfigSharedPrefs(context).getString(SharedProperties.RECIPIENT_ID.toString(), "");
        return recipientId;
    }

    public static String osName(Context context) {
        return Build.VERSION.CODENAME;
    }

    public static String osVersion(Context context) {
        return Integer.toString(Build.VERSION.SDK_INT);
    }

    public static String appName(Context context) {
        String appName = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(appName)) {
            appName = "UNKNOWN";
        }
        return appName;
    }

    public static String appVersion(Context context) {
        int version = -1;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version > -1) {
            return Integer.toString(version);
        } else {
            return "UNKNOWN";
        }
    }

    //[Lindsay Thurmond:1/14/15] TODO: add back primary user id methods? even tho mobile user id is the same thing

    public static void storeMobileUserId(Context context, String primaryUserId) {
        getConfigSharedPrefs(context).edit().putString(SharedProperties.PRIMARY_USER_ID.toString(), primaryUserId).commit();

        context.sendBroadcast(new Intent(PRIMARY_USER_ID_SET_EVENT));
    }

    public static void storeRecipientId(Context context, String recipientId) {
        getConfigSharedPrefs(context).edit().putString(SharedProperties.RECIPIENT_ID.toString(), recipientId).commit();
    }

    public static void storeAuditRecordTableId(Context context, String tableId) {
        getConfigSharedPrefs(context).edit().putString(SharedProperties.AUDIT_RECORD_TABLE_ID.toString(), tableId).commit();
    }

    public static String auditRecordTableId(Context context) {
        String auditRecordTableId = getConfigSharedPrefs(context).getString(SharedProperties.AUDIT_RECORD_TABLE_ID.toString(), "");
        return auditRecordTableId;
    }

    public static String anonymousUserId(Context context) {
        return getConfigSharedPrefs(context).getString(SharedProperties.ANONYMOUS_ID.toString(), "");
    }

    public static void storeAnonymousUserId(Context context, String anonymousUserId) {
        getConfigSharedPrefs(context).edit().putString(SharedProperties.ANONYMOUS_ID.toString(), anonymousUserId).commit();
    }

    public static String currentCampaign(Context context) {
        if (campaignExpirationDate != null) {
            if (campaignExpirationDate.compareTo(new Date()) > 0) {
                return getConfigSharedPrefs(context).getString(SharedProperties.CURRENT_CAMPAIGN.toString(), "");
            } else {
                campaignExpirationDate = null;
                getConfigSharedPrefs(context).edit().putLong(SharedProperties.CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP.toString(), -1).commit();
                return "";
            }
        } else {
            //If the campaign expiration date is null that means the campaign never expires.
            //We should load the stored expiration time to check this isn't after an app relaunch however and the campaign expired while it was closed.
            long expirationTimeStamp = getConfigSharedPrefs(context).getLong(SharedProperties.CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP.toString(), -1);
            if (expirationTimeStamp == -1) {
                Date expDate = new Date(expirationTimeStamp);
                if (expDate.compareTo(new Date()) > 0) {
                    campaignExpirationDate = new Date(expirationTimeStamp);
                    return currentCampaign(context);
                } else {
                    return "";
                }
            } else if (expirationTimeStamp == 0) {
                return getConfigSharedPrefs(context).getString(SharedProperties.CURRENT_CAMPAIGN.toString(), "");
            } else {
                return "";
            }
        }
    }

    public static void storeCurrentCampaignWithExpirationTimestamp(Context context, String currentCampaign, long expirationTimestamp) {
        SharedPreferences sharedPreferences = getConfigSharedPrefs(context);
        sharedPreferences.edit().putString(SharedProperties.CURRENT_CAMPAIGN.toString(), currentCampaign).commit();

        if (expirationTimestamp > 0) {
            campaignExpirationDate = new Date(expirationTimestamp);
            sharedPreferences.edit().putLong(SharedProperties.CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP.toString(), campaignExpirationDate.getTime()).commit();
        } else {
            campaignExpirationDate = null;
            sharedPreferences.edit().putLong(SharedProperties.CURRENT_CAMPAIGN_EXPIRATION_TIMESTAMP.toString(), 0).commit();
        }
    }

    public static String lastCampaign(Context context) {
        return getConfigSharedPrefs(context).getString(SharedProperties.CURRENT_CAMPAIGN.toString(), null);
    }

    public static Date currentCampaignExpirationDate() {
        return campaignExpirationDate;
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

        if (EngageConfig.currentAddressCache() == null) {
            expired = true;
        }

        return expired;
    }

    public static String buildLocationAddress() {

        //Sets the location name and address.
        if (EngageConfig.currentAddressCache() != null) {
            Address address = EngageConfig.currentAddressCache();
            StringBuilder builder = new StringBuilder();
            builder.append(address.getLocality() != null ? address.getLocality() : "");
            builder.append(", ");
            builder.append(address.getAdminArea() != null ? address.getAdminArea() : "");
            builder.append(" ");
            builder.append(address.getPostalCode() != null ? address.getPostalCode() : "");
            builder.append(" ");
            builder.append(address.getCountryName() != null ? address.getCountryName() : "");
            builder.append(" ");
            builder.append(address.getCountryCode() != null ? "(" + address.getCountryCode() + ")" : "");

            return builder.toString();
        } else {
            return "";
        }
    }

    public static boolean appInstalled(Context context) {
        String appInstalled = getConfigSharedPrefs(context).getString(SharedProperties.APP_INSTALLED.toString(), "NO");
        return "YES".equals(appInstalled);
    }

    public static void storeAppInstalled(Context context, String appInstalled) {
        getConfigSharedPrefs(context).edit().putString(SharedProperties.APP_INSTALLED.toString(), appInstalled).commit();
    }

    /**
     * @param context
     * @return session started timestamp
     */
    public static long session(Context context) {
        return getConfigSharedPrefs(context).getLong(SharedProperties.SESSION.toString(), -1);
    }

    /**
     * @param context
     * @param session session started timestamp
     */
    public static void storeSession(Context context, long session) {
        getConfigSharedPrefs(context).edit().putLong(SharedProperties.SESSION.toString(), session).commit();
    }

    public static void clearSession(Context context) {
        getConfigSharedPrefs(context).edit().putLong(SharedProperties.SESSION.toString(), -1).commit();
    }

    private static SharedPreferences getConfigSharedPrefs(Context context) {
        return context.getSharedPreferences(SharedProperties.ENGAGE_CONFIG_PREF_ID.toString(), Context.MODE_PRIVATE);
    }
}