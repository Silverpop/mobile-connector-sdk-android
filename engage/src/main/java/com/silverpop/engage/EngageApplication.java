package com.silverpop.engage;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.deeplinking.EngageDeepLinkManager;
import com.silverpop.engage.domain.*;
import com.silverpop.engage.location.manager.EngageLocationManager;
import com.silverpop.engage.location.manager.plugin.EngageLocationManagerDefault;
import com.silverpop.engage.network.AuthenticationHandler;
import com.silverpop.engage.network.EngageConnectionManager;
import com.silverpop.engage.network.UBFClient;
import com.silverpop.engage.network.XMLAPIClient;
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

    private Date sessionExpires = null;
    private Date sessionBegan = null;

    private EngageLocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Resources r = getResources();

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
                    getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            clientId = bundle.getString(CLIENT_ID_META);
            clientSecret = bundle.getString(CLIENT_SECRET_META);
            refreshToken = bundle.getString(REFRESH_TOKEN_META);
            host = bundle.getString(HOST);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to load EngageSDK credential specific meta data. " +
                    "Did you provide your engage credentials in your manifest?");
        }

        configureLocationServicesIfNeeded();

        // init singletons
        EngageConnectionManager.init(getApplicationContext(), clientId, clientSecret, refreshToken, host,
                new AuthenticationHandler() {
                    @Override
                    public void onSuccess(String response) {
                        // post any existing events
                        //[Lindsay Thurmond:1/6/15] TODO: consider add event listeners to handle instead
                        XMLAPIClient.get().postCachedEvents();
                        UBFClient.get().postUBFEngageEvents();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                    }
                });
        XMLAPIManager.init(getApplicationContext());
        UBFManager.init(getApplicationContext());
        MobileIdentityManager.init(getApplicationContext());
        AnonymousMobileIdentityManager.init(getApplicationContext());

        //Registers a default deep linking handler for parsing URL parameters
        EngageDeepLinkManager.registerHandler(EngageDeepLinkManager.DEFAULT_HANDLER_NAME, new Handler() {
            @Override
            public Map<String, String> execute(Map<String, String> paramsMap) {
                UBFManager.get().handleExternalURLOpenedEvents(getApplicationContext(), paramsMap);
                return paramsMap;
            }
        });

        //Check if this is the first time the app has been ran or not
        final boolean firstInstall = !EngageConfig.appInstalled(getApplicationContext());
        if (firstInstall) {

            Log.d(TAG, "EngageSDK - Application has been installed/ran for the first time");
            EngageConfig.storeAppInstalled(getApplicationContext(), "YES");

            waitForPrimaryUserIdThenCreateInstalledEvent();

            configureXmlApiDatabaseTables();
        }

        //Examine the session and determine if events should be posted.
        handleSessionApplicationLaunch(firstInstall);
    }

    private void configureXmlApiDatabaseTables() {
        //Create the Last known user location database columns
        createDatabaseColumn(EngageConfigManager.get(getApplicationContext()).lastKnownLocationColumn(),
                XMLAPIColumnType.TEXT_COLUMN, "");

        createDatabaseColumn(EngageConfigManager.get(getApplicationContext()).lastKnownLocationTimestampColumn(),
                XMLAPIColumnType.DATE_COLUMN, "");
    }

    protected void createDatabaseColumn(String columnName, XMLAPIColumnType columnType, Object defaultValue) {
        createDatabaseColumn(EngageConfigManager.get(getApplicationContext()).engageListId(), columnName, columnType, defaultValue);
    }

    protected void createDatabaseColumn(String listId, String columnName, XMLAPIColumnType columnType, Object defaultValue) {
        XMLAPI createColumnXml = XMLAPI.builder().operation(XMLAPIOperation.ADD_LIST_COLUMN)
                .listId(listId)
                .param(XMLAPIElement.COLUMN_NAME, columnName)
                .param(XMLAPIElement.COLUMN_TYPE, columnType.value())
                .param(XMLAPIElement.DEFAULT, defaultValue)
                .build();
        XMLAPIManager.get().postXMLAPI(createColumnXml, null, null);
    }

    private void configureLocationServicesIfNeeded() {
        EngageConfigManager configManager = EngageConfigManager.get(getApplicationContext());

        if (configManager.locationServicesEnabled()) {
            String pluggableLocationClassname = EngageConfigManager.get(getApplicationContext()).pluggableLocationManagerClassName();
            if (pluggableLocationClassname != null) {
                try {
                    Class<?> clazz = Class.forName(pluggableLocationClassname);
                    locationManager = (EngageLocationManager) clazz.newInstance();
                    locationManager.setEngageApplication(this);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (InstantiationException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, e.getMessage(), e);
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
    }

    private void handleSessionApplicationLaunch(final boolean firstInstall) {

        if (sessionBegan == null) {
            //Checks to see if a previous session has been persisted.
            long sessionStartedTimestamp = EngageConfig.session(getApplicationContext());
            if (sessionStartedTimestamp == -1) {
                //Start a session.
                sessionBegan = new Date();
                // app just installed so wait for login
                if (firstInstall) {
                    waitForPrimaryUserIdThenCreateSessionStartedEvent();
                } else {
                    createAndPostSessionStartedEvent();
                }
                EngageConfig.storeSession(getApplicationContext(), sessionBegan.getTime());

            } else {
                sessionBegan = new Date(sessionStartedTimestamp);
            }

            EngageConfigManager configManager = EngageConfigManager.get(getApplicationContext());
            EngageExpirationParser parser = new EngageExpirationParser(configManager.sessionLifecycleExpiration(), sessionBegan);
            sessionExpires = parser.expirationDate();

            if (isSessionExpired()) {
                createAndPostSessionEndedEvent();
                EngageConfig.clearSession(getApplicationContext());
                createAndPostSessionStartedEvent();
            }

        } else {
            //Compare the current time to the session began time.
            if (isSessionExpired()) {
                createAndPostSessionEndedEvent();
                EngageConfig.clearSession(getApplicationContext());
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
        final String primaryUserId = EngageConfig.mobileUserId(getApplicationContext());
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

        final String primaryUserId = EngageConfig.mobileUserId(getApplicationContext());
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
        boolean sessionExpired = sessionExpires.compareTo(new Date()) < 0;
        return sessionExpired;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (isSessionExpired()) {
            createAndPostSessionEndedEvent();
        }
    }
}

