package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/8/15.
 */
public abstract class AddRecipientResponseHandler extends DefaultXMLAPIResponseHandler {

    @Override
    public void onSuccessfulResponse(EngageResponseXML response) {
        AddRecipientResponse addRecipientResponse = new AddRecipientResponse(response);
        onAddRecipientSuccess(addRecipientResponse);
    }

    public abstract void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse);

}
