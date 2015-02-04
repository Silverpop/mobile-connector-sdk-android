package com.silverpop.engage.response.handler;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/24/15.
 */
public class XMLAPIResponseFailure {

    private EngageResponseXML responseXml;

    private Exception exception;

    public XMLAPIResponseFailure(EngageResponseXML responseXml) {
        this.responseXml = responseXml;
    }

    public XMLAPIResponseFailure(Exception exception) {
        this.exception = exception;
    }

    public EngageResponseXML getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(EngageResponseXML responseXml) {
        this.responseXml = responseXml;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
