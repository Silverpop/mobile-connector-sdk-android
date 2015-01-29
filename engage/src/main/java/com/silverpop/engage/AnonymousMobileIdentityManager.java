package com.silverpop.engage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.android.volley.VolleyError;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.response.EngageResponseXML;

import java.util.HashMap;
import java.util.Map;

/**
 * Anonymous user support is now all in one place.  Keeping it around for backwards compatibility.
 * <p/>
 * The new approach is to use the {@link MobileIdentityManager} to create
 * the recipient's identity.
 * <p/>
 * Created by Lindsay Thurmond on 1/5/15.
 *
 * @deprecated
 */
public class AnonymousMobileIdentityManager extends BaseManager {

    private static final String TAG = AnonymousMobileIdentityManager.class.getName();

    private static AnonymousMobileIdentityManager instance = null;

    protected AnonymousMobileIdentityManager(Context context) {
        super(context);
    }

    public static synchronized AnonymousMobileIdentityManager init(Context context) {
        if (instance == null) {
            instance = new AnonymousMobileIdentityManager(context);
        }
        return instance;
    }

    public static AnonymousMobileIdentityManager get() {
        if (instance == null) {
            final String error = AnonymousMobileIdentityManager.class.getName() + " must be initialized before it can be retrieved";
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
        return instance;
    }

    /**
     * Create an anonymous user for the specified listId(database identifier)
     *
     * @param listId      Database identifier.
     * @param successTask AsyncTask to execute on successful result.
     * @param failureTask AsyncTask to execute on failure
     * @deprecated
     */
    public void createAnonymousUserList(String listId,
                                        final AsyncTask<EngageResponseXML, Void, Object> successTask,
                                        final AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = XMLAPI.addRecipientAnonymousToList(listId);
        getXMLAPIManager().postXMLAPI(createAnonymous, new AsyncTask<EngageResponseXML, Void, Object>() {
            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                return engageResponseXMLs[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {
                try {
                    EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                    if (responseXML.isSuccess()) {

                        String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                        EngageConfig.storeAnonymousUserId(getContext(), id);
                        successTask.execute(responseXML);
                    } else {
                        callFailureTask(failureTask, null);
                    }
                } catch (XMLResponseParseException e) {
                    Log.e(TAG, e.getMessage(), e);
                    callFailureTask(failureTask, null);
                }
            }
        }, failureTask);
    }

    private void callFailureTask(AsyncTask<VolleyError, Void, Object> failureTask, VolleyError error) {
        //[Lindsay Thurmond:1/29/15] TODO: handle errors better
        if (failureTask != null) {
            failureTask.execute(error);
        }
    }

    /**
     * @param userId
     * @param listId
     * @param primaryUserColumn
     * @param mergeColumn
     * @param successTask
     * @param failureTask
     * @deprecated
     */
    public void updateAnonymousUserToKnownUser(final String userId, final String listId, final String primaryUserColumn, final String mergeColumn,
                                               final AsyncTask<EngageResponseXML, Void, Object> successTask,
                                               final AsyncTask<VolleyError, Void, Object> failureTask) {

        EngageConfig.storePrimaryUserId(getContext(), userId);

        String anonymousId = EngageConfig.anonymousUserId(getContext());
        XMLAPI anonymousUser = XMLAPI.updateRecipient(anonymousId, listId);
        Map<String, Object> cols = new HashMap<String, Object>();
        cols.put(mergeColumn, userId);
        anonymousUser.addColumns(cols);
        getXMLAPIManager().postXMLAPI(anonymousUser, new AsyncTask<EngageResponseXML, Void, Object>() {
            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                return engageResponseXMLs[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {
                EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                if (responseXML.isSuccess()) {

                    XMLAPI updateRecipientXml = XMLAPI.builder()
                            .operation(XMLAPIOperation.UPDATE_RECIPIENT)
                            .listId(listId)
                            .syncField(primaryUserColumn, userId)
                            .column(mergeColumn, userId)
                            .build();

                    getXMLAPIManager().postXMLAPI(updateRecipientXml, successTask, failureTask);
                } else {
                    callFailureTask(failureTask, null);
                }
            }
        }, failureTask);
    }


    /**
     * @param listId
     * @return
     * @deprecated
     */
    public static XMLAPI addRecipientAnonymousToList(String listId) {
        XMLAPI addRecipientXml = XMLAPI.builder().operation(XMLAPIOperation.ADD_RECIPIENT).listId(listId).build();
        return addRecipientXml;
    }

}
