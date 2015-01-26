package com.silverpop.engage.recipient;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/24/15.
 */
public class CheckIdentityFailure {

    private Exception exception;

    private String message;

    private EngageResponseXML responseXml;

    public CheckIdentityFailure() {
    }

    public CheckIdentityFailure(EngageResponseXML responseXml) {
        this.responseXml = responseXml;
    }

    public CheckIdentityFailure(String message, Exception exception) {
        this.exception = exception;
        this.message = message;
    }

    public CheckIdentityFailure(Exception exception) {
        this.exception = exception;
    }

    public CheckIdentityFailure(String message) {
        this.message = message;
    }

    public CheckIdentityFailure(String message, Exception exception, EngageResponseXML responseXml) {
        this.exception = exception;
        this.message = message;
        this.responseXml = responseXml;
    }

    public CheckIdentityFailure(Exception exception, EngageResponseXML responseXml) {
        this.exception = exception;
        this.responseXml = responseXml;
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
