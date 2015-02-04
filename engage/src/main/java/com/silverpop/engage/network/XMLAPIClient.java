package com.silverpop.engage.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.XMLAPI;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class XMLAPIClient extends BaseClient {

    private static final String TAG = XMLAPIClient.class.getName();

    private static XMLAPIClient instance = null;
    private final ArrayList<RequestCacheWrapper> apiCache = new ArrayList<RequestCacheWrapper>();

    private XMLAPIClient(Context context) {
        super(context);
    }

    public static synchronized XMLAPIClient init(Context context) {
        if (instance == null) {
            instance = new XMLAPIClient(context);
        }
        return instance;
    }

    public void postCachedEvents() {
        ArrayList<RequestCacheWrapper> postedAPI = new ArrayList<RequestCacheWrapper>();
        for (RequestCacheWrapper wrapper : instance.apiCache) {
            instance.postResource(wrapper.getXmlapi(),
                    wrapper.getSuccessListener(), wrapper.getErrorListener());
            postedAPI.add(wrapper);
        }
        instance.apiCache.removeAll(postedAPI);
    }

    public static XMLAPIClient get() {
        if (instance == null) {
            final String error = XMLAPIClient.class.getName() + " must be initialized before it can be retrieved";
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
        return instance;
    }

    private String getXMLAPIURL() {
        String url;
        try {
            final String host = connectionManager().getHost();
            final boolean secureConnection = EngageConfigManager.get(getContext()).secureConnection();
            //[Lindsay Thurmond:1/6/15] TODO: move resource to external config
            url =  new URI(secureConnection ? "https" : "http", host, "/XMLAPI", null).toString();

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            url = "";
        }
        return url;
    }

    public void postResource(final XMLAPI api, Response.Listener<String> successListener, Response.ErrorListener errorListener) {
        if (connectionManager().isAuthenticated()) {
            //Create the Volley Request object.
            StringRequest req = new StringRequest(Request.Method.POST, getXMLAPIURL(), successListener, errorListener) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("xml", api.envelope());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return connectionManager().buildHeaderParams();
                }
            };

            connectionManager().request(req);
        } else {
            apiCache.add(new RequestCacheWrapper(api, successListener, errorListener));
        }
    }

}
