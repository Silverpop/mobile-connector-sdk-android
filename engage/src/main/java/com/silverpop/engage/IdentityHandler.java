package com.silverpop.engage;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public interface IdentityHandler {

    //[Lindsay Thurmond:1/9/15] TODO: think about making more generic - with event name as param

    public void onSuccess(String recipientId, String mobileUserId);

    public void onFailure(Exception e);

}
