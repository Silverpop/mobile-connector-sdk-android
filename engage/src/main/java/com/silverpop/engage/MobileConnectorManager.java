package com.silverpop.engage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.RelationalTableRow;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.EngageConfigException;
import com.silverpop.engage.exception.XMLAPIResponseException;
import com.silverpop.engage.recipient.SetupRecipientHandler;
import com.silverpop.engage.response.*;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.UpdateRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;

import java.util.Date;
import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public class MobileConnectorManager extends BaseManager {

    private static final String TAG = MobileConnectorManager.class.getName();
    private static MobileConnectorManager instance = null;

    protected MobileConnectorManager(Context context) {
        super(context);
        AutoMobileConnectorManager.init(context);
        AnonymousMobileConnectorManager.init(context);
    }

    public static synchronized MobileConnectorManager init(Context context) {
        if (instance == null) {
            instance = new MobileConnectorManager(context);
        }
        return instance;
    }

    public static MobileConnectorManager get() {
        if (instance == null) {
            final String error = MobileConnectorManager.class.getName() + " must be initialized before it can be retrieved";
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
        return instance;
    }

    public void setupRecipient(final SetupRecipientHandler setupRecipientHandler) {

        try {
            String existingRecipientId = EngageConfig.recipientId(getContext());
            String existingMobileUserId = EngageConfig.mobileUserId(getContext());
            String listId;
            String mobileUserIdColumn;

            if (!TextUtils.isEmpty(existingRecipientId) && !TextUtils.isEmpty(existingMobileUserId)) {
                // recipient previously setup, no need to go any further
                setupRecipientHandler.onSuccess(existingRecipientId);
                return;
            }

            // validate that we can auto generate the recipient
            try {
                if (TextUtils.isEmpty(existingMobileUserId) && !getEngageConfigManager().enableAutoAnonymousTracking()) {
                    String error = "Cannot create user with empty mobileUserId.  mobileUserId must be set manually or enableAutoAnonymousTracking must be set to true";
                    Log.e(TAG, error);
                    throw new EngageConfigException(error);
                }

                listId = getEngageConfigManager().engageListId();
                if (TextUtils.isEmpty(listId)) {
                    String error = "ListId must be configured before recipient can be auto configured.";
                    Log.e(TAG, error);
                    throw new EngageConfigException(error);
                }

                mobileUserIdColumn = getEngageConfigManager().mobileUserIdColumnName();
                if (TextUtils.isEmpty(mobileUserIdColumn)) {
                    String error = "mobileUserIdColumn must be configured before recipient can be auto configured.";
                    Log.e(TAG, error);
                    throw new EngageConfigException(error);
                }


            } catch (EngageConfigException e) {
                // failed validation, no need to keep trying
                setupRecipientHandler.onFailure(e);
                return;
            }

            // create a brand new recipient
            if (!TextUtils.isEmpty(existingRecipientId)) {
                String newMobileUserId = existingMobileUserId;
                // generate mobile user id if needed
                if (TextUtils.isEmpty(newMobileUserId)) {
                    newMobileUserId = AutoMobileConnectorManager.get().generateMobileUserId();
                    EngageConfig.storeMobileUserId(getContext(), newMobileUserId);
                    Log.d(TAG, "MobileUserId was auto generated");
                }


                XMLAPI addRecipient = XMLAPI.addRecipient(mobileUserIdColumn, newMobileUserId, listId, true);
                getXMLAPIManager().postXMLAPI(addRecipient, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        String recipientId = addRecipientResponse.getRecipientId();

                        if (TextUtils.isEmpty(recipientId)) {
                            if (setupRecipientHandler != null) {
                                setupRecipientHandler.onFailure(
                                        new XMLAPIResponseException("Empty recipientId returned from Silverpop", addRecipientResponse.getResponseXml()));
                            }
                        } else {
                            EngageConfig.storeRecipientId(getContext(), recipientId);
                            // EngageConfig.storeAnonymousUserId(context, recipientId);

                            if (setupRecipientHandler != null) {
                                setupRecipientHandler.onSuccess(recipientId);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        if (setupRecipientHandler != null) {
                            setupRecipientHandler.onFailure(new XMLAPIResponseException(exception));
                        }
                    }
                });

            }
            // we have existing existingRecipientId but not mobileUserId - this really shouldn't happen, but just in case
            else if (TextUtils.isEmpty(existingMobileUserId)) {
                //[Lindsay Thurmond:1/8/15] TODO: verify we should actually do this instead of just throwing an exception!
                // update the existing recipient with a mobile user id
                XMLAPI updateRecipientXml = XMLAPI.updateRecipient(existingRecipientId, listId);
                updateRecipientXml.addColumn(mobileUserIdColumn, existingMobileUserId);
                //[Lindsay Thurmond:1/8/15] TODO: do I need sync fields?
                getXMLAPIManager().postXMLAPI(updateRecipientXml, new UpdateRecipientResponseHandler() {
                    @Override
                    public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {
                        if (setupRecipientHandler != null) {
                            setupRecipientHandler.onSuccess(updateRecipientResponse.getRecipientId());
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        if (setupRecipientHandler != null) {
                            setupRecipientHandler.onFailure(exception);
                        }
                    }
                });
            }
        } catch (Exception e) {
            // just in case something unexpected happens
            Log.e(TAG, e.getMessage(), e);
            if (setupRecipientHandler != null) {
                setupRecipientHandler.onFailure(e);
            }
        }
    }

    public void checkIdentity(final String idFieldName, final String idValue, final IdentityHandler identityHandler) {

        setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(String recipientId) {
                checkForExistingRecipientAndUpdateIfNeeded(idFieldName, idValue, recipientId, identityHandler);
            }

            @Override
            public void onFailure(Exception e) {
                if (identityHandler != null) {
                    Log.e(TAG, e.getMessage(), e);
                    identityHandler.onFailure(e);
                }
            }
        });
    }

    //[Lindsay Thurmond:1/9/15] TODO: change to support multiple id columns
    public void checkForExistingRecipientAndUpdateIfNeeded(final String idFieldName, final String idValue, final String recipientId, final IdentityHandler identityHandler) {

        // look up recipient from silverpop
        final String listId = getEngageConfigManager().engageListId();
        final XMLAPI selectRecipientData = XMLAPI.selectRecipientData();
        selectRecipientData.addListIdParam(listId);
        selectRecipientData.addColumn(idFieldName, idValue);

        getXMLAPIManager().postXMLAPI(selectRecipientData, new XMLAPIResponseHandler() {
            @Override
            public void onSuccess(EngageResponseXML response) {
                final SelectRecipientResponse existingRecipientResponse = new SelectRecipientResponse(response);

                //[Lindsay Thurmond:1/8/15] TODO: what if selected recipient has same recipient id? merge? ignore?

                // scenario 1 - recipient not found
                if (!existingRecipientResponse.isSuccess()) {

                    // user not found or error with actual request?
                    if (existingRecipientResponse.getErrorCode() == ErrorCode.RECIPIENT_NOT_LIST_MEMBER) {
                        // recipient doesn't exist
                        updateRecipientWithCustomId(recipientId, listId, idFieldName, idValue, identityHandler);
                    } else {
                        // an error happened with the request/response
                        //[Lindsay Thurmond:1/7/15] TODO: handle error
                        if (identityHandler != null) {
                            // request failed for unknown reason, time to bail
                            identityHandler.onFailure(new XMLAPIResponseException(response));
                        }
                    }
                }
                // we found an existing recipient - does it have a mobileUserId?
                else {

                    Map<String, String> existingRecipientDataColumns = existingRecipientResponse.getColumns();
                    final String mobileUserIdColumnName = getEngageConfigManager().mobileUserIdColumnName();
                    String mobileUserId = existingRecipientDataColumns.get(mobileUserIdColumnName);

                    // scenario 2 - existing recipient doesn't have a mobileUserId
                    if (TextUtils.isEmpty(mobileUserId)) {
                        //[Lindsay Thurmond:1/7/15] TODO: updateRecipient(mobileUserId = nil, recipientId = currentRecipientId, mergedRecipientId=existingRecipient.recipientId, mergeDate=now())

                        // keep all data from existing recipient, just add the mobile user id to it
                        final String mobileUserIdFromApp = EngageConfig.mobileUserId(getContext());
                        if (TextUtils.isEmpty(mobileUserIdFromApp)) {
                            final String error = "Cannot find mobileUserId to update the existing applicant with for recipientId = " + existingRecipientResponse.getRecipientId();
                            Log.e(TAG, error);
                            // time to bail, can't go any further
                            if (identityHandler != null) {
                                identityHandler.onFailure(new EngageConfigException(error));
                            }
                        } else {
                            //[Lindsay Thurmond:1/8/15] TODO: do i need to send all the data columns or just the ones I want to update?
                            existingRecipientDataColumns.put(mobileUserIdColumnName, mobileUserIdFromApp);

                            // update existing recipient on server with new mobile user id
                            final XMLAPI updateExistingRecipient = XMLAPI.updateRecipient(existingRecipientResponse.getRecipientId(), listId);
                            updateExistingRecipient.addColumns((Map<String, Object>) (Object) existingRecipientDataColumns);
                            getXMLAPIManager().postXMLAPI(updateExistingRecipient, new UpdateRecipientResponseHandler() {
                                @Override
                                public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {

                                    // clear mobile user id from recipient in app config, and set its merged_recipient_id_ and merged date

                                    // for recipient currently in the app config
                                    XMLAPI updateCurrentRecipient = XMLAPI.updateRecipient(
                                            EngageConfig.recipientId(getContext()), getEngageConfigManager().engageListId());
                                    updateCurrentRecipient.addParam(mobileUserIdColumnName, null);
                                    //[Lindsay Thurmond:1/8/15] TODO: what should date format be?
                                    updateCurrentRecipient.addParam(getEngageConfigManager().mergedDateColumnName(), new Date());
                                    updateCurrentRecipient.addParam(getEngageConfigManager().mergedRecipientIdColumnName(), existingRecipientResponse.getRecipientId());

                                    getXMLAPIManager().postXMLAPI(updateCurrentRecipient, new UpdateRecipientResponseHandler() {
                                        @Override
                                        public void onUpdateRecipientSuccess(UpdateRecipientResponse updateCurrentRecipientResponse) {
                                            // start using existing recipient id instead
                                            final String oldRecipientId = EngageConfig.recipientId(getContext());
                                            final String newRecipientId = updateCurrentRecipientResponse.getRecipientId();
                                            EngageConfig.storeRecipientId(getContext(), newRecipientId);
                                            // both recipients have been updated, time to update audit table
                                            final String auditRecordTableId = EngageConfig.auditRecordTableId(getContext());
                                            if (TextUtils.isEmpty(auditRecordTableId)) {
                                                if (identityHandler != null) {
                                                    //[Lindsay Thurmond:1/9/15] TODO: should I do this check earlier?
                                                    identityHandler.onFailure(new EngageConfigException("Cannot update audit record without audit table id"));
                                                }
                                            } else {
                                                XMLAPI updateAuditRecordApi = XMLAPI.insertUpdateRelationalTable(auditRecordTableId);
                                                RelationalTableRow newAuditRecordRow = new RelationalTableRow();
                                                newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordOldRecipientIdColumnName(), oldRecipientId);
                                                newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordNewRecipientIdColumnName(), newRecipientId);
                                                //[Lindsay Thurmond:1/9/15] TODO: date format
                                                newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordCreateDateColumnName(), new Date());
                                                updateAuditRecordApi.addRow(newAuditRecordRow);

                                                getXMLAPIManager().postXMLAPI(updateAuditRecordApi, new XMLAPIResponseHandler() {
                                                    @Override
                                                    public void onSuccess(EngageResponseXML response) {

                                                        if (response.isSuccess()) {
                                                            // congratz, we're done!
                                                            if (identityHandler != null) {
                                                                identityHandler.onSuccess(EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext()));
                                                            }
                                                        } else {
                                                            if (identityHandler != null) {
                                                                identityHandler.onFailure(new XMLAPIResponseException(response));
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Exception exception) {
                                                        if (identityHandler != null) {
                                                            identityHandler.onFailure(exception);
                                                        }
                                                    }
                                                });

                                            }


                                        }

                                        @Override
                                        public void onFailure(Exception exception) {
                                            // failed to update current recipient, can't continue
                                            if (identityHandler != null) {
                                                identityHandler.onFailure(exception);
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onFailure(Exception exception) {
                                    // first recipient update failed, can't go any further
                                    if (identityHandler != null) {
                                        identityHandler.onFailure(exception);
                                    }
                                }
                            });

                        }

                    }
                    // scenario 3 - existing recipient has a mobileUserId
                    else {
                        //[Lindsay Thurmond:1/9/15] TODO: implement me!
                    }

                }


                Log.d(TAG, response.getXml());

            }

            @Override
            public void onFailure(Exception exception) {
                Log.d(TAG, exception.getMessage(), exception);
            }
        });
    }

    /**
     * Scenario 1 - no existing recipient
     */
    protected void updateRecipientWithCustomId(String recipientId, String listId, String idFieldName, String idValue, final IdentityHandler identityHandler) {
        XMLAPI xmlapi = XMLAPI.updateRecipient(recipientId, listId);
        xmlapi.addColumn(idFieldName, idValue);
        //[Lindsay Thurmond:1/9/15] TODO: support multiple ids

        //[Lindsay Thurmond:1/7/15] TODO: are sync fields needed?

        getXMLAPIManager().postXMLAPI(xmlapi, new UpdateRecipientResponseHandler() {
            @Override
            public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {
                if (identityHandler != null) {
                    identityHandler.onSuccess(updateRecipientResponse.getRecipientId(), EngageConfig.mobileUserId(getContext()));
                }
            }

            @Override
            public void onFailure(Exception exception) {
                if (identityHandler != null) {
                    identityHandler.onFailure(exception);
                }
            }
        });
    }
}
