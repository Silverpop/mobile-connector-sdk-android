package com.silverpop.engage.network;

import com.android.volley.Response;
import com.silverpop.engage.domain.XMLAPI;

import java.io.Serializable;

/**
 * Created by jeremydyer on 6/4/14.
 */
public class RequestCacheWrapper
    implements Serializable {

    private XMLAPI xmlapi;
    private Response.Listener<String> successListener;
    private Response.ErrorListener errorListener;

    public RequestCacheWrapper(XMLAPI api, Response.Listener<String> suc, Response.ErrorListener err) {
        setXmlapi(api);
        setSuccessListener(suc);
        setErrorListener(err);
    }

    public XMLAPI getXmlapi() {
        return xmlapi;
    }

    public void setXmlapi(XMLAPI xmlapi) {
        this.xmlapi = xmlapi;
    }

    public Response.Listener<String> getSuccessListener() {
        return successListener;
    }

    public void setSuccessListener(Response.Listener<String> successListener) {
        this.successListener = successListener;
    }

    public Response.ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(Response.ErrorListener errorListener) {
        this.errorListener = errorListener;
    }
}
