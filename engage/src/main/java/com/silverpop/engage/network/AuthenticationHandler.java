package com.silverpop.engage.network;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public interface AuthenticationHandler {

    void onSuccess(String response);

    void onFailure(Exception exception);

}
