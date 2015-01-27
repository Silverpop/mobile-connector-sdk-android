package com.silverpop.engage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.RelationalTableRow;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.exception.EngageConfigException;
import com.silverpop.engage.recipient.*;
import com.silverpop.engage.response.*;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.UpdateRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseFailure;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;
import com.silverpop.engage.util.DateUtil;
import com.silverpop.engage.util.uuid.UUIDGenerator;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

import java.util.Date;
import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 * <p/>
 * Handles the creation and merging of recipients and auto-generates the mobile user id if needed.
 */
public class MobileIdentityManager extends BaseManager {

    private static final String TAG = MobileIdentityManager.class.getName();

    private static MobileIdentityManager instance = null;

    protected MobileIdentityManager(Context context) {
        super(context);
    }

    public static synchronized MobileIdentityManager init(Context context) {
        if (instance == null) {
            instance = new MobileIdentityManager(context);
        }
        return instance;
    }

    public static MobileIdentityManager get() {
        if (instance == null) {
            final String error = MobileIdentityManager.class.getName() + " must be initialized before it can be retrieved";
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
        return instance;
    }

    /**
     * Checks if the mobile user id has been configured yet.  If not
     * and the {@code enableAutoAnonymousTracking} flag is set to true it is auto generated
     * using either the {@link com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator} or
     * the generator configured as the {@code mobileUserIdGeneratorClassName}.  If
     * {@code enableAutoAnonymousTracking} is {@code false} you are responsible for
     * manually setting the id using {@link com.silverpop.engage.config.EngageConfig#storeMobileUserId(android.content.Context, String)}.
     * <p/>
     * Once we have a mobile user id (generated or manually set) a new recipient is
     * created with the mobile user id.
     * <p/>
     * On successful completion of this method the EngageConfig will contain the
     * mobile user id and new recipient id.
     *
     * @param setupRecipientHandler custom behavior to run on success and failure of this method
     */
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
                if (setupRecipientHandler != null) {
                    setupRecipientHandler.onFailure(new SetupRecipientFailure(e));
                }
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
                setupRecipientHandler.onFailure(new SetupRecipientFailure(e));
            }
        }
    }

    private void updateExistingRecipientWithMobileUserId(final SetupRecipientHandler setupRecipientHandler,
                                                         String existingRecipientId, String listId, String mobileUserIdColumn) {
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
            public void onFailure(XMLAPIResponseFailure failure) {
                if (setupRecipientHandler != null) {
                    setupRecipientHandler.onFailure(new SetupRecipientFailure(failure.getException(), failure.getResponseXml()));
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


        XMLAPI addRecipientXml = XMLAPI.addRecipient(mobileUserIdColumn, newMobileUserId, listId, false);
        getXMLAPIManager().postXMLAPI(addRecipientXml, new AddRecipientResponseHandler() {
            @Override
            public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                String recipientId = addRecipientResponse.getRecipientId();

                if (TextUtils.isEmpty(recipientId)) {
                    if (setupRecipientHandler != null) {
                        setupRecipientHandler.onFailure(
                                new SetupRecipientFailure("Empty recipientId returned from Silverpop", addRecipientResponse.getResponseXml()));
                    }
                } else {
                    EngageConfig.storeRecipientId(getContext(), recipientId);

                    if (setupRecipientHandler != null) {
                        setupRecipientHandler.onSuccess(new SetupRecipientResult(recipientId));
                    }
                }
            }

            @Override
            public void onFailure(XMLAPIResponseFailure failure) {
                if (setupRecipientHandler != null) {
                    setupRecipientHandler.onFailure(new SetupRecipientFailure(failure.getException(), failure.getResponseXml()));
                }
            }
        });
    }

    /**
     * Checks for an existing recipient with all the specified ids.  If a matching recipient doesn't exist
     * the currently configured recipient is updated with the searched ids.  If an existing recipient
     * does exist the two recipients are merged and the engage app config is switched to the existing
     * recipient.
     * <p/>
     * When recipients are merged a history of the merged recipients is recorded using the
     * Mobile User Id, Merged Recipient Id, and Merged Date columns.
     * //[Lindsay Thurmond:1/15/15] TODO: update with audit table when applicable
     * <p/>
     * WARNING: The merge process is not currently transactional.  If this method errors the data is likely to
     * be left in an inconsistent state.
     *
     * @param idFieldNamesToValues Map of column name to id value for that column.  Searches for an
     *                             existing recipient that contains ALL of the column values in this map.
     *                             <p/>
     *                             Examples:
     *                             - Key: facebook_id, Value: 100
     *                             - Key: twitter_id, Value: 9999
     * @param identityHandler      custom behavior to run on success and failure of this method
     */
    public void checkIdentity(final Map<String, String> idFieldNamesToValues, final CheckIdentityHandler identityHandler) {

        setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                String currentRecipientId = result.getRecipientId();
                checkForExistingRecipientAndUpdateIfNeeded(idFieldNamesToValues, currentRecipientId, identityHandler);
            }

            @Override
            public void onFailure(SetupRecipientFailure failure) {
                if (identityHandler != null) {
                    Log.e(TAG, failure.getMessage(), failure.getException());
                    identityHandler.onFailure(new CheckIdentityFailure(
                            failure.getMessage(), failure.getException(), failure.getResponseXml()));
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
        selectRecipientData.addColumns((Map<String, Object>)((Object)idFieldNamesToValues));

        getXMLAPIManager().postXMLAPI(selectRecipientData, new XMLAPIResponseHandler() {
            @Override
            public void onSuccess(EngageResponseXML response) {
                final SelectRecipientResponse existingRecipientResponse = new SelectRecipientResponse(response);
                Log.d(TAG, response.getXml());

                // scenario 1 - recipient not found
                if (!existingRecipientResponse.isSuccess()) {

                    // user not found or error with actual request?
                    if (existingRecipientResponse.getErrorCode() == XMLAPIErrorCode.RECIPIENT_NOT_LIST_MEMBER) {
                        // recipient doesn't exist
                        updateRecipientWithCustomId(currentRecipientId, listId, idFieldNamesToValues, identityHandler);
                    } else {
                        // an error happened with the request/response
                        if (identityHandler != null) {
                            // request failed for unknown reason, time to bail
                            identityHandler.onFailure(new CheckIdentityFailure(response));
                        }
                    }
                }
                // we found an existing recipient - does it have a mobileUserId?
                else {

                    final String existingRecipientId = existingRecipientResponse.getRecipientId();
                    final String existingMobileUserId = existingRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName());

                    // make sure we didn't find our self
                    if (existingRecipientId.equals(EngageConfig.recipientId(getContext()))) {
                        handleExistingRecipientIsSameAsInApp(existingMobileUserId, identityHandler);

                    } else {
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
            }

            @Override
            public void onFailure(XMLAPIResponseFailure failure) {
                if (failure.getException() != null) {
                    Log.d(TAG, failure.getException().getMessage(), failure.getException());
                } else if (failure.getResponseXml() != null) {
                    Log.d(TAG, failure.getResponseXml().getFaultString());
                } else {
                    Log.d(TAG, "Unexpected XMLAPI exception selecting recipient");
                }
            }
        });
    }

    protected void handleExistingRecipientIsSameAsInApp(final String existingMobileUserId, final CheckIdentityHandler identityHandler) {
        // we did find our self
        if (TextUtils.isEmpty(existingMobileUserId)) {
            // It really shouldn't be possible to get here since the first thing CheckIdentity does
            // is call setupRecipient which would fill in the mobile user id for the recipient if
            // it was missing before we got here - but just in case let's handle it here again

            // update with mobile user id
            final String existingRecipientId = EngageConfig.recipientId(getContext());
            XMLAPI updateExistingRecipientXml = XMLAPI.builder().operation(XMLAPIOperation.UPDATE_RECIPIENT)
                    .listId(getEngageConfigManager().engageListId())
                    .recipientId(existingRecipientId)
                    .column(getEngageConfigManager().mobileUserIdColumnName(), EngageConfig.mobileUserId(getContext()))
                    .build();
            getXMLAPIManager().postXMLAPI(updateExistingRecipientXml, new UpdateRecipientResponseHandler() {
                @Override
                public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {
                    if (identityHandler != null) {
                        identityHandler.onSuccess(new CheckIdentityResult(existingRecipientId, EngageConfig.mobileUserId(getContext())));
                    }
                }

                @Override
                public void onFailure(XMLAPIResponseFailure failure) {
                    if (failure.getException() != null) {
                        Log.d(TAG, failure.getException().getMessage(), failure.getException());
                    } else if (failure.getResponseXml() != null) {
                        Log.d(TAG, failure.getResponseXml().getFaultString());
                    } else {
                        Log.d(TAG, "Unexpected XMLAPI exception updating recipient");
                    }
                    if (identityHandler != null) {
                        identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
                    }
                }
            });

        } else {
            // nothing to do here, we're done
            if (identityHandler != null) {
                identityHandler.onSuccess(new CheckIdentityResult(
                        EngageConfig.recipientId(getContext()), EngageConfig.mobileUserId(getContext())));
            }
        }
    }

    /**
     * Scenario 3 - existing recipient has a mobileUserId
     */
    private void handleExistingRecipientWithRecipientId(final SelectRecipientResponse existingRecipientResponse, final String existingMobileUserId,
                                                        String currentRecipientId, String listId, final CheckIdentityHandler identityHandler) {
        // mark current recipient as merged
        XMLAPI updateCurrentRecipientXml = XMLAPI.updateRecipient(currentRecipientId, listId);
        if (getEngageConfigManager().mergeHistoryInMarketingDatabase()) {
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
                            newRecipientId, oldRecipientId, existingMobileUserId));
                }
            }

            @Override
            public void onFailure(XMLAPIResponseFailure failure) {
                if (identityHandler != null) {
                    identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
                }
            }
        });
    }

    /**
     * Scenario 2 - existing recipient doesn't have a mobileUserId
     */
    private void handleExistingRecipientWithoutRecipientId(final SelectRecipientResponse existingRecipientResponse,
                                                           final CheckIdentityHandler identityHandler, final String listId) {

        final String mobileUserIdFromApp = EngageConfig.mobileUserId(getContext());
        if (TextUtils.isEmpty(mobileUserIdFromApp)) {
            final String error = "Cannot find mobileUserId to update the existing applicant with for recipientId = " + existingRecipientResponse.getRecipientId();
            Log.e(TAG, error);
            // time to bail, can't go any further
            if (identityHandler != null) {
                identityHandler.onFailure(new CheckIdentityFailure(error));
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
                    if (getEngageConfigManager().mergeHistoryInMarketingDatabase()) {
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
                                        newRecipientId, oldRecipientId, EngageConfig.mobileUserId(getContext())));
                            }
                        }

                        @Override
                        public void onFailure(XMLAPIResponseFailure failure) {
                            // failed to update current recipient, can't continue
                            if (identityHandler != null) {
                                identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
                            }
                        }
                    });
                }

                @Override
                public void onFailure(XMLAPIResponseFailure failure) {
                    // first recipient update failed, can't go any further
                    if (identityHandler != null) {
                        identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
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
    private void updateAuditRecordWithMergeChanges(final String oldRecipientId, final String newRecipientId, final CheckIdentityHandler identityHandler) {
        final String auditRecordTableId = EngageConfig.auditRecordTableId(getContext());
        if (TextUtils.isEmpty(auditRecordTableId)) {
            if (identityHandler != null) {
                identityHandler.onFailure(new CheckIdentityFailure("Cannot update audit record without audit table id"));
            }
        } else {
            XMLAPI updateAuditRecordApi = XMLAPI.insertUpdateRelationalTable(auditRecordTableId);
            RelationalTableRow newAuditRecordRow = new RelationalTableRow();
            newAuditRecordRow.addColumn(getEngageConfigManager().auditRecordPrimaryKeyColumnName(), generateAuditRecordPrimaryKey());
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
                                   newRecipientId, oldRecipientId, EngageConfig.mobileUserId(getContext())));
                        }
                    } else {
                        if (identityHandler != null) {
                            identityHandler.onFailure(new CheckIdentityFailure(response));
                        }
                    }
                }

                @Override
                public void onFailure(XMLAPIResponseFailure failure) {
                    if (identityHandler != null) {
                        identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
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

        getXMLAPIManager().postXMLAPI(updateCurrentRecipientXml, new UpdateRecipientResponseHandler() {
            @Override
            public void onUpdateRecipientSuccess(UpdateRecipientResponse updateRecipientResponse) {
                if (identityHandler != null) {
                    identityHandler.onSuccess(new CheckIdentityResult(
                            updateRecipientResponse.getRecipientId(), EngageConfig.mobileUserId(getContext())));
                }
            }

            @Override
            public void onFailure(XMLAPIResponseFailure failure) {
                if (identityHandler != null) {
                    identityHandler.onFailure(new CheckIdentityFailure(failure.getException(), failure.getResponseXml()));
                }
            }
        });
    }

    /**
     * Generates a mobile user id using the class configured as the {@code mobileUserIdGeneratorClassName}
     * in the EngageConfig or the {@link com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator} if a
     * valid class isn't configured.
     *
     * @return a new unique id
     */
    public String generateMobileUserId() {
        String uuidClassFullPackageName = getEngageConfigManager().mobileUserIdGeneratorClassName();

        UUIDGenerator uuidGenerator;
        try {
            Class uuidClassName = Class.forName(uuidClassFullPackageName);
            uuidGenerator = (UUIDGenerator) uuidClassName.newInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Unable to initialize UUID generator class '" + uuidClassFullPackageName +
                    ".' Using default implementation of " + DefaultUUIDGenerator.class.getName() + ": " + ex.getMessage());

            uuidGenerator = new DefaultUUIDGenerator();
        }

        String mobileUserId = uuidGenerator.generateUUID();
        return mobileUserId;
    }

    /**
     * Generates a unique id using the class configured as the {@code auditRecordPrimaryKeyGeneratorClassName}
     * in the EngageConfig or the {@link com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator} if a
     * valid class isn't configured.
     *
     * @return a new unique id
     */
    public String generateAuditRecordPrimaryKey() {
        String uuidClassFullPackageName = getEngageConfigManager().auditRecordPrimaryKeyGeneratorClassName();

        UUIDGenerator uuidGenerator;
        try {
            Class uuidClassName = Class.forName(uuidClassFullPackageName);
            uuidGenerator = (UUIDGenerator) uuidClassName.newInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Unable to initialize UUID generator class '" + uuidClassFullPackageName +
                    ".' Using default implementation of " + DefaultUUIDGenerator.class.getName() + ": " + ex.getMessage());

            uuidGenerator = new DefaultUUIDGenerator();
        }

        String mobileUserId = uuidGenerator.generateUUID();
        return mobileUserId;
    }
}
