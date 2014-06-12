package com.silverpop.engage.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by jeremydyer on 5/19/14.
 */
public abstract class EngageClient
        extends BroadcastReceiver {

    private static final String TAG = EngageClient.class.getName();

    protected static Context mAppContext;
    protected String clientId, clientSecret, refreshToken, host;
    protected static RequestQueue requestQueue;
    private static RequestQueue authenticationQueue;

    //Authentication
    private final String oauthEndpoint = "/oauth/token";
    private Credential credential = null;

    public EngageClient(Context context, String clientId, String secret, String refreshToken, String host) {
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
    protected void authenticateClient(final Response.Listener<String> authSuccess, final Response.ErrorListener authFailure) {
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
                protected Map<String,String> getParams() throws AuthFailureError {
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

    protected boolean isAuthenticated() {
        if (credential.getOauthToken() != null && !isAuthTokenExpired()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAuthTokenExpired() {
        Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (credential.getOauthTokenExpirationTimeStamp() <= current.getTime().getTime()) {
            credential.setOauthToken(null);
            credential.setOauthTokenExpirationTimeStamp(-1);
            return true;
        } else {
            return false;
        }
    }

    protected String getOauthToken() {
        if (isAuthenticated()) {
            return credential.getOauthToken();
        } else {
            return null;
        }
    }

    /**
     * Determines if the network is currently active or not.
     *
     * @param context
     *      Android application context.
     *
     * @return
     *      True if the network is active and false otherwise.
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

    protected String getClientId() {
        return clientId;
    }

    protected void setClientId(String clientId) {
        this.clientId = clientId;
    }

    protected String getClientSecret() {
        return clientSecret;
    }

    protected void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    protected String getRefreshToken() {
        return refreshToken;
    }

    protected void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    protected String getHost() {
        return host;
    }

    protected void setHost(String host) {
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
}
