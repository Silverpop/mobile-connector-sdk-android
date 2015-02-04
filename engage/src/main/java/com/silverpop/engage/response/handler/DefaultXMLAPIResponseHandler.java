package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/8/15.
 */
public abstract class DefaultXMLAPIResponseHandler implements XMLAPIResponseHandler {

    @Override
    public void onSuccess(EngageResponseXML response) {
        if (!response.isSuccess()) {
            // call on failure instead
            onFailure(new XMLAPIResponseFailure(response));
        }
        onSuccessfulResponse(response);
    }

    /**
     * Called only if the request/response didn't error AND the contents of the response are marked as successful.
     *
     * @param response
     */
    public abstract void onSuccessfulResponse(EngageResponseXML response);

}
