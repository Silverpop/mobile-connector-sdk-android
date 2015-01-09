package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.SelectRecipientResponse;

/**
 * Created by Lindsay Thurmond on 1/8/15.
 */
public abstract class SelectRecipientResponseHandler extends DefaultXMLAPIResponseHandler {

    @Override
    public void onSuccessfulResponse(EngageResponseXML response) {
        SelectRecipientResponse updateRecipientResponse = new SelectRecipientResponse(response);
        onSelectRecipientSuccess(updateRecipientResponse);
    }

    public abstract void onSelectRecipientSuccess(SelectRecipientResponse updateRecipientResponse);

}
