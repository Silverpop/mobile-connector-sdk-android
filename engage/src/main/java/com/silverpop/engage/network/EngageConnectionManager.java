package com.silverpop.engage.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.silverpop.engage.config.EngageConfigManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public class EngageConnectionManager extends BroadcastReceiver {

    private static final String TAG = EngageConnectionManager.class.getName();

    private static RequestQueue requestQueue;
    private static RequestQueue authenticationQueue;

    private static Context mAppContext;

    //Authentication
    //[Lindsay Thurmond:12/30/14] TODO: move endpoint to external config
    private final String oauthEndpoint = "/oauth/token";
    private Credential credential = null;

    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String host;

    private static EngageConnectionManager instance;

    public static EngageConnectionManager get() {
        if (instance == null) {
            Log.e(TAG, EngageConnectionManager.class.getName() + " must be initialized before it can be retrieved");
            throw new RuntimeException(EngageConnectionManager.class.getName() + " must be initialized before it can be retrieved");
        }
        return instance;
    }

    public static synchronized EngageConnectionManager init(Context context, String clientId, String secret,
                                                           String refreshToken, String host, final AuthenticationHandler authHandler) {
        if (instance == null) {
            instance = new EngageConnectionManager(context, clientId, secret, refreshToken, host);
            instance.authenticateClient(new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (authHandler != null) {
                        authHandler.onSuccess(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.w(TAG, "MobileConnectorManager authentication failed");
                    if (authHandler != null) {
                        authHandler.onFailure(volleyError);
                    }
                }
            });

        }
        return instance;
    }

    private EngageConnectionManager(Context context, String clientId, String secret, String refreshToken, String host) {
        if (mAppContext == null) {
            mAppContext = context;
        }

        setClientId(clientId);
        setClientSecret(secret);
        setRefreshToken(refreshToken);
        setHost(host);

        if (credential == null) {
            credential = new Credential(clientId, secret, refreshToken, getHost());
        }

        //Creates the Authenticate queue and begins the auth process.
        if (authenticationQueue == null) {
            authenticationQueue = Volley.newRequestQueue(context);
            authenticationQueue.start();
        }

        //Create the new Volley Request Queue BUT pause the execution until the authentication is complete.
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            requestQueue.stop();
        }

        if (isAuthenticated()) {
            requestQueue.start();
        }

        //Watches for the network connectivity to change.
        IntentFilter networkIntentFilter = new IntentFilter();
        networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, networkIntentFilter);
    }

    /**
     * Handles authenticating the client.
     */
    private void authenticateClient(final Response.Listener<String> authSuccess, final Response.ErrorListener authFailure) {
        StringRequest authRequest = new StringRequest(Request.Method.POST,
                getOauthEndpoint(),
                new Response.Listener<String>() {
                    public void onResponse(String response) {
                        try {
                            JSONObject authResponse = new JSONObject(response);
                            credential.setOauthToken(authResponse.getString("access_token"));
                            int expiresInSeconds = authResponse.getInt("expires_in");

                            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            now.add(Calendar.SECOND, expiresInSeconds);
                            credential.setOauthTokenExpirationTimeStamp(now.getTimeInMillis());

                            //Start the request operation queue if the network is active.
                            if (isNetworkActive(mAppContext)) {
                                requestQueue.start();
                            } else {
                                requestQueue.stop();
                            }

                            if (authSuccess != null) {
                                authSuccess.onResponse(response);
                            }

                            credential.setCurrentlyAttemptingAuth(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error Authenticating client! " + error.getMessage());

                        if (authFailure != null) {
                            authFailure.onErrorResponse(error);
                        }

                        credential.setCurrentlyAttemptingAuth(false);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("client_id", getClientId());
                params.put("client_secret", getClientSecret());
                params.put("refresh_token", getRefreshToken());
                params.put("grant_type", "refresh_token");
                return params;
            }
        };
        authenticationQueue.add(authRequest);
    }

    /**
     * Determines if the network is currently active or not.
     *
     * @param context Android application context.
     * @return True if the network is active and false otherwise.
     */
    private boolean isNetworkActive(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        //Check for network activity.
        if (activeNetInfo == null && mobNetInfo == null) {
            //Everything is offline.
            return false;
        } else if (activeNetInfo != null && mobNetInfo == null) {
            if (activeNetInfo.isAvailable() && activeNetInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else if (activeNetInfo == null && mobNetInfo != null) {
            if (mobNetInfo.isAvailable() && mobNetInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            //Both net info are ok.
            if (activeNetInfo.isAvailable() && activeNetInfo.isConnected()) {
                return true;
            } else if (mobNetInfo.isAvailable() && mobNetInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Above and beyond anything the client must be authenticated before request queue can be turned on.
        if (isAuthenticated()) {
            if (isNetworkActive(context)) {
                requestQueue.start();
            } else {
                requestQueue.stop();
            }
        }
    }

    public void request(Request<?> request) {
        requestQueue.add(request);
    }

    public boolean isAuthenticated() {
        boolean isAuthenticated = credential.getOauthToken() != null && !isAuthTokenExpired();
        return isAuthenticated;
    }

    private boolean isAuthTokenExpired() {
        Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        boolean tokenExpired = credential.getOauthTokenExpirationTimeStamp() <= current.getTime().getTime();
        if (tokenExpired) {
            credential.setOauthToken(null);
            credential.setOauthTokenExpirationTimeStamp(-1);
        }
        return tokenExpired;
    }

    public String getOauthToken() {
        if (isAuthenticated()) {
            return credential.getOauthToken();
        } else {
            return null;
        }
    }


    private String getOauthEndpoint() {
        try {
            return getOauthUri().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private URI getOauthUri() throws Exception {
        if (EngageConfigManager.get(mAppContext).secureConnection()) {
            return new URI("https", host, oauthEndpoint, null);
        } else {
            return new URI("http", host, oauthEndpoint, null);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (host != null) {
            this.host = host.replace("http://", "");
            this.host = this.host.replace("https://", "");
            if (this.host.endsWith("/")) {
                this.host = this.host.substring(0, this.host.length() - 1);
            }
        } else {
            this.host = host;
        }
    }

    public Map<String, String> buildHeaderParams() {
        Map<String, String> params = new HashMap<String, String>();
        String oauthToken = getOauthToken();
        if (!TextUtils.isEmpty(oauthToken)) {
            params.put("Authorization", "Bearer " + getOauthToken());
        } else {
            Log.w(TAG, "Client authentication has expired. Event post will fail " +
                    "but will be reattempted after new authentication is complete.");
        }

        return params;
    }

}
