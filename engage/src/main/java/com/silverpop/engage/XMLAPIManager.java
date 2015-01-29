package com.silverpop.engage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.network.XMLAPIClient;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.handler.XMLAPIResponseFailure;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;

/**
 * Methods for interacting with the Silverpop Engage XML API.
 *
 * Created by jeremydyer on 5/21/14.
 */
public class XMLAPIManager extends BaseManager {

    private static final String TAG = XMLAPIManager.class.getName();

    private XMLAPIClient xmlapiClient;

    private static XMLAPIManager xmlapiManager = null;

    private XMLAPIManager(Context context) {
        super(context);
        xmlapiClient = XMLAPIClient.init(context);
    }

    public static synchronized XMLAPIManager init(Context context) {
        if (xmlapiManager == null) {
            xmlapiManager = new XMLAPIManager(context);
        }
        return xmlapiManager;
    }

    public static XMLAPIManager get() {
        if (xmlapiManager == null) {
            Log.e(TAG, "EngageSDK - You have not yet initialized your XMLAPIManager instance! " +
                    "A null XMLAPIManager instance will be returned!");
        }
        return xmlapiManager;
    }

    /**
     * Post an XMLAPI request to Engage
     *
     * @param api         XMLAPI operation desired.
     * @param successTask AsyncTask to execute on successful result.
     * @param failureTask AsyncTask to execute on failure
     */
    public void postXMLAPI(XMLAPI api,
                           final AsyncTask<EngageResponseXML, Void, Object> successTask,
                           final AsyncTask<VolleyError, Void, Object> failureTask) {

        Response.Listener<String> successListener = new Response.Listener<String>() {
            public void onResponse(String response) {
                //If null the user doesn't want anything special to happen.
                if (successTask != null) {

                    //Perform the EngageSDK internal logic before passing processing off to user defined AsyncTask.
                    EngageResponseXML responseXML = new EngageResponseXML(response);
                    successTask.execute(responseXML);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);

                //Call the SDK user defined method.
                if (failureTask != null) {
                    failureTask.execute(error);
                }
            }
        };
        xmlapiClient.postResource(api, successListener, errorListener);
    }

    /**
     * Post an XMLAPI request to Engage using a generic handler (as opposed to AsyncTasks) for the response.
     *
     * @param api             XMLAPI operation desired.
     * @param responseHandler functionality to run on success or failure of the request.
     */
    public void postXMLAPI(XMLAPI api,
                           final XMLAPIResponseHandler responseHandler) {
        xmlapiClient.postResource(api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (responseHandler != null) {
                    responseHandler.onSuccess(new EngageResponseXML(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (responseHandler != null) {
                    responseHandler.onFailure(new XMLAPIResponseFailure(volleyError));
                }
            }
        });
    }

    /**
     * Create an anonymous user for the specified listId(database identifier).
     * <p/>
     * Method left here for backwards compatibility, but functionality has been moved to
     * {@link AnonymousMobileIdentityManager#createAnonymousUserList(String, android.os.AsyncTask, android.os.AsyncTask)}
     * which you are encouraged to use instead.
     *
     * @param listId      Database identifier.
     * @param successTask AsyncTask to execute on successful result.
     * @param failureTask AsyncTask to execute on failure
     *
     * @deprecated Functionality has been moved to {@link com.silverpop.engage.AnonymousMobileIdentityManager#createAnonymousUserList(String, android.os.AsyncTask, android.os.AsyncTask)}
     */
    public void createAnonymousUserList(String listId,
                                        AsyncTask<EngageResponseXML, Void, Object> successTask,
                                        AsyncTask<VolleyError, Void, Object> failureTask) {

        AnonymousMobileIdentityManager.get().createAnonymousUserList(listId, successTask, failureTask);
    }


    //[Lindsay Thurmond:12/29/14] TODO: identical to createAnonymousUserList() - fix or delete me
    /**
     * Method left here for backwards compatibility, but functionality has been moved to
     * {@link AnonymousMobileIdentityManager#updateAnonymousUserToKnownUser(String, android.os.AsyncTask, android.os.AsyncTask)}
     * which you are encouraged to use instead.
     *
     * @param listId      Database identifier.
     * @param successTask AsyncTask to execute on successful result.
     * @param failureTask AsyncTask to execute on failure
     *
     * @deprecated Functionality has been moved to {@link com.silverpop.engage.AnonymousMobileIdentityManager#updateAnonymousUserToKnownUser(String, android.os.AsyncTask, android.os.AsyncTask)}
     */
    public void updateAnonymousUserToKnownUser(String listId, AsyncTask<EngageResponseXML, Void, Object> successTask,
                                               AsyncTask<VolleyError, Void, Object> failureTask) {

        AnonymousMobileIdentityManager.get().updateAnonymousUserToKnownUser(listId, successTask, failureTask);
    }

}
