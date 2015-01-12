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
import com.silverpop.engage.util.DateUtil;

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
                createNewRecipient(setupRecipientHandler, existingMobileUserId, listId, mobileUserIdColumn);

            }
            // we have existing existingRecipientId but not mobileUserId - this really shouldn't happen, but just in case
            else if (TextUtils.isEmpty(existingMobileUserId)) {
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

    private void createNewRecipient(final SetupRecipientHandler setupRecipientHandler, String existingMobileUserId, String listId, String mobileUserIdColumn) {
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

    //[Lindsay Thurmond:1/12/15] TODO: test me
    public void checkIdentity(final Map<String, String> idFieldNamesToValues, final IdentityHandler identityHandler) {

        setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(String currentRecipientId) {
                checkForExistingRecipientAndUpdateIfNeeded(idFieldNamesToValues, currentRecipientId, identityHandler);
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

    private void checkForExistingRecipientAndUpdateIfNeeded(final Map<String, String> idFieldNamesToValues, final String currentRecipientId, final IdentityHandler identityHandler) {

        // look up recipient from silverpop
        final String listId = getEngageConfigManager().engageListId();
        final XMLAPI selectRecipientData = XMLAPI.selectRecipientData();
        selectRecipientData.addListIdParam(listId);
        for (Map.Entry<String, String> fieldValueEntry : idFieldNamesToValues.entrySet()) {
            String idFieldName = fieldValueEntry.getKey();
            String idValue = fieldValueEntry.getValue();
            selectRecipientData.addColumn(idFieldName, idValue);
        }

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
                        updateRecipientWithCustomId(currentRecipientId, listId, idFieldNamesToValues, identityHandler);
                    } else {
                        // an error happened with the request/response
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
                    final String existingMobileUserId = existingRecipientDataColumns.get(mobileUserIdColumnName);

                    // scenario 2 - existing recipient doesn't have a mobileUserId
                    if (TextUtils.isEmpty(existingMobileUserId)) {
                        handleExistingRecipientWithoutRecipientId(existingRecipientResponse, existingRecipientDataColumns, identityHandler, listId);
                    }
                    // scenario 3 - existing recipient has a mobileUserId
                    else {
                        handleExistingRecipientWithRecipientId(existingRecipientResponse, existingMobileUserId, currentRecipientId, listId, identityHandler);
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
     * Scenario 3 - existing recipient has a mobileUserId
     *
     * @param existingRecipientResponse
     * @param existingMobileUserId
     * @param currentRecipientId
     * @param listId
     * @param identityHandler
     */
    private void handleExistingRecipientWithRecipientId(final SelectRecipientResponse existingRecipientResponse, final String existingMobileUserId, String currentRecipientId, String listId, final IdentityHandler identityHandler) {
        // mark current recipient as merged
        XMLAPI updateCurrentRecipientXml = XMLAPI.updateRecipient(currentRecipientId, listId);
        if (getEngageConfigManager().mergeHistoryInMergedMarketingDatabase()) {
            updateCurrentRecipientXml.addColumn(getEngageConfigManager().mergedRecipientIdColumnName(), existingRecipientResponse.getRecipientId());
            updateCurrentRecipientXml.addColumn(getEngageConfigManager().mergedDateColumnName(), DateUtil.toGmtString(new Date()));
        }

        getXMLAPIManager().postXMLAPI(updateCurrentRecipientXml, new UpdateRecipientResponseHandler() {
            @Override
            public void onUpdateRecipientSuccess(UpdateRecipientResponse updateCurrentRecipientResponse) {
                // start using existing recipient id instead
                final String oldRecipientId = EngageConfig.recipientId(getContext());
                final String newRecipientId = existingRecipientResponse.getRecipientId();
                EngageConfig.storeRecipientId(getContext(), newRecipientId);
                EngageConfig.storeMobileUserId(getContext(), existingMobileUserId);

                // current recipient has been updated and we are now using the existing recipient instead, time to update audit table
                if (!getEngageConfigManager().mergeHistoryInAuditRecordTableDatabase()) {
                    updateAuditRecordWithMergeChanges(oldRecipientId, newRecipientId, identityHandler);
                } else {
                    identityHandler.onSuccess(EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext()));
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

    /**
     * Scenario 2 - existing recipient doesn't have a mobileUserId
     *
     * @param existingRecipientResponse
     * @param existingRecipientDataColumns
     * @param identityHandler
     * @param listId
     */
    private void handleExistingRecipientWithoutRecipientId(final SelectRecipientResponse existingRecipientResponse,
                                                           Map<String, String> existingRecipientDataColumns, final IdentityHandler identityHandler, String listId) {

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
            existingRecipientDataColumns.put(getEngageConfigManager().mobileUserIdColumnName(), mobileUserIdFromApp);

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
                    updateCurrentRecipient.addParam(getEngageConfigManager().mobileUserIdColumnName(), null);
                    if (getEngageConfigManager().mergeHistoryInMergedMarketingDatabase()) {
                        updateCurrentRecipient.addParam(getEngageConfigManager().mergedDateColumnName(), DateUtil.toGmtString(new Date()));
                        updateCurrentRecipient.addParam(getEngageConfigManager().mergedRecipientIdColumnName(), existingRecipientResponse.getRecipientId());
                    }

                    getXMLAPIManager().postXMLAPI(updateCurrentRecipient, new UpdateRecipientResponseHandler() {
                        @Override
                        public void onUpdateRecipientSuccess(UpdateRecipientResponse updateCurrentRecipientResponse) {
                            // start using existing recipient id instead
                            final String oldRecipientId = EngageConfig.recipientId(getContext());
                            final String newRecipientId = updateCurrentRecipientResponse.getRecipientId();
                            EngageConfig.storeRecipientId(getContext(), newRecipientId);

                            // both recipients have been updated, time to update audit table
                            if (!getEngageConfigManager().mergeHistoryInAuditRecordTableDatabase()) {
                                updateAuditRecordWithMergeChanges(oldRecipientId, newRecipientId, identityHandler);
                            } else {
                                identityHandler.onSuccess(EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext()));
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

    /**
     * Used with Scenario 2 & 3
     * <p/>
     * Should only be called if the 'mergeHistoryInAuditRecordTable' config property is {@code true}
     */
    private void updateAuditRecordWithMergeChanges(String oldRecipientId, String newRecipientId, final IdentityHandler identityHandler) {
        final String auditRecordTableId = EngageConfig.auditRecordTableId(getContext());
        if (TextUtils.isEmpty(auditRecordTableId)) {
            if (identityHandler != null) {
                identityHandler.onFailure(new EngageConfigException("Cannot update audit record without audit table id"));
            }
        } else {
            XMLAPI updateAuditRecordApi = XMLAPI.insertUpdateRelationalTable(auditRecordTableId);
            RelationalTableRow newAuditRecordRow = new RelationalTableRow();
            newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordOldRecipientIdColumnName(), oldRecipientId);
            newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordNewRecipientIdColumnName(), newRecipientId);
            newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordCreateDateColumnName(), DateUtil.toGmtString(new Date()));
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

    /**
     * Scenario 1 - no existing recipient
     */
    private void updateRecipientWithCustomId(String recipientId, String listId, Map<String, String> idFieldNamesToValues, final IdentityHandler identityHandler) {
        XMLAPI updateCurrentRecipientXml = XMLAPI.updateRecipient(recipientId, listId);
        for (Map.Entry<String, String> fieldValueEntry : idFieldNamesToValues.entrySet()) {
            String idFieldName = fieldValueEntry.getKey();
            String idValue = fieldValueEntry.getValue();
            updateCurrentRecipientXml.addColumn(idFieldName, idValue);
        }
        //[Lindsay Thurmond:1/7/15] TODO: are sync fields needed?

        getXMLAPIManager().postXMLAPI(updateCurrentRecipientXml, new UpdateRecipientResponseHandler() {
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
