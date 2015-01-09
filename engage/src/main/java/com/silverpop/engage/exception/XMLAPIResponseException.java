package com.silverpop.engage.exception;

import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by Lindsay Thurmond on 1/8/15.
 */
public class XMLAPIResponseException extends Exception {

    private EngageResponseXML xmlResponse;

    public XMLAPIResponseException(String detailMessage) {
        super(detailMessage);
    }

    public XMLAPIResponseException(EngageResponseXML xmlResponse) {
        super(xmlResponse != null ? xmlResponse.getFaultString() : "Unknown error");
        this.xmlResponse = xmlResponse;
    }

    public XMLAPIResponseException(String detailMessage, EngageResponseXML xmlResponse) {
        super(detailMessage);
        this.xmlResponse = xmlResponse;
    }

    public XMLAPIResponseException(Throwable throwable) {
        super(throwable);
    }

    public EngageResponseXML getXmlResponse() {
        return xmlResponse;
    }

    public void setXmlResponse(EngageResponseXML xmlResponse) {
        this.xmlResponse = xmlResponse;
    }
}
