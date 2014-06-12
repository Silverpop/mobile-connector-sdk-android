package com.silverpop.engage.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
public class XMLAPIClient
    extends EngageClient {

    private static final String TAG = XMLAPIClient.class.getName();

    private static XMLAPIClient xmlapiClient = null;
    private final ArrayList<RequestCacheWrapper> apiCache = new ArrayList<RequestCacheWrapper>();

    private XMLAPIClient(Context context, String clientId, String secret, String refreshToken, String hostUrl) {
        super(context, clientId, secret, refreshToken, hostUrl);
    }

    public static XMLAPIClient init(Context context, String clientId, String secret, String refreshToken, String host) {
        if (xmlapiClient == null) {
            xmlapiClient = new XMLAPIClient(context, clientId, secret, refreshToken, host);
            xmlapiClient.authenticateClient(new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    ArrayList<RequestCacheWrapper> postedAPI = new ArrayList<RequestCacheWrapper>();
                    for (RequestCacheWrapper wrapper : xmlapiClient.apiCache) {
                        xmlapiClient.postResource(wrapper.getXmlapi(),
                                wrapper.getSuccessListener(), wrapper.getErrorListener());
                        postedAPI.add(wrapper);
                    }
                    xmlapiClient.apiCache.removeAll(postedAPI);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.w(TAG, "XMLAPIClient authentication failed");
                }
            });
        }
        return xmlapiClient;
    }


    public static XMLAPIClient get(Context context) {
        if (xmlapiClient == null) {
            Log.e(TAG, XMLAPIClient.class.getName() + " must be initialized before it can be retrieved");
            throw new RuntimeException(XMLAPIClient.class.getName() + " must be initialized before it can be retrieved");
        }
        return xmlapiClient;
    }

    private String getXMLAPIURL() {
        try {
            if (EngageConfigManager.get(mAppContext).secureConnection()) {
                return new URI("https", getHost(), "/XMLAPI", null).toString();
            } else {
                return new URI("http", getHost(), "/XMLAPI", null).toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public void postResource(final XMLAPI api, Response.Listener<String> successListener, Response.ErrorListener errorListener) {
        if (isAuthenticated()) {
            //Create the Volley Request object.
            StringRequest req = new StringRequest(Request.Method.POST,
                    getXMLAPIURL(), successListener, errorListener)
            {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("xml", api.envelope());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + getOauthToken());
                    return params;
                }
            };

            request(req);
        } else {
            apiCache.add(new RequestCacheWrapper(api, successListener, errorListener));
        }
    }
}
