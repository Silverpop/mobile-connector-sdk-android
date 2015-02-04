package com.silverpop.engage.response;

/**
 * Created by Lindsay Thurmond on 1/7/15.
 */
public class UpdateRecipientResponse {

    private static final String TAG = UpdateRecipientResponse.class.getName();

    private EngageResponseXML responseXml;

    public UpdateRecipientResponse(String response) {
        this(new EngageResponseXML(response));
    }

    public UpdateRecipientResponse(EngageResponseXML engageResponseXml) {
        responseXml = engageResponseXml;
    }

    public boolean isSuccess() {
        boolean isSuccess = responseXml.isSuccess();
        return isSuccess;
    }

    public String getRecipientId(){
        String recipientId = responseXml.getString("envelope.body.result.recipientid");
        return recipientId;
    }
}
