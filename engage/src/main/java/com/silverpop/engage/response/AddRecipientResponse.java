package com.silverpop.engage.response;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public class AddRecipientResponse {

    private static final String TAG = AddRecipientResponse.class.getName();

    private EngageResponseXML responseXml;

    public AddRecipientResponse(String response) {
        this(new EngageResponseXML(response));
    }

    public AddRecipientResponse(EngageResponseXML engageResponseXML) {
        this.responseXml = engageResponseXML;
    }

    public boolean isSuccess() {
        boolean success = responseXml.isSuccess();
        return success;
    }

    public String getRecipientId() {
        String recipientId = responseXml.getString("envelope.body.result.recipientid");
        return recipientId;
    }

    public String getFaultString() {
        String faultString = responseXml.getString("envelope.body.fault.faultstring");
        return faultString;
    }

    public EngageResponseXML getResponseXml() {
        return responseXml;
    }
}
