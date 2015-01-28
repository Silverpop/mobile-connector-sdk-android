package com.silverpop.engage;

import java.util.Map;
import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.network.XMLAPIClient;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.exception.XMLResponseParseException;


/**
 * Created by jeremydyer on 5/21/14.
 */
public class XMLAPIManager {

    private static final String TAG = XMLAPIManager.class.getName();

    private Context context;
    private XMLAPIClient xmlapiClient;

    private static XMLAPIManager xmlapiManager = null;

    private XMLAPIManager(Context context, String clientId, String clientSecret, String refreshToken, String host) {
        setContext(context);
        xmlapiClient = XMLAPIClient.init(context, clientId, clientSecret, refreshToken, host);
    }

    public static XMLAPIManager initialize(Context context, String clientId, String clientSecret, String refreshToken, String host) {
        if (xmlapiManager == null) {
            xmlapiManager = new XMLAPIManager(context, clientId, clientSecret, refreshToken, host);
        }
        return xmlapiManager;
    }

    public static XMLAPIManager get() {
        if (xmlapiManager == null) {
            Log.e(TAG, "EngageSDK - You have not yet initialized your XMLAPIManager instance! " +
                    "A null XMLAPIManager instance will be returned!");
            return null;
        } else {
            return xmlapiManager;
        }
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
                                        final AsyncTask<EngageResponseXML, Void, Object> successTask,
                                        final AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = XMLAPI.addRecipientAnonymousToList(listId);
        postXMLAPI(createAnonymous, new AsyncTask<EngageResponseXML, Void, Object>() {
            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                return engageResponseXMLs[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {
                try {
                    EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                    String result = responseXML.valueForKeyPath("envelope.body.result.success");
                    if (result.equalsIgnoreCase("true")) {
                    	String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                    	EngageConfig.storeAnonymousUserId(context, id);
                    	successTask.execute(responseXML);
                    } else {
                    	callFailureTask(failureTask);
                    }
                } catch (XMLResponseParseException e) {
                    e.printStackTrace();
                	callFailureTask(failureTask);
                }
            }
        }, failureTask);
    }
    private void callFailureTask(AsyncTask<VolleyError, Void, Object> failureTask){
    	//TODO: implement
    }

    public void updateAnonymousUserToKnownUser(String listId, AsyncTask<EngageResponseXML, Void, Object> successTask,
                                               AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = XMLAPI.addRecipientAnonymousToList(listId);
        postXMLAPI(createAnonymous, successTask, failureTask);
    }
    public void updateAnonymousUserToKnownUser(final String userId, final String listId, final String primaryUserColumn, final String mergeColumn, final AsyncTask<EngageResponseXML, Void, Object> successTask,
            final AsyncTask<VolleyError, Void, Object> failureTask) {
    	EngageConfig.storePrimaryUserId(context, userId);
    	
    	String anonymousId = EngageConfig.anonymousUserId(context);
    	XMLAPI anonymousUser = XMLAPI.updateRecipient(anonymousId, listId);
    	Map<String, Object> cols = new HashMap<String, Object>();
    	cols.put(mergeColumn, userId);    	
    	anonymousUser.addColumns(cols);
    	postXMLAPI(anonymousUser, new AsyncTask<EngageResponseXML, Void, Object>() {
            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                return engageResponseXMLs[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {
                try {
                    EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                    String result = responseXML.valueForKeyPath("envelope.body.result.success");
                    if (result.equalsIgnoreCase("true")) {
                    	Map<String, Object> params = new HashMap<String, Object>();
                    	params.put("LIST_ID", listId); 
                    	XMLAPI mobileUser = new XMLAPI("UpdateRecipient", params);
                    	
                    	Map<String, Object> syncFields = new HashMap<String, Object>();
                    	syncFields.put(primaryUserColumn, userId); 
                    	mobileUser.addSyncFields(syncFields);

                    	Map<String, Object> columns = new HashMap<String, Object>();
                    	columns.put(mergeColumn, userId); 
                    	mobileUser.addColumns(columns);
                    	postXMLAPI(mobileUser, successTask, failureTask);
                    } else {
                    	callFailureTask(failureTask);
                    }
                } catch (XMLResponseParseException e) {
                    e.printStackTrace();
                	callFailureTask(failureTask);
                }
            }
        }, failureTask);
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
