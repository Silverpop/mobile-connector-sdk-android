package com.silverpop.engage;

import android.util.Log;
import com.silverpop.BaseAndroidTest;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.recipient.*;
import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.SelectRecipientResponse;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.SelectRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseFailure;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that actually hit the Silverpop Xml Api web service.
 */
public class MobileIdentityManager_IT extends BaseAndroidTest {

    private List<XMLAPI> tearDownAPICalls = new ArrayList<XMLAPI>();

    private static final String TAG = MobileIdentityManager_IT.class.getName();

    // note: these columns are expected to be setup before the tests are run
    private static final String CUSTOM_ID_COLUMN = "Custom Integration Test Id";
    private static final String CUSTOM_ID_COLUMN_2 = "Custom Integration Test Id 2";

    private DefaultUUIDGenerator uuidGenerator = new DefaultUUIDGenerator();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clearSharedPreferences();
        initManagers();
        // wait a few seconds to allow silverpop auth
        TimeUnit.SECONDS.sleep(5);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (!tearDownAPICalls.isEmpty()) {
            for (XMLAPI tearDownAPICall : tearDownAPICalls) {
                getXMLAPIManager().postXMLAPI(tearDownAPICall, new XMLAPIResponseHandler() {
                    @Override
                    public void onSuccess(EngageResponseXML response) {
                        Log.d(TAG, "Clean up success");
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        Log.d(TAG, "Error running cleanup");
                    }
                });
            }
            // allow some time for cleanup
            TimeUnit.SECONDS.sleep(5);
            tearDownAPICalls.clear();
        }
    }

    public void testSetupRecipient_createNewRecipient() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        assertThat(EngageConfig.mobileUserId(getContext())).isEmpty();
        assertThat(EngageConfig.recipientId(getContext())).isEmpty();

        MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                String recipientId = result.getRecipientId();
                scheduleCleanup(recipientId);

                assertThat(recipientId).isNotNull();
                // verify recipient id and mobile user id are configured in app
                assertThat(EngageConfig.recipientId(getContext())).isNotEmpty();
                assertThat(EngageConfig.mobileUserId(getContext())).isNotEmpty();

                // release countdown
                signal.countDown();
            }

            @Override
            public void onFailure(SetupRecipientFailure error) {
                fail(error.getMessage());
            }

            protected void scheduleCleanup(String recipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getListId())
                        .recipientId(recipientId)
                        .column(mobileUserIdColumnName(), EngageConfig.mobileUserId(getContext()))
                        .build());
            }
        });

        // The testing thread will wait here until the UI thread releases it
        // above with the countDown() or 30 seconds passes and it times out.
        signal.await(30, TimeUnit.SECONDS);
    }

    public void testSetupRecipient_recipientPreviouslySetup() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String prevMobileUserId = uuidGenerator.generateUUID();
        final String prevRecipientId = "00000";

        // pretend these were previously set
        EngageConfig.storeMobileUserId(getContext(), prevMobileUserId);
        EngageConfig.storeRecipientId(getContext(), prevRecipientId);

        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(prevMobileUserId);
        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(prevRecipientId);

        // calling setup recipient again should return the values that were previously set instead of generating new ones
        MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                String recipientId = result.getRecipientId();
                assertThat(recipientId).isEqualTo(prevRecipientId);

                // engage config should remain unchanged
                assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(prevMobileUserId);
                assertThat(EngageConfig.recipientId(getContext())).isEqualTo(prevRecipientId);

                // release countdown
                signal.countDown();
            }

            @Override
            public void onFailure(SetupRecipientFailure error) {
                fail(error.getMessage());
            }
        });

        // The testing thread will waits here until the UI thread releases it or timer runs out
        signal.await(3, TimeUnit.SECONDS);
    }

    /**
     * This is not a normal case that should happen, but lets test that we can recover from this situation correctly.
     */
    public void testSetupRecipient_existingRecipientIdButNoMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        // setup - create a user without a mobile user id
        final String email = uuidGenerator.generateUUID() + "@makeandbuild.com";
        final String listId = getListId();
        XMLAPI setupExistingRecipientXml = XMLAPI.addRecipientWithEmail(email, listId);
        getXMLAPIManager().postXMLAPI(setupExistingRecipientXml, new AddRecipientResponseHandler() {
            @Override
            public void onAddRecipientSuccess(final AddRecipientResponse addRecipientResponse) {
                final String existingRecipientId = addRecipientResponse.getRecipientId();
                scheduleCleanup(existingRecipientId);
                EngageConfig.storeRecipientId(getContext(), existingRecipientId);
                assertThat(EngageConfig.mobileUserId(getContext())).isEmpty();
                // setup complete, we now have an existing recipient with an email and recipient id only, not a mobile user id

                // start actual test
                MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
                    @Override
                    public void onSuccess(SetupRecipientResult result) {
                        String recipientId = result.getRecipientId();
                        // recipient should have been updated with mobile user id
                        assertThat(recipientId).isEqualTo(existingRecipientId);
                        assertThat(EngageConfig.mobileUserId(getContext())).isNotEmpty();

                        // release countdown
                        signal.countDown();
                    }

                    @Override
                    public void onFailure(SetupRecipientFailure error) {
                        fail(error.getMessage());
                    }
                });
            }

            protected void scheduleCleanup(String recipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(listId)
                        .recipientId(recipientId)
                        .email(email)
                        .build());
            }

            @Override
            public void onFailure(XMLAPIResponseFailure failure) {
                fail("Failed to setup test recipient: " +
                        failure.getException() != null ? failure.getException().getMessage() : "Error adding recipient");
            }
        });

        signal.await(30, TimeUnit.SECONDS);
    }

    public void testCheckIdentity_s1_recipientNotFound() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        // setup recipient on server with recipientId and mobileUserId set
        MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdRecipientId = result.getRecipientId();
                scheduleCleanup(createdRecipientId);

                // recipient is setup, we should have recipientId and mobileUserId now
                assertThat(createdRecipientId).isNotEmpty();
                assertThat(EngageConfig.mobileUserId(getContext())).isNotEmpty();

                // look for an existing recipient with the following
                final String nonExistingCustomIdValue = uuidGenerator.generateUUID();

                Map<String, String> idFieldNamesToValues = new HashMap<String, String>();
                idFieldNamesToValues.put(CUSTOM_ID_COLUMN, nonExistingCustomIdValue);


                MobileIdentityManager.get().checkIdentity(idFieldNamesToValues, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {
                        final String recipientId = result.getRecipientId();
                        final String mergedRecipientId = result.getMergedRecipientId();
                        final String mobileUserId = result.getMobileUserId();

                        // check that existing recipient was updated with a generated mobile user id
                        assertThat(recipientId).isNotEmpty();
                        assertThat(mergedRecipientId).isNullOrEmpty();
                        assertThat(mobileUserId).isNotEmpty();

                        // double check that the server is updated correctly
                        XMLAPI selectRecipientXml = XMLAPI.builder()
                                .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                .recipientId(recipientId)
                                .listId(getListId())
                                .build();
                        getXMLAPIManager().postXMLAPI(selectRecipientXml, new SelectRecipientResponseHandler() {
                            @Override
                            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {
                                String foundMobileUserId = selectRecipientResponse.getColumnValue(mobileUserIdColumnName());
                                String foundCustomId = selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN);

                                // verify that mobileUserId and custom Id were actually saved to the server
                                assertThat(foundCustomId).isNotEmpty().isEqualTo(nonExistingCustomIdValue);
                                assertThat(foundMobileUserId).isNotEmpty().isEqualTo(mobileUserId);

                                signal.countDown();
                            }

                            @Override
                            public void onFailure(XMLAPIResponseFailure exception) {
                                fail();
                            }
                        });
                    }

                    @Override
                    public void onFailure(CheckIdentityFailure e) {
                        fail(e.getMessage());
                    }
                });
            }

            protected void scheduleCleanup(String createdRecipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getListId())
                        .recipientId(createdRecipientId)
                        .build());
            }

            @Override
            public void onFailure(SetupRecipientFailure failure) {
                fail(failure.getMessage());
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    /**
     * Assumes database list has an existing column for the custom id, mobile user id, merged recipient id, and merged date
     */
    public void testCheckIdentity_s2_existingRecipientWithoutMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String RECIPIENT_LIST_ID = getListId();

        setupScenario2(new ScenarioSetupHandler() {
            @Override
            public void onSuccess(final Recipient currentRecipient, final Recipient existingRecipient) {

                // look for an existing recipient with customId
                final String customId = existingRecipient.customIdFields.get(CUSTOM_ID_COLUMN);

                MobileIdentityManager.get().checkIdentity(existingRecipient.customIdFields, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {

                        // verify correct values passed in result
                        assertThat(result.getRecipientId()).isEqualTo(EngageConfig.recipientId(getContext()));
                        assertThat(result.getMergedRecipientId()).isNotEmpty().isNotEqualTo(result.getRecipientId());
                        assertThat(result.getMobileUserId()).isEqualTo(EngageConfig.mobileUserId(getContext()));

                        // verify the app is now using the existing recipient
                        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(existingRecipient.recipientId);
                        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(currentRecipient.mobileUserId);

                        // check state of recipients on server
                        // check first recipient
                        getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                        .listId(RECIPIENT_LIST_ID).recipientId(currentRecipient.recipientId).build(),
                                new SelectRecipientResponseHandler() {
                                    @Override
                                    public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                        // mobile id cleared
                                        assertThat(selectRecipientResponse.getColumnValue(mobileUserIdColumnName())).isNullOrEmpty();
                                        // merged properties set
                                        assertThat(selectRecipientResponse.getColumnValue(mergedRecipientIdColumnName())).isEqualTo(existingRecipient.recipientId);
                                        assertThat(selectRecipientResponse.getColumnValue(mergedDateColumnName())).isNotEmpty();

                                        // check second recipient
                                        getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                                        .listId(RECIPIENT_LIST_ID).recipientId(existingRecipient.recipientId).build(),
                                                new SelectRecipientResponseHandler() {
                                                    @Override
                                                    public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                                        // mobile id set to merged recipient id
                                                        assertThat(selectRecipientResponse.getColumnValue(mobileUserIdColumnName())).isEqualTo(currentRecipient.mobileUserId);
                                                        assertThat(selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN)).isEqualTo(customId);
                                                        assertThat(selectRecipientResponse.getColumnValue(mergedRecipientIdColumnName())).isNullOrEmpty();
                                                        assertThat(selectRecipientResponse.getColumnValue(mergedDateColumnName())).isNullOrEmpty();

                                                        signal.countDown();
                                                    }

                                                    @Override
                                                    public void onFailure(XMLAPIResponseFailure failure) {
                                                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error selecting recipient");
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(XMLAPIResponseFailure failure) {
                                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error selecting recipient");
                                    }
                                });
                    }

                    @Override
                    public void onFailure(CheckIdentityFailure e) {
                        fail(e.getMessage());
                    }
                });
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    public void setupScenario2(final ScenarioSetupHandler setupHandler) throws Exception {

        final String RECIPIENT_LIST_ID = getListId();

        // setup recipient on server with recipientId and mobileUserId set
        MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdWithMobileUserId_RecipientId = result.getRecipientId();
                scheduleCleanup(createdWithMobileUserId_RecipientId);
                final String originalMobileUserId = EngageConfig.mobileUserId(getContext());

                final String customId = uuidGenerator.generateUUID();

                // setup existing recipient on server with custom id but not a mobile user id
                XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(RECIPIENT_LIST_ID)
                        .column(CUSTOM_ID_COLUMN, customId)
                        .build();
                getXMLAPIManager().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientWithCustomIdResponse) {
                        final String createdWithCustomId_RecipientId = addRecipientWithCustomIdResponse.getRecipientId();
                        scheduleCleanup(createdWithCustomId_RecipientId);

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |
                        //    value    |              |  value

                        Recipient currentRecipient = new Recipient();
                        currentRecipient.recipientId = createdWithMobileUserId_RecipientId;
                        currentRecipient.mobileUserId = originalMobileUserId;

                        Recipient existingRecipient = new Recipient();
                        existingRecipient.recipientId = createdWithCustomId_RecipientId;
                        existingRecipient.customIdFields.put(CUSTOM_ID_COLUMN, customId);

                        setupHandler.onSuccess(currentRecipient, existingRecipient);
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error adding recipient");
                    }
                });
            }

            @Override
            public void onFailure(SetupRecipientFailure failure) {
                fail(failure.getException() != null ? failure.getException().getMessage() : "Error setting up recipient");
            }

            protected void scheduleCleanup(String createdRecipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getListId())
                        .recipientId(createdRecipientId)
                        .build());
            }
        });

    }

    public void testCheckIdentity_s3_existingRecipientWithMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String RECIPIENT_LIST_ID = getListId();

//        EngageConfig.storeAuditRecordTableId(getContext(), "29392");

        setupScenario3(false, new ScenarioSetupHandler() {
            @Override
            public void onSuccess(final Recipient currentRecipient, final Recipient existingRecipient) {

                // look for an existing recipient with customId
                MobileIdentityManager.get().checkIdentity(existingRecipient.customIdFields, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {

                        // verify correct values passed in result
                        assertThat(result.getRecipientId()).isEqualTo(EngageConfig.recipientId(getContext()));
                        assertThat(result.getMergedRecipientId()).isNotEmpty().isNotEqualTo(result.getRecipientId());
                        assertThat(result.getMobileUserId()).isEqualTo(EngageConfig.mobileUserId(getContext()));

                        // verify the app is now using the existing recipient
                        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(existingRecipient.mobileUserId);
                        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(existingRecipient.recipientId);

                        // verify old recipient is marked as merged on the server
                        getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                        .listId(RECIPIENT_LIST_ID).recipientId(currentRecipient.recipientId).build(),
                                new SelectRecipientResponseHandler() {
                                    @Override
                                    public void onSelectRecipientSuccess(SelectRecipientResponse selectMergedRecipientResponse) {

                                        // check that properties didn't change
                                        assertThat(selectMergedRecipientResponse.getColumnValue(mobileUserIdColumnName())).isEqualTo(currentRecipient.mobileUserId);
                                        assertThat(selectMergedRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN)).isNullOrEmpty();

                                        // check marked as merged
                                        assertThat(selectMergedRecipientResponse.getColumnValue(mergedRecipientIdColumnName())).isEqualTo(existingRecipient.recipientId);
                                        assertThat(selectMergedRecipientResponse.getColumnValue(mergedDateColumnName())).isNotEmpty();

                                        signal.countDown();
                                    }

                                    @Override
                                    public void onFailure(XMLAPIResponseFailure failure) {
                                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error selecting recipient");
                                    }
                                });
                    }

                    @Override
                    public void onFailure(CheckIdentityFailure e) {
                        fail(e.getMessage());
                    }
                });
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }



    public void testCheckIdentity_s3_existingRecipientWithMobileUserId_multipleCustomIds() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String RECIPIENT_LIST_ID = getListId();

        setupScenario3(true, new ScenarioSetupHandler() {
            @Override
            public void onSuccess(final Recipient currentRecipient, final Recipient existingRecipient) {
                String customId = existingRecipient.customIdFields.get(CUSTOM_ID_COLUMN);
                String customId2 = existingRecipient.customIdFields.get(CUSTOM_ID_COLUMN_2);

                // look for an existing recipient with both custom ids

                MobileIdentityManager.get().checkIdentity(existingRecipient.customIdFields, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {

                        // verify correct values passed in result
                        assertThat(result.getRecipientId()).isEqualTo(EngageConfig.recipientId(getContext()));
                        assertThat(result.getMergedRecipientId()).isNotEmpty().isNotEqualTo(result.getRecipientId());
                        assertThat(result.getMobileUserId()).isEqualTo(EngageConfig.mobileUserId(getContext()));

                        // verify the app is now using the existing recipient
                        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(existingRecipient.mobileUserId);
                        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(existingRecipient.recipientId);

                        // verify old recipient is marked as merged on the server
                        getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                        .listId(RECIPIENT_LIST_ID).recipientId(currentRecipient.recipientId).build(),
                                new SelectRecipientResponseHandler() {
                                    @Override
                                    public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                        // check that properties didn't change
                                        assertThat(selectRecipientResponse.getColumnValue(mobileUserIdColumnName())).isEqualTo(currentRecipient.mobileUserId);
                                        assertThat(selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN)).isNullOrEmpty();
                                        assertThat(selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN_2)).isNullOrEmpty();

                                        // check marked as merged
                                        assertThat(selectRecipientResponse.getColumnValue(mergedRecipientIdColumnName())).isEqualTo(existingRecipient.recipientId);
                                        assertThat(selectRecipientResponse.getColumnValue(mergedDateColumnName())).isNotEmpty();

                                        signal.countDown();
                                    }

                                    @Override
                                    public void onFailure(XMLAPIResponseFailure failure) {
                                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error adding recipient");
                                    }
                                });
                    }

                    @Override
                    public void onFailure(CheckIdentityFailure e) {
                        fail(e.getMessage());
                    }
                });
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    private void setupScenario3(final boolean twoCustomIds, final ScenarioSetupHandler setupHandler) throws Exception {

        final String RECIPIENT_LIST_ID = getListId();

        // setup recipient on server with recipientId and mobileUserId set
        MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdWithMobileUserId_RecipientId = result.getRecipientId();
                scheduleCleanup(createdWithMobileUserId_RecipientId);
                final String originalCurrentMobileUserId = EngageConfig.mobileUserId(getContext());

                final String customId = uuidGenerator.generateUUID();
                final String originalExistingMobileUserId = uuidGenerator.generateUUID();
                final String customId2 = uuidGenerator.generateUUID();

                // setup existing recipient on server with custom id(s) and a different mobileUserId
                XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(RECIPIENT_LIST_ID)
                        .column(CUSTOM_ID_COLUMN, customId)
                        .column(mobileUserIdColumnName(), originalExistingMobileUserId)
                        .build();
                if (twoCustomIds) {
                    addRecipientWithCustomIdXml.addColumn(CUSTOM_ID_COLUMN_2, customId2);
                }

                getXMLAPIManager().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        final String createdWithCustomId_RecipientId = addRecipientResponse.getRecipientId();
                        scheduleCleanup(createdWithCustomId_RecipientId);

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |
                        //    value    |     value    |  value

                        Recipient currentRecipient = new Recipient();
                        currentRecipient.recipientId = createdWithMobileUserId_RecipientId;
                        currentRecipient.mobileUserId = originalCurrentMobileUserId;

                        Recipient existingRecipient = new Recipient();
                        existingRecipient.recipientId = createdWithCustomId_RecipientId;
                        existingRecipient.mobileUserId = originalExistingMobileUserId;
                        existingRecipient.customIdFields.put(CUSTOM_ID_COLUMN, customId);
                        if (twoCustomIds) {
                            existingRecipient.customIdFields.put(CUSTOM_ID_COLUMN_2, customId2);
                        }

                        setupHandler.onSuccess(currentRecipient, existingRecipient);
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        fail(failure.getException() != null ? failure.getException().getMessage() : "Error adding recipient");
                    }
                });
            }

            @Override
            public void onFailure(SetupRecipientFailure error) {
                fail(error.getMessage());
            }

            protected void scheduleCleanup(String recipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getListId())
                        .recipientId(recipientId)
                        .build());
            }
        });

    }

    public void testCheckIdentity_selfFoundAsExistingRecipient_WithMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        // setup
        final String mobileUseId = uuidGenerator.generateUUID();
        final String customId = uuidGenerator.generateUUID();
        getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(getListId())
                        .column(getEngageConfigManager().mobileUserIdColumnName(), mobileUseId)
                        .column(CUSTOM_ID_COLUMN, customId).build(),
                new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(final AddRecipientResponse addRecipientResponse) {
                        EngageConfig.storeRecipientId(getContext(), addRecipientResponse.getRecipientId());
                        EngageConfig.storeMobileUserId(getContext(), mobileUseId);

                        // schedule cleanup
                        tearDownAPICalls.add(XMLAPI.builder()
                                .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                                .recipientId(addRecipientResponse.getRecipientId())
                                .listId(getListId())
                                .build());

                        // recipient setup with mobile user id and custom id, lets search for that user
                        Map<String, String> customIdMap = new HashMap<String, String>();
                        customIdMap.put(CUSTOM_ID_COLUMN, customId);
                        MobileIdentityManager.get().checkIdentity(customIdMap, new CheckIdentityHandler() {
                            @Override
                            public void onSuccess(CheckIdentityResult result) {
                                // nothing should have happened, no merging and EngageConfig should remain the same
                                assertThat(result.getMobileUserId()).isEqualTo(mobileUseId).isEqualTo(EngageConfig.mobileUserId(getContext()));
                                assertThat(result.getMergedRecipientId()).isNullOrEmpty();
                                assertThat(result.getRecipientId()).isNotEmpty()
                                        .isEqualTo(addRecipientResponse.getRecipientId()).isEqualTo(EngageConfig.recipientId(getContext()));

                                signal.countDown();
                            }

                            @Override
                            public void onFailure(CheckIdentityFailure failure) {
                                String error = failure.getException() != null ? failure.getException().getMessage() : "Check identity failure";
                                Log.e(TAG, error, failure.getException());
                                fail(error);
                            }
                        });
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        String error = failure.getException() != null ? failure.getException().getMessage() : "Add recipient failure";
                        Log.e(TAG, error, failure.getException());
                        fail(error);
                    }
                });
        signal.await(10, TimeUnit.SECONDS);
    }

    class Recipient {
        String recipientId;
        String mobileUserId;
        Map<String, String> customIdFields = new HashMap<String, String>();
    }

    interface ScenarioSetupHandler {
        void onSuccess(Recipient currentRecipient, Recipient existingRecipient);
    }

}