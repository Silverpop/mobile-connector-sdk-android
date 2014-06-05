package com.silverpop.engage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.network.XMLAPIClient;
import com.silverpop.engage.response.EngageResponseXML;


/**
 * Created by jeremydyer on 5/21/14.
 */
public class XMLAPIManager {

    private static final String TAG = XMLAPIManager.class.getName();

    private Context context;
    private XMLAPIClient xmlapiClient;

    public XMLAPIManager(Context context, String clientId, String clientSecret, String refreshToken, String host) {
        setContext(context);
        xmlapiClient = XMLAPIClient.init(context, clientId, clientSecret, refreshToken, host);
    }

    /**
     * Post an XMLAPI request to Engage
     *
     * @param api
     *      XMLAPI operation desired.
     *
     * @param successTask
     *      AsyncTask to execute on successful result.
     *
     * @param failureTask
     *      AsyncTask to execute on failure
     */
    public void postXMLAPI(XMLAPI api,
                           AsyncTask<EngageResponseXML, Void, Object> successTask,
                           AsyncTask<VolleyError, Void, Object> failureTask) {
        Response.Listener<String> successListener = successListenerForXMLAPI(api, successTask);
        Response.ErrorListener errorListener = null;
        xmlapiClient.postResource(api, successListener, errorListener);
    }

    /**
     * Create an anonymous user for the specified listId(database identifier)
     *
     * @param listId
     *      Database identifier.
     *
     * @param successTask
     *      AsyncTask to execute on successful result.
     *
     * @param failureTask
     *      AsyncTask to execute on failure
     */
    public void createAnonymousUserList(String listId,
                                        AsyncTask<EngageResponseXML, Void, Object> successTask,
                                        AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = XMLAPI.addRecipientAnonymousToList(listId);
        postXMLAPI(createAnonymous, successTask, failureTask);
    }


    public void updateAnonymousUserToKnownUser(String listId, AsyncTask<EngageResponseXML, Void, Object> successTask,
                                               AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = XMLAPI.addRecipientAnonymousToList(listId);
        postXMLAPI(createAnonymous, successTask, failureTask);
    }

    /**
     * Handles the successful completion of the UBF post
     *
     * @return
     */
    private Response.Listener<String> successListenerForXMLAPI(final XMLAPI api,
                                                                  final AsyncTask<EngageResponseXML, Void, Object> successTask) {
        return new Response.Listener<String>() {
            public void onResponse(String response) {

                //Perform the EngageSDK internal logic before passing processing off to user defined AsyncTask.
                EngageResponseXML responseXML = new EngageResponseXML(response);

                //If null the user doesn't want anything special to happen.
                if (successTask != null) {
                    successTask.execute(responseXML);
                }
            }
        };
    }


    /**
     * Handles the failure of a UBF event post.
     *
     * @return
     */
    private Response.ErrorListener errorListenerForXMLAPI(final XMLAPI api, final AsyncTask<VolleyError, Void, Object> failureTask) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());

                //Call the SDK user defined method.
                if (failureTask != null) {
                    failureTask.execute(error);
                }
            }
        };
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
