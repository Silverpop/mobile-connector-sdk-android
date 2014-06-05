package com.silverpop.engage.demo.engagetest;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.silverpop.engage.UBFManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.deeplinking.EngageDeepLinkManager;
import com.silverpop.engage.domain.UBF;
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

    private final String APP_INSTALLED = "APP_INSTALLED";
    private final String SESSION = "SESSION";

    private UBFManager globalUBFManager = null;
    private Date sessionExpires = null;
    private Date sessionBegan = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "EngageApplication onCreate()");

        final UBFManager ubfManager = getUbfManager();

        EngageDeepLinkManager.registerHandler(EngageDeepLinkManager.DEFAULT_HANDLER_NAME, new Handler() {
            @Override
            public Map<String, String> execute(Map<String, String> paramsMap) {
                ubfManager.handleExternalURLOpenedEvents(getApplicationContext(), paramsMap);
                return paramsMap;
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().
                getSharedPreferences(EngageConfig.ENGAGE_CONFIG_PREF_ID, Context.MODE_PRIVATE);

        //Check if this is the first time the app has been ran or not
        if (sharedPreferences.getString(APP_INSTALLED, null) == null) {
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
                getUbfManager().postEvent(UBF.sessionStarted(getApplicationContext(),
                        null, EngageConfig.currentCampaign(getApplicationContext())));
                sharedPreferences.edit().putLong(SESSION, sessionBegan.getTime()).commit();
            } else {
                sessionBegan = new Date(sessionStartedTimestamp);
            }

            EngageConfigManager cm = EngageConfigManager.get(getApplicationContext());
            EngageExpirationParser parser = new EngageExpirationParser(cm.sessionLifecycleExpiration(), sessionBegan);
            sessionExpires = parser.expirationDate();

            if (booleanIsSessionExpired()) {
                getUbfManager().postEvent(UBF.sessionEnded(getApplicationContext(), null));
            }

        } else {
            //Compare the current time to the session began time.
            if (booleanIsSessionExpired()) {
                getUbfManager().postEvent(UBF.sessionEnded(getApplicationContext(), null));
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
        Log.d(TAG, "EngageApplication onTerminate()");
        handleSessionEvents();
    }

    public UBFManager getUbfManager() {
        if (globalUBFManager == null) {
            Resources r = getApplicationContext().getResources();
            globalUBFManager = UBFManager.get(getApplicationContext(),
                    r.getString(R.string.clientID),
                    r.getString(R.string.clientSecret),
                    r.getString(R.string.refreshToken),
                    r.getString(R.string.host));
        }
        return globalUBFManager;
    }
}

