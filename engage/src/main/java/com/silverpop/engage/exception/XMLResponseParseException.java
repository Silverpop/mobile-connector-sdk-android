package com.silverpop.engage.exception;

/**
 * Created by jeremydyer on 5/23/14.
 */
public class XMLResponseParseException
    extends Exception {

    private String xml;
    private String keyPath;

    public XMLResponseParseException(String message, String xml, String keyPath) {
        super(message);
        setXml(xml);
        setKeyPath(keyPath);
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
