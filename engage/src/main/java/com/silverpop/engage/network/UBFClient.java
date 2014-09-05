package com.silverpop.engage.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.store.EngageLocalEventStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBFClient
    extends EngageClient {

    private static final String TAG = EngageClient.class.getName();

    private static UBFClient ubfClient;
    private EngageLocalEventStore engageLocalEventStore = null;
    private int maxRetries = 3;

    //List of inprogress events kept in memory. Helps with concurrency issues and preventing events from being sent twice.
    private ArrayList<Long> inProgressEventIds = null;

    private UBFClient(Context context, String clientId, String secret, String refreshToken, String host) {
        super(context, clientId, secret, refreshToken, host);

        maxRetries = EngageConfigManager.get(context).maxNumRetries();
        engageLocalEventStore = EngageLocalEventStore.get(context);
        inProgressEventIds = new ArrayList<Long>();

        try {
            if (!engageLocalEventStore.isConnected()) {
                engageLocalEventStore.open();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UBFClient init(Context context, String clientId, String secret, String refreshToken, String host) {
        if (ubfClient == null) {
            ubfClient = new UBFClient(context, clientId, secret, refreshToken, host);
            ubfClient.authenticateClient(new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    ubfClient.postUBFEngageEvents(null, null);
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.w(TAG, "UBFClient authentication failed");
                }
            });
        }
        return ubfClient;
    }

    public static UBFClient get(Context context) {
        if (ubfClient == null) {
            Log.e(TAG, UBFClient.class.getName() + " must be initialized before it can be retrieved");
            throw new RuntimeException(UBFClient.class.getName() + " must be initialized before it can be retrieved");
        }
        return ubfClient;
    }

    private String getUBFURL() {
        try {
            if (EngageConfigManager.get(mAppContext).secureConnection()) {
                return new URI("https", getHost(), "/rest/events/submission", null).toString();
            } else {
                return new URI("http", getHost(), "/rest/events/submission", null).toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public void postUBFEngageEvents(Response.Listener<JSONObject> success,
                                    Response.ErrorListener error) {

        if (isAuthenticated()) {
            //Find the events that are ready to be sent to Engage.
            final EngageEvent[] unpostedEvents = engageLocalEventStore.findUnpostedEvents();

            if (unpostedEvents != null && unpostedEvents.length > 0) {

                //Create the JSONObject wrapped around the "events" tag engage expects.
                JSONObject events = new JSONObject();
                final ArrayList<EngageEvent> stackCopyOfIds = new ArrayList<EngageEvent>();

                try {
                    JSONArray eventsArray = new JSONArray();

                    //Adds the events to the JSON array and makes sure that the events are not already in progress.
                    for (EngageEvent ee : unpostedEvents) {
                        if (!inProgressEventIds.contains(ee.getId())) {
                            eventsArray.put(new JSONObject(ee.getEventJson()));
                            inProgressEventIds.add(new Long(ee.getId()));
                            stackCopyOfIds.add(ee);
                        }
                    }

                    //Only post if the events array contains elements.
                    if (eventsArray.length() > 0) {
                        events.put("events", eventsArray);

                        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                                getUBFURL(), events,
                                new Response.Listener<JSONObject>() {
                                    public void onResponse(JSONObject response) {
                                        //TODO: Add extra logic to make sure that the message was successful based on response payload
                                        //Update all of the EngageEvents to SUCCESSFULLY_POSTED
                                        for (EngageEvent ee : stackCopyOfIds) {
                                            ee.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
                                            engageLocalEventStore.saveUBFEvent(ee);

                                            //Removes the EngageEvent from the list of "inProgress" events.
                                            if (inProgressEventIds.contains(ee.getId())) {
                                                inProgressEventIds.remove(ee.getId());
                                            }
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Update the failure count and then fail the status if the maximum has been reached.
                                        for (EngageEvent ee : stackCopyOfIds) {
                                            ee.setEventFailedPostCount(ee.getEventFailedPostCount() + 1);
                                            if (ee.getEventFailedPostCount() >= maxRetries) {
                                                ee.setEventStatus(EngageEvent.FAILED_POST);
                                            }
                                            inProgressEventIds.remove(ee.getId());
                                            engageLocalEventStore.saveUBFEvent(ee);
                                        }
                                    }
                                }
                        )
                        {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                String oauthToken = getOauthToken();
                                if (oauthToken != null) {
                                    params.put("Authorization", "Bearer " + getOauthToken());
                                } else {
                                    Log.w(TAG, "Client authentication has expired. Event post will fail " +
                                            "but will be reattempted after new authentication is complete.");
                                }

                                return params;
                            }
                        };
                        request(jsonRequest);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, "UBFClient is not yet authenticated. " +
                    "Events will be pushed when authentication is complete");
        }
    }
}
