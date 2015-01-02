package com.silverpop.engage.user;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.VolleyError;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.EngageConfigException;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.util.uuid.UUIDGenerator;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public class UserManager {

    private static final String TAG = UserManager.class.getName();

    private static UserManager instance = null;

    private static final String DEFAULT_UUID_GENERATOR_CLASS = "com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator";

    private final Context appContext;

    private UserManager(Context context) {
        this.appContext = context;
    }

    public static synchronized UserManager initialize(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    public static UserManager get() {
        if (instance == null) {
            Log.e(TAG, "EngageSDK - You have not yet initialized your UserManager instance! Returning null UserManager!");
        }
        return instance;
    }


    public void setupRecipient(final SetupUserHandler setupUserHandler) throws EngageConfigException {
        final EngageConfigManager engageConfigManager = getEngageConfigManager();

        String mobileUserId = EngageConfig.mobileUserId(appContext);

        if (TextUtils.isEmpty(mobileUserId)) {
            String error = "Cannot create user with empty mobileUserId.  mobileUserId must be set manually or enableAutoAnonymousTracking must be set to true";
            Log.e(TAG, error);
            throw new EngageConfigException(error);
        }

        String listId = engageConfigManager.engageListId();
        if (TextUtils.isEmpty(listId)) {
            String error = "ListId must be configured before recipient can be auto configured.";
            Log.e(TAG, error);
            throw new EngageConfigException(error);
        }

        String mobileUserIdColumn = engageConfigManager.mobileUserIdColumnName();
        if (TextUtils.isEmpty(mobileUserIdColumn)) {
            String error = "mobileUserIdColumn must be configured before recipient can be auto configured.";
            Log.e(TAG, error);
            throw new EngageConfigException(error);
        }

        // generate mobile user id if needed
        if (TextUtils.isEmpty(mobileUserId) && engageConfigManager.enableAutoAnonymousTracking()) {
            mobileUserId = generateMobileUserId();
            Log.d(TAG, "MobileUserId was auto generated");
        }

        EngageConfig.storeMobileUserId(appContext, mobileUserId);

        postRecipientToSilverpop(setupUserHandler, mobileUserId, listId, mobileUserIdColumn);

    }

    private void postRecipientToSilverpop(final SetupUserHandler setupUserHandler, String mobileUserId, String listId, String mobileUserIdColumn) {
        XMLAPI addRecipient = XMLAPI.addRecipient(mobileUserIdColumn, mobileUserId, listId, true);

        XMLAPIManager.get().postXMLAPI(addRecipient, new AsyncTask<EngageResponseXML, Void, Object>() {

            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                return engageResponseXMLs[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {

                try {
                    EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                    final String result = responseXML.valueForKeyPath("envelope.body.result.success");
                    if ("true".equalsIgnoreCase(result)) {
                        String recipientId = responseXML.valueForKeyPath("envelope.body.result.recipientid");

                        if (TextUtils.isEmpty(recipientId)) {
                            if (setupUserHandler != null) {
                                setupUserHandler.onCreateFailure("Empty recipientId returned from Silverpop");
                            }
                        } else {
//                            EngageConfig.storeRecipientId(appContext, recipientId);
                            EngageConfig.storeAnonymousUserId(appContext, recipientId);

                            if (setupUserHandler != null) {
                                setupUserHandler.onCreateSuccess(recipientId);
                            }
                        }

                    } else {
                        String faultString = responseXML.valueForKeyPath("envelope.body.fault.faultstring");
                        if (setupUserHandler != null) {
                            setupUserHandler.onCreateFailure(faultString);
                        }
                    }

                } catch (Exception e) {
                    String error = e.getMessage();
                    Log.e(TAG, error);
                    if (setupUserHandler != null) {
                        setupUserHandler.onCreateFailure(error);
                    }
                }

            }
        }, new AsyncTask<VolleyError, Void, Object>() {
            @Override
            protected Object doInBackground(VolleyError... volleyErrors) {
                Log.e(TAG, "Failure posting AddRecipient to auto create user event to Silverpop");
                return volleyErrors[0];
            }

            @Override
            protected void onPostExecute(Object responseObject) {
                VolleyError error = (VolleyError) responseObject;
                String message = "Error creating anonymous user: " + error.getMessage();
                if (setupUserHandler != null) {
                    setupUserHandler.onCreateFailure(message);
                }
            }
        });
    }

    private EngageConfigManager getEngageConfigManager() {
        return EngageConfigManager.get(appContext);
    }

    private String generateMobileUserId() {
        String uuidClassFullPackageName = getEngageConfigManager().mobileUserIdGeneratorClassName();

        UUIDGenerator uuidGenerator;
        try {
            Class uuidClassName = Class.forName(uuidClassFullPackageName);
             uuidGenerator = (UUIDGenerator) uuidClassName.newInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Unable to initialize UUID generator class '" + uuidClassFullPackageName +
                    ".' Using default implementation of " + DEFAULT_UUID_GENERATOR_CLASS +": " + ex.getMessage());

            uuidGenerator = new DefaultUUIDGenerator();
        }

        String mobileUserId = uuidGenerator.generateUUID();
        return mobileUserId;
    }

}
