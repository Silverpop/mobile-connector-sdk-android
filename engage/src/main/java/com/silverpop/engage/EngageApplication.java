package com.silverpop.engage;

import android.app.Application;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.deeplinking.EngageDeepLinkManager;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.location.manager.EngageLocationManager;
import com.silverpop.engage.location.manager.plugin.EngageLocationManagerDefault;
import com.silverpop.engage.util.EngageExpirationParser;

import org.mobiledeeplinking.android.Handler;

import java.util.Date;
import java.util.HashMap;
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

    private static final String APP_INSTALLED = "APP_INSTALLED";
    private static final String SESSION = "SESSION";

    private Date sessionExpires = null;
    private Date sessionBegan = null;

    private EngageLocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Resources r = getResources();

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
            String pluggableLocationClassname = EngageConfigManager.get(getApplicationContext()).pluggableLocationManagerClassName();
            if (pluggableLocationClassname != null) {
                try {
                    Class<?> clazz = Class.forName(pluggableLocationClassname);
                    locationManager = (EngageLocationManager) clazz.newInstance();
                    locationManager.setEngageApplication(this);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            if (locationManager == null) {
                Log.w(TAG, "Unable to create PluggableLocationManager instance. Defaulting to : '"
                        + EngageLocationManagerDefault.class.getName() + "'");
                //Create the default EngageLocationManager instance.
                locationManager = new EngageLocationManagerDefault();
                locationManager.setEngageApplication(this);
            }

            locationManager.startLocationUpdates();
            Log.d(TAG, "Starting location services");

        } else {
            Log.d(TAG, "Location services are disabled");
        }

        //Initializes the UBFManager and XMLAPIManager instances.
        XMLAPIManager.initialize(getApplicationContext(), clientId, clientSecret, refreshToken, host);
        UBFManager.initialize(getApplicationContext(), clientId, clientSecret, refreshToken, host);


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
        final boolean firstInstall = sharedPreferences.getString(APP_INSTALLED, null) == null;
        if (firstInstall) {
            Log.d(TAG, "EngageSDK - Application has been installed/ran for the first time");
            sharedPreferences.edit().putString(APP_INSTALLED, "YES").commit();

            waitForPrimaryUserIdThenCreateInstalledEvent();

            //Create the Last known user location database columns
            Map<String, Object> bodyElements = new HashMap<String, Object>();
            bodyElements.put("LIST_ID", EngageConfigManager.get(getApplicationContext()).engageListId());
            bodyElements.put("COLUMN_NAME", EngageConfigManager.get(getApplicationContext()).lastKnownLocationColumn());
            bodyElements.put("COLUMN_TYPE", 0);
            bodyElements.put("DEFAULT", "");
            XMLAPI createLastKnownLocationColumns = new XMLAPI("AddListColumn", bodyElements);
            XMLAPIManager.get().postXMLAPI(createLastKnownLocationColumns, null, null);

            bodyElements = new HashMap<String, Object>();
            bodyElements.put("LIST_ID", EngageConfigManager.get(getApplicationContext()).engageListId());
            bodyElements.put("COLUMN_NAME", EngageConfigManager.get(getApplicationContext()).lastKnownLocationTimestampColumn());
            bodyElements.put("COLUMN_TYPE", 3);
            bodyElements.put("DEFAULT", "");
            createLastKnownLocationColumns = new XMLAPI("AddListColumn", bodyElements);
            XMLAPIManager.get().postXMLAPI(createLastKnownLocationColumns, null, null);
        }

        //Examine the session and determine if events should be posted.
        handleSessionApplicationLaunch(firstInstall);
    }

    private void handleSessionApplicationLaunch(final boolean firstInstall) {
        SharedPreferences sharedPreferences = getApplicationContext().
                getSharedPreferences(EngageConfig.ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);
        if (sessionBegan == null) {
            //Checks to see if a previous session has been persisted.
            long sessionStartedTimestamp = sharedPreferences.getLong(SESSION, -1);
            if (sessionStartedTimestamp == -1) {
                //Start a session.
                sessionBegan = new Date();
                // app just installed so wait for login
                if (firstInstall) {
                    waitForPrimaryUserIdThenCreateSessionStartedEvent();
                } else {
                    createAndPostSessionStartedEvent();
                }
                sharedPreferences.edit().putLong(SESSION, sessionBegan.getTime()).commit();
            } else {
                sessionBegan = new Date(sessionStartedTimestamp);
            }

            EngageConfigManager cm = EngageConfigManager.get(getApplicationContext());
            EngageExpirationParser parser = new EngageExpirationParser(cm.sessionLifecycleExpiration(), sessionBegan);
            sessionExpires = parser.expirationDate();

            if (isSessionExpired()) {
                createAndPostSessionEndedEvent();
                sharedPreferences.edit().putLong(SESSION, -1).commit();
                createAndPostSessionStartedEvent();
            }

        } else {
            //Compare the current time to the session began time.
            if (isSessionExpired()) {
                createAndPostSessionEndedEvent();
                sharedPreferences.edit().putLong(SESSION, -1).commit();
                createAndPostSessionStartedEvent();
            }
        }
    }

    private void createAndPostSessionEndedEvent() {
        UBFManager.get().postEvent(UBF.sessionEnded(getApplicationContext(), null));
    }

    private void createAndPostSessionStartedEvent() {
        UBFManager.get().postEvent(UBF.sessionStarted(getApplicationContext(),
                null, EngageConfig.currentCampaign(getApplicationContext())));
    }


    private void waitForPrimaryUserIdThenCreateInstalledEvent() {
        Log.i(TAG, "Registering primary user id listener for installed event");
        final String primaryUserId = EngageConfig.primaryUserId(getApplicationContext());
        if (primaryUserId != null && !primaryUserId.isEmpty()) {
            createAndPostInstalledEvent();
        } else {
            this.registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            createAndPostInstalledEvent();

                            // remove listener
                            EngageApplication.this.unregisterReceiver(this);
                            Log.i(TAG, "Removed primary user id listener for installed event");
                        }
                    },
                    new IntentFilter(EngageConfig.PRIMARY_USER_ID_SET_EVENT));
        }
    }

    private void createAndPostInstalledEvent() {
        UBF appInstalled = UBF.installed(getApplicationContext(), null);
        UBFManager.get().postEvent(appInstalled);
    }

    private void waitForPrimaryUserIdThenCreateSessionStartedEvent() {
        Log.i(TAG, "Registering primary user id listener for session started event");

        final String primaryUserId = EngageConfig.primaryUserId(getApplicationContext());
        if (primaryUserId != null && !primaryUserId.isEmpty()) {
            createAndPostSessionStartedEvent();
        } else {
            this.registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            createAndPostSessionStartedEvent();

                            // remove listener
                            EngageApplication.this.unregisterReceiver(this);
                            Log.i(TAG, "Removed primary user id listener for session started event");
                        }
                    },
                    new IntentFilter(EngageConfig.PRIMARY_USER_ID_SET_EVENT));
        }
    }

    private boolean isSessionExpired() {
        if (sessionExpires.compareTo(new Date()) < 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (isSessionExpired()) {
            createAndPostSessionEndedEvent();
        }
    }
}

