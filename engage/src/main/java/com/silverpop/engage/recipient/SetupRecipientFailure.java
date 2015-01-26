package com.silverpop.engage.recipient;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/24/15.
 */
public class SetupRecipientFailure {

    private Exception exception;

    private String message;

    private EngageResponseXML responseXml;

    public SetupRecipientFailure() {
    }

    public SetupRecipientFailure(String message, Exception exception) {
        this.exception = exception;
        this.message = message;
    }

    public SetupRecipientFailure(String message, EngageResponseXML responseXml) {
        this.message = message;
        this.responseXml = responseXml;
    }

    public SetupRecipientFailure(Exception exception, EngageResponseXML responseXml) {
        this.exception = exception;
        this.responseXml = responseXml;
    }

    public SetupRecipientFailure(Exception exception) {
        this.exception = exception;
    }

    public SetupRecipientFailure(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public EngageResponseXML getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(EngageResponseXML responseXml) {
        this.responseXml = responseXml;
    }
}
