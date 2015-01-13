package com.silverpop.engage.response;

import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public class SelectRecipientResponse {

    private static final String TAG = SelectRecipientResponse.class.getName();

    private EngageResponseXML responseXml;

    public SelectRecipientResponse(String response) {
        this(new EngageResponseXML(response));
    }

    public SelectRecipientResponse(EngageResponseXML engageResponseXml) {
        responseXml = engageResponseXml;
    }

    public boolean isSuccess() {
        boolean isSuccess = responseXml.isSuccess();
        return isSuccess;
    }

    public String getEmail() {
        String email = responseXml.getString("envelope.body.result.email");
        return email;
    }

    public String getRecipientId() {
        String recipientId = responseXml.getString("envelope.body.result.recipientid");
        return recipientId;
    }

    public Integer getEmailType() {
        Integer emailType = responseXml.getInteger("envelope.body.result.emailtype");
        return emailType;
    }

    /**
     * @return date string in format 6/25/04 3:29 PM
     */
    public String getLastModified() {
        String lastModified = responseXml.getString("envelope.body.result.lastmodified");
        return lastModified;
    }

    public Integer getCreatedFrom() {
        Integer createdFrom = responseXml.getInteger("envelope.body.result.createdfrom");
        return createdFrom;
    }

    /**
     * @return date string in format 6/25/04 3:29 PM
     */
    public String getOptedIn() {
        String optedIn = responseXml.getString("envelope.body.result.optedin");
        return optedIn;
    }

    /**
     * @return date string in format 6/25/04 3:29 PM
     */
    public String getOptedOut() {
        String optedOut = responseXml.getString("envelope.body.result.optedout");
        return optedOut;
    }

    public Map<String, String> getColumns() {
        Map<String, String> columns = responseXml.getColumns();
        return columns;
    }

    public String getColumnValue(String columnName) {
        String columnValue = responseXml.getColumnValue(columnName);
        return columnValue;
    }

    public String getFaultString() {
        String faultString = responseXml.getFaultString();
        return faultString;
    }

    public Integer getErrorId() {
        Integer errorId = responseXml.getErrorId();
        return errorId;
    }

    public ErrorCode getErrorCode() {
        Integer errorId = getErrorId();
        if (errorId != null) {
            return ErrorCode.findByNumber(errorId);
        }
        return null;
    }

}
