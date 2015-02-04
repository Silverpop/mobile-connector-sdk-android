package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.UpdateRecipientResponse;

/**
 * Created by Lindsay Thurmond on 1/8/15.
 */
public abstract class UpdateRecipientResponseHandler extends DefaultXMLAPIResponseHandler {

    @Override
    public void onSuccessfulResponse(EngageResponseXML response) {
        UpdateRecipientResponse updateRecipientResponse = new UpdateRecipientResponse(response);
        onUpdateRecipientSuccess(updateRecipientResponse);
    }

    public abstract void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse);
}
