package com.silverpop.engage;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.silverpop.engage.augmentation.UBFAugmentationService;
import com.silverpop.engage.augmentation.UBFAugmentationServiceImpl;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.network.UBFClient;
import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.store.EngageLocalEventStore;
import com.silverpop.engage.util.EngageExpirationParser;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBFManager {

    private static final String TAG = UBFManager.class.getName();

    private Context context;
    private UBFClient ubfClient;
    private EngageLocalEventStore localEventStore;
    private UBFAugmentationService ubfAugmentationService;
    private EngageConfigManager engageConfigManager;
    private long augmentationTimeoutSeconds;

    private static UBFManager ubfManager = null;

    private UBFManager(Context context, String clientId, String clientSecret, String refreshToken, String host) {
        setContext(context);
        ubfClient = UBFClient.init(context, clientId, clientSecret, refreshToken, host);
        ubfAugmentationService = UBFAugmentationServiceImpl.get(context);
        engageConfigManager = EngageConfigManager.get(context);

        EngageExpirationParser exp = new EngageExpirationParser(engageConfigManager.augmentationTimeout(), null);
        augmentationTimeoutSeconds = exp.secondsParsedFromExpiration();

        localEventStore = EngageLocalEventStore.get(context);
        try {
            if (!localEventStore.isConnected()) {
                localEventStore.open();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Fire off a thread to look for localevents that have not yet been posted and post those events.
        new RePostFailedEventsTask().execute();
    }

    /**
     * Create the initial instance of the UBFManager.
     *
     * @param context
     * @param clientId
     * @param clientSecret
     * @param refreshToken
     * @param host
     * @return
     */
    public static UBFManager initialize(Context context, String clientId, String clientSecret, String refreshToken, String host) {
        if (ubfManager == null) {
            ubfManager = new UBFManager(context, clientId, clientSecret, refreshToken, host);
        }
        return ubfManager;
    }

    public static UBFManager get() {
        if (ubfManager == null) {
            Log.e(TAG, "EngageSDK - You have not yet initialized your UBFManager instance! Null UBFManager will be returned!");
            return null;
        } else {
            return ubfManager;
        }
    }

    public void postEventCache() {
        postEventCache(null, null);
    }

    public void postEventCache(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        ubfClient.postUBFEngageEvents(successListener, errorListener);
    }

    public long postEvent(UBF event) {
        return postEvent(event, null, null);
    }

    //[Lindsay Thurmond:12/29/14] TODO: success failure listeners not hooked up
    public long postEvent(UBF event, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        //Save the UBF event in the SQLite DB.
        EngageEvent engageEvent = localEventStore.saveUBFEvent(event.toEngageEvent());

        //Augments the event.
        if (ubfAugmentationService.augmentorsCount() > 0) {
            ubfAugmentationService.augmentUBFEvent(event, engageEvent, augmentationTimeoutSeconds);
        } else {
            postEventCache();
        }

        return engageEvent.getId();
    }


    /**
     * Gets the current status for an event in the system.
     *
     * @param eventId
     * @return
     */
    public long statusForEventById(long eventId) {
        EngageEvent event = localEventStore.findEventByIdentifier(eventId);
        if (event != null) {
            return event.getEventStatus();
        } else {
            return -1l;
        }
    }

    /**
     * Invoked when a remote notifcation is received.
     *
     * @param intent
     *      Intent
     *
     * @return
     */
    public long handleRemoteNotification(Intent intent) {
        UBF pushNotification = null;
        if (intent != null && intent.getExtras() != null) {
            Map<String, Object> params = intentExtraValueToMap(intent);
            params = monitorParamsForImportantSystemEvents(params);
            pushNotification = UBF.receivedNotification(context, params);
        } else {
            pushNotification = UBF.receivedNotification(context, null);
        }
        return postEvent(pushNotification);
    }


    public long handleNotificationReceivedEvents(Context context, Notification notification, Map<String, Object> params) {
        return handleNotificationReceivedEvents(context, notification, params, null, null);
    }

    public long handleNotificationReceivedEvents(Context context,
                                                 Notification notification,
                                                 Map<String, Object> params,
                                                 Response.Listener<JSONObject> successListener,
                                                 Response.ErrorListener errorListener) {
        UBF notificationReceived = UBF.receivedNotification(context, notification, params);
        return postEvent(notificationReceived, successListener, errorListener);
    }

    public long handleNotificationOpenedEvents(Context context, Notification notification, Map<String, Object> params) {
        return handleNotificationReceivedEvents(context, notification, params, null, null);
    }

    public long handleNotificationOpenedEvents(Context context, Notification notification,
                                               Map<String, Object> params,
                                               Response.Listener<JSONObject> successListener,
                                               Response.ErrorListener errorListener) {
        UBF notificationReceived = UBF.openedNotification(context, notification, params);
        return postEvent(notificationReceived, successListener, errorListener);
    }

    public long handleExternalURLOpenedEvents(Context context, Map<String, String> params) {
        Map<String, Object> refactoredParams = new HashMap<String, Object>();
        refactoredParams.putAll(params);
        refactoredParams = monitorParamsForImportantSystemEvents(refactoredParams);
        UBF externalURLOpenedEvent = UBF.deepLinkOpened(context, refactoredParams);
        return postEvent(externalURLOpenedEvent);
    }

    /**
     * Converts an Intent bundle into a more friend Map datastructure.
     *
     * @param intent
     * @return
     */
    private Map<String, Object> intentExtraValueToMap(Intent intent) {
        Map<String, Object> intentParams = new HashMap<String, Object>();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            Iterator<String> itr = bundle.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                try {
                    intentParams.put(key, bundle.getString(key));
                } catch (Exception ex) {}
            }
        }
        return intentParams;
    }

    /**
     * Examines the incoming params for things like new campaigns, campaign expiration timestamps, etc.
     * @param params
     */
    private Map<String, Object> monitorParamsForImportantSystemEvents(Map<String, Object> params) {

        Map<String, Object> refactoredParams = new HashMap<String, Object>();

        EngageConfigManager cm = EngageConfigManager.get(context);
        if (params.containsKey(cm.paramCurrentCampaign())) {
            Object value = params.get(cm.paramCurrentCampaign());

            //Check for a user defined campaign expiration
            if (params.containsKey(cm.paramCampaignExpiresAt())) {
                EngageExpirationParser parser = new EngageExpirationParser((String)params.get(cm.paramCampaignExpiresAt()), null);
                EngageConfig.storeCurrentCampaignWithExpirationTimestamp(context,
                        (String)value, parser.expirationTimeStamp());
            } else if (params.containsKey(cm.paramCampaignValidFor())) {
                EngageExpirationParser parser = new EngageExpirationParser((String)params.get(cm.paramCampaignValidFor()), null);
                EngageConfig.storeCurrentCampaignWithExpirationTimestamp(context,
                        (String)value, parser.expirationTimeStamp());
            } else {
                EngageConfig.storeCurrentCampaignWithExpirationTimestamp(context,
                        (String)value, -1);
            }

            refactoredParams.put(cm.ubfCurrentCampaignFieldName(), value);
        }

        //Parse the CallToAction Parameter
        if (params.containsKey(cm.paramCallToAction())) {
            refactoredParams.put(cm.paramCallToAction(), params.get(cm.paramCallToAction()));
        }

        return refactoredParams;
    }

    private class RePostFailedEventsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... strings) {
            ubfClient.postUBFEngageEvents(null, null);
            return null;
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
