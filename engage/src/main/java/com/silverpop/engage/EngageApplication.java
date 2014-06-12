package com.silverpop.engage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.deeplinking.EngageDeepLinkManager;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.location.EngageLocationManager;
import com.silverpop.engage.util.EngageExpirationParser;

import org.mobiledeeplinking.android.Handler;

import java.util.Date;
import java.util.Map;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class EngageApplication
        extends Application {

    private static final String TAG = EngageApplication.class.getName();

    public static final String CLIENT_ID_META = "ENGAGE_CLIENT_ID";
    public static final String CLIENT_SECRET_META = "ENGAGE_CLIENT_SECRET_META";
    public static final String REFRESH_TOKEN_META = "ENGAGE_REFRESH_TOKEN";
    public static final String HOST = "ENGAGE_HOST";

    private static String clientId = null;
    private static String clientSecret = null;
    private static String refreshToken = null;
    private static String host = null;

    private final String APP_INSTALLED = "APP_INSTALLED";
    private final String SESSION = "SESSION";

    private Date sessionExpires = null;
    private Date sessionBegan = null;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(
                    getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            clientId = bundle.getString(CLIENT_ID_META);
            clientSecret = bundle.getString(CLIENT_SECRET_META);
            refreshToken = bundle.getString(REFRESH_TOKEN_META);
            host = bundle.getString(HOST);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to load EngageSDK credential specific meta data. " +
                    "Did you provide your engage credentials in your manifest?");
        }

        EngageConfigManager cm = EngageConfigManager.get(getApplicationContext());

        if (cm.locationServicesEnabled()) {
            EngageLocationManager locationManager = EngageLocationManager.get(getApplicationContext());
            locationManager.startLocationUpdates();
            Log.d(TAG, "Starting location services");
        } else {
            Log.d(TAG, "Location services are disabled");
        }

        //Initializes the UBFManager and XMLAPIManager instances.
        UBFManager.initialize(getApplicationContext(), clientId, clientSecret, refreshToken, host);
        XMLAPIManager.initialize(getApplicationContext(), clientId, clientSecret, refreshToken, host);


        final UBFManager ubfManager = UBFManager.get();

        //Registers a default deep linking handler for parsing URL parameters
        EngageDeepLinkManager.registerHandler(EngageDeepLinkManager.DEFAULT_HANDLER_NAME, new Handler() {
            @Override
            public Map<String, String> execute(Map<String, String> paramsMap) {
                UBFManager.get().handleExternalURLOpenedEvents(getApplicationContext(), paramsMap);
                return paramsMap;
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().
                getSharedPreferences(EngageConfig.ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);

        //Check if this is the first time the app has been ran or not
        if (sharedPreferences.getString(APP_INSTALLED, null) == null) {
            Log.d(TAG, "EngageSDK - Application has been installed/ran for the first time");
            sharedPreferences.edit().putString(APP_INSTALLED, "YES").commit();
            UBF appInstalled = UBF.installed(getApplicationContext(), null);
            ubfManager.postEvent(appInstalled);
        }

        //Examine the session and determine if events should be posted.
        handleSessionEvents();
    }

    private void handleSessionEvents() {
        if (sessionBegan == null) {

            //Checks to see if a previous session has been persisted.
            SharedPreferences sharedPreferences = getApplicationContext().
                    getSharedPreferences(EngageConfig.ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);
            long sessionStartedTimestamp = sharedPreferences.getLong(SESSION, -1);
            if (sessionStartedTimestamp == -1) {
                //Start a session.
                sessionBegan = new Date();
                UBFManager.get().postEvent(UBF.sessionStarted(getApplicationContext(),
                        null, EngageConfig.currentCampaign(getApplicationContext())));
                sharedPreferences.edit().putLong(SESSION, sessionBegan.getTime()).commit();
            } else {
                sessionBegan = new Date(sessionStartedTimestamp);
            }

            EngageConfigManager cm = EngageConfigManager.get(getApplicationContext());
            EngageExpirationParser parser = new EngageExpirationParser(cm.sessionLifecycleExpiration(), sessionBegan);
            sessionExpires = parser.expirationDate();

            if (booleanIsSessionExpired()) {
                UBFManager.get().postEvent(UBF.sessionEnded(getApplicationContext(), null));
            }

        } else {
            //Compare the current time to the session began time.
            if (booleanIsSessionExpired()) {
                UBFManager.get().postEvent(UBF.sessionEnded(getApplicationContext(), null));
            }
        }
    }

    private boolean booleanIsSessionExpired() {
        if (sessionExpires.compareTo(new Date()) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        handleSessionEvents();
    }
}

