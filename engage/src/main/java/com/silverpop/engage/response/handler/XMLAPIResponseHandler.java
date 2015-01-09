package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public interface XMLAPIResponseHandler {

    public void onSuccess(EngageResponseXML response);

    public void onFailure(Exception exception);

}
