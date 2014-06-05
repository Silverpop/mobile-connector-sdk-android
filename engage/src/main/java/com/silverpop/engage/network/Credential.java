package com.silverpop.engage.network;

import java.io.Serializable;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class Credential
    implements Serializable {

    private String clientId, clientSecret, refreshToken, host;
    private boolean currentlyAttemptingAuth = false;
    private String oauthToken = null;
    private long oauthTokenExpirationTimeStamp = -1;

    public Credential(String clientId, String clientSecret, String refreshToken, String host) {
        setClientId(clientId);
        setClientSecret(clientSecret);
        setRefreshToken(refreshToken);
        setHost(host);
        setOauthToken(oauthToken);
        setOauthTokenExpirationTimeStamp(oauthTokenExpirationTimeStamp);
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
        this.host = host;
    }

    public boolean isCurrentlyAttemptingAuth() {
        return currentlyAttemptingAuth;
    }

    public void setCurrentlyAttemptingAuth(boolean currentlyAttemptingAuth) {
        this.currentlyAttemptingAuth = currentlyAttemptingAuth;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public long getOauthTokenExpirationTimeStamp() {
        return oauthTokenExpirationTimeStamp;
    }

    public void setOauthTokenExpirationTimeStamp(long oauthTokenExpirationTimeStamp) {
        this.oauthTokenExpirationTimeStamp = oauthTokenExpirationTimeStamp;
    }
}
