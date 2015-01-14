package com.silverpop.engage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.RelationalTableRow;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.exception.EngageConfigException;
import com.silverpop.engage.exception.XMLAPIResponseException;
import com.silverpop.engage.recipient.CheckIdentityResult;
import com.silverpop.engage.recipient.CheckIdentityHandler;
import com.silverpop.engage.recipient.SetupRecipientHandler;
import com.silverpop.engage.recipient.SetupRecipientResult;
import com.silverpop.engage.response.*;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.UpdateRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;
import com.silverpop.engage.util.DateUtil;
import com.silverpop.engage.util.uuid.UUIDGenerator;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

import java.util.Date;
import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 *
 * Handles creation of recipients and auto-generates the mobile user id if needed.
 */
public class MobileConnectorManager extends BaseManager implements MobileConnector {

    private static final String TAG = MobileConnectorManager.class.getName();

    private static final String DEFAULT_UUID_GENERATOR_CLASS = "com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator";

    private static MobileConnectorManager instance = null;

    protected MobileConnectorManager(Context context) {
        super(context);
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

    @Override
    public void setupRecipient(final SetupRecipientHandler setupRecipientHandler) {

        try {
            String existingRecipientId = EngageConfig.recipientId(getContext());
            String existingMobileUserId = EngageConfig.mobileUserId(getContext());
            String listId;
            String mobileUserIdColumn;

            if (!TextUtils.isEmpty(existingRecipientId) && !TextUtils.isEmpty(existingMobileUserId)) {
                // recipient previously setup, no need to go any further
                setupRecipientHandler.onSuccess(new SetupRecipientResult(existingRecipientId));
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
            if (TextUtils.isEmpty(existingRecipientId)) {
                createNewRecipient(setupRecipientHandler, existingMobileUserId, listId, mobileUserIdColumn);
            }
            // we have existing existingRecipientId but not mobileUserId - this really shouldn't happen, but just in case
            else if (TextUtils.isEmpty(existingMobileUserId)) {
                updateExistingRecipientWithMobileUserId(setupRecipientHandler, existingRecipientId, listId, mobileUserIdColumn);
            }
        } catch (Exception e) {
            // just in case something unexpected happens
            Log.e(TAG, e.getMessage(), e);
            if (setupRecipientHandler != null) {
                setupRecipientHandler.onFailure(e);
            }
        }
    }

    protected void updateExistingRecipientWithMobileUserId(final SetupRecipientHandler setupRecipientHandler, String existingRecipientId, String listId, String mobileUserIdColumn) {
        // update the existing recipient with a mobile user id
        XMLAPI updateRecipientXml = XMLAPI.updateRecipient(existingRecipientId, listId);

        // generate new mobile user id
        String newMobileUserId = generateMobileUserId();
        EngageConfig.storeMobileUserId(getContext(), newMobileUserId);
        Log.d(TAG, "MobileUserId was auto generated");
        updateRecipientXml.addColumn(mobileUserIdColumn, newMobileUserId);

        getXMLAPIManager().postXMLAPI(updateRecipientXml, new UpdateRecipientResponseHandler() {
            @Override
            public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {
                if (setupRecipientHandler != null) {
                    setupRecipientHandler.onSuccess(new SetupRecipientResult(updateRecipientResponse.getRecipientId()));
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

    private void createNewRecipient(final SetupRecipientHandler setupRecipientHandler, String existingMobileUserId, String listId, String mobileUserIdColumn) {
        String newMobileUserId = existingMobileUserId;
        // generate mobile user id if needed
        if (TextUtils.isEmpty(newMobileUserId)) {
            newMobileUserId = generateMobileUserId();
            EngageConfig.storeMobileUserId(getContext(), newMobileUserId);
            Log.d(TAG, "MobileUserId was auto generated");
        }


        XMLAPI addRecipient = XMLAPI.addRecipient(mobileUserIdColumn, newMobileUserId, listId, false);
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
                        setupRecipientHandler.onSuccess(new SetupRecipientResult(recipientId));
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

    @Override
    public void checkIdentity(final Map<String, String> idFieldNamesToValues, final CheckIdentityHandler identityHandler) {

        setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                String currentRecipientId = result.getRecipientId();
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

    private void checkForExistingRecipientAndUpdateIfNeeded(final Map<String, String> idFieldNamesToValues,
                                                            final String currentRecipientId, final CheckIdentityHandler identityHandler) {

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
                Log.d(TAG, response.getXml());

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
                    final String existingMobileUserId = existingRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName());

                    // scenario 2 - existing recipient doesn't have a mobileUserId
                    if (TextUtils.isEmpty(existingMobileUserId)) {
                        handleExistingRecipientWithoutRecipientId(existingRecipientResponse, identityHandler, listId);
                    }
                    // scenario 3 - existing recipient has a mobileUserId
                    else {
                        handleExistingRecipientWithRecipientId(existingRecipientResponse, existingMobileUserId, currentRecipientId, listId, identityHandler);
                    }
                }
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
    private void handleExistingRecipientWithRecipientId(final SelectRecipientResponse existingRecipientResponse, final String existingMobileUserId, String currentRecipientId, String listId, final CheckIdentityHandler identityHandler) {
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
                if (getEngageConfigManager().mergeHistoryInAuditRecordTableDatabase()) {
                    updateAuditRecordWithMergeChanges(oldRecipientId, newRecipientId, identityHandler);
                } else {
                    identityHandler.onSuccess(new CheckIdentityResult(
                            EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext())));
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
     * @param identityHandler
     * @param listId
     */
    private void handleExistingRecipientWithoutRecipientId(final SelectRecipientResponse existingRecipientResponse,
                                                           final CheckIdentityHandler identityHandler, final String listId) {

        final String mobileUserIdFromApp = EngageConfig.mobileUserId(getContext());
        if (TextUtils.isEmpty(mobileUserIdFromApp)) {
            final String error = "Cannot find mobileUserId to update the existing applicant with for recipientId = " + existingRecipientResponse.getRecipientId();
            Log.e(TAG, error);
            // time to bail, can't go any further
            if (identityHandler != null) {
                identityHandler.onFailure(new EngageConfigException(error));
            }
        } else {

            final XMLAPI updateExistingRecipientXml = XMLAPI.builder()
                    .operation(XMLAPIOperation.UPDATE_RECIPIENT)
                    .listId(listId)
                    .recipientId(existingRecipientResponse.getRecipientId())
                    .column(getEngageConfigManager().mobileUserIdColumnName(), mobileUserIdFromApp)
                    .build();

            // update existing recipient on server with new mobile user id

            getXMLAPIManager().postXMLAPI(updateExistingRecipientXml, new UpdateRecipientResponseHandler() {
                @Override
                public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {

                    // clear mobile user id from recipient in app config, and set its merged_recipient_id_ and merged date

                    // for recipient currently in the app config
                    XMLAPI updateCurrentRecipient = XMLAPI.updateRecipient(
                            EngageConfig.recipientId(getContext()), listId);
                    updateCurrentRecipient.addColumn(getEngageConfigManager().mobileUserIdColumnName(), "");
                    if (getEngageConfigManager().mergeHistoryInMergedMarketingDatabase()) {
                        updateCurrentRecipient.addColumn(getEngageConfigManager().mergedDateColumnName(), DateUtil.toGmtString(new Date()));
                        updateCurrentRecipient.addColumn(getEngageConfigManager().mergedRecipientIdColumnName(), existingRecipientResponse.getRecipientId());
                    }

                    getXMLAPIManager().postXMLAPI(updateCurrentRecipient, new UpdateRecipientResponseHandler() {
                        @Override
                        public void onUpdateRecipientSuccess(UpdateRecipientResponse updateCurrentRecipientResponse) {
                            // start using existing recipient id instead
                            final String oldRecipientId = EngageConfig.recipientId(getContext());
                            final String newRecipientId = existingRecipientResponse.getRecipientId();
                            EngageConfig.storeRecipientId(getContext(), newRecipientId);

                            // both recipients have been updated, time to update audit table
                            if (getEngageConfigManager().mergeHistoryInAuditRecordTableDatabase()) {
                                updateAuditRecordWithMergeChanges(oldRecipientId, newRecipientId, identityHandler);
                            } else {
                                identityHandler.onSuccess(new CheckIdentityResult(
                                        EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext())));
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
    private void updateAuditRecordWithMergeChanges(String oldRecipientId, String newRecipientId, final CheckIdentityHandler identityHandler) {
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
                            identityHandler.onSuccess(new CheckIdentityResult(
                                    EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext())));
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
    private void updateRecipientWithCustomId(String recipientId, String listId, Map<String, String> idFieldNamesToValues, final CheckIdentityHandler identityHandler) {
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
                    identityHandler.onSuccess(new CheckIdentityResult(
                            updateRecipientResponse.getRecipientId(), EngageConfig.mobileUserId(getContext())));
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

    public String generateMobileUserId() {
        String uuidClassFullPackageName = getEngageConfigManager().mobileUserIdGeneratorClassName();

        UUIDGenerator uuidGenerator;
        try {
            Class uuidClassName = Class.forName(uuidClassFullPackageName);
            uuidGenerator = (UUIDGenerator) uuidClassName.newInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Unable to initialize UUID generator class '" + uuidClassFullPackageName +
                    ".' Using default implementation of " + DEFAULT_UUID_GENERATOR_CLASS + ": " + ex.getMessage());

            uuidGenerator = new DefaultUUIDGenerator();
        }

        String mobileUserId = uuidGenerator.generateUUID();
        return mobileUserId;
    }
}
