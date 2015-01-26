package com.silverpop.engage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.android.volley.VolleyError;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIElement;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.response.EngageResponseXML;

import java.util.LinkedHashMap;
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
                                        AsyncTask<EngageResponseXML, Void, Object> successTask,
                                        AsyncTask<VolleyError, Void, Object> failureTask) {
        XMLAPI createAnonymous = addRecipientAnonymousToList(listId);
        XMLAPIManager.get().postXMLAPI(createAnonymous, successTask, failureTask);
    }


    //[Lindsay Thurmond:12/29/14] TODO: identical to createAnonymousUserList() - fix or delete me
//    public void updateAnonymousUserToKnownUser(String listId, AsyncTask<EngageResponseXML, Void, Object> successTask,
//                                               AsyncTask<VolleyError, Void, Object> failureTask) {
//        XMLAPI createAnonymous = addRecipientAnonymousToList(listId);
//        XMLAPIManager.get().postXMLAPI(createAnonymous, successTask, failureTask);
//    }

    /**
     * @param listId
     * @return
     * @deprecated
     */
    public static XMLAPI addRecipientAnonymousToList(String listId) {
        Map<String, Object> bodyElements = new LinkedHashMap<String, Object>();
        bodyElements.put(XMLAPIElement.LIST_ID.toString(), listId);

        XMLAPI api = new XMLAPI(XMLAPIOperation.ADD_RECIPIENT, bodyElements);
        return api;
    }

}
