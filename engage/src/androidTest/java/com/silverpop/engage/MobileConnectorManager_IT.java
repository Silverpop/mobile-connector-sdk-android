package com.silverpop.engage;

import android.util.Log;
import com.silverpop.BaseAndroidTest;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.recipient.CheckIdentityResult;
import com.silverpop.engage.recipient.CheckIdentityHandler;
import com.silverpop.engage.recipient.SetupRecipientHandler;
import com.silverpop.engage.recipient.SetupRecipientResult;
import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.SelectRecipientResponse;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.SelectRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that actually hit the Silverpop Xml Api web service.
 */
public class MobileConnectorManager_IT extends BaseAndroidTest {

    private List<XMLAPI> tearDownAPICalls = new ArrayList<XMLAPI>();

    private static final String TAG = MobileConnectorManager_IT.class.getName();

    private static final String CUSTOM_ID_COLUMN = "Custom Integration Test Id";

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
                    public void onFailure(Exception exception) {
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

        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                String recipientId = result.getRecipientId();
                scheduleCleanup(recipientId);

                assertThat(recipientId).isNotNull();
                // release countdown
                signal.countDown();
            }

            @Override
            public void onFailure(Exception error) {
                fail(error.getMessage());
            }

            protected void scheduleCleanup(String recipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getEngageConfigManager().engageListId())
                        .recipientId(recipientId)
                        .column(getEngageConfigManager().mobileUserIdColumnName(), EngageConfig.mobileUserId(getContext()))
                        .build());
            }
        });

        // The testing thread will wait here until the UI thread releases it
        // above with the countDown() or 30 seconds passes and it times out.
        signal.await(30, TimeUnit.SECONDS);
    }

    public void testSetupRecipient_recipientPreviouslySetup() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String prevMobileUserId = UUID.randomUUID().toString();
        final String prevRecipientId = "00000";

        // pretend these were previously set
        EngageConfig.storeMobileUserId(getContext(), prevMobileUserId);
        EngageConfig.storeRecipientId(getContext(), prevRecipientId);

        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(prevMobileUserId);
        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(prevRecipientId);

        // calling setup recipient again should return the values that were previously set instead of generating new ones
        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
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
            public void onFailure(Exception error) {
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
        final String email = UUID.randomUUID().toString() + "@makeandbuild.com";
        final String listId = getEngageConfigManager().engageListId();
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
                MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
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
                    public void onFailure(Exception error) {
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
            public void onFailure(Exception exception) {
                fail("Failed to setup test recipient: " + exception.getMessage());
            }
        });

        signal.await(30, TimeUnit.SECONDS);
    }

    public void testCheckIdentity_s1_recipientNotFound() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        // setup recipient on server with recipientId and mobileUserId set
        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdRecipientId = result.getRecipientId();
                scheduleCleanup(createdRecipientId);

                // recipient is setup, we should have recipientId and mobileUserId now
                assertThat(createdRecipientId).isNotEmpty();
                assertThat(EngageConfig.mobileUserId(getContext())).isNotEmpty();

                // look for an existing recipient with the following
                final String nonExistingCustomIdValue = UUID.randomUUID().toString();

                Map<String, String> idFieldNamesToValues = new HashMap<String, String>();
                idFieldNamesToValues.put(CUSTOM_ID_COLUMN, nonExistingCustomIdValue);


                MobileConnectorManager.get().checkIdentity(idFieldNamesToValues, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {
                        final String recipientId = result.getRecipientId();
                        final String mobileUserId = result.getMobileUserId();

                        // check that existing recipient was updated with a generated mobile user id
                        assertThat(recipientId).isNotEmpty();
                        assertThat(mobileUserId).isNotEmpty();

                        // double check that the server is updated correctly
                        XMLAPI selectRecipientXml = XMLAPI.builder()
                                .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                .recipientId(recipientId)
                                .listId(getEngageConfigManager().engageListId())
                                .build();
                        getXMLAPIManager().postXMLAPI(selectRecipientXml, new SelectRecipientResponseHandler() {
                            @Override
                            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {
                                String foundMobileUserId = selectRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName());
                                String foundCustomId = selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN);

                                // verify that mobileUserId and custom Id were actually saved to the server
                                assertThat(foundCustomId).isNotEmpty().isEqualTo(nonExistingCustomIdValue);
                                assertThat(foundMobileUserId).isNotEmpty().isEqualTo(mobileUserId);

                                signal.countDown();
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                fail(exception.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail(e.getMessage());
                    }
                });
            }

            protected void scheduleCleanup(String createdRecipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getEngageConfigManager().engageListId())
                        .recipientId(createdRecipientId)
                        .build());
            }

            @Override
            public void onFailure(Exception error) {
                fail(error.getMessage());
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    /**
     * Assumes database list has an existing column for the custom id, mobile user id, merged recipient id, and merged date
     */
    public void testCheckIdentity_s2_existingRecipientWithoutMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String RECIPIENT_LIST_ID = getEngageConfigManager().engageListId();

        // setup recipient on server with recipientId and mobileUserId set
        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdWithMobileUserId_RecipientId = result.getRecipientId();
                scheduleCleanup(createdWithMobileUserId_RecipientId);
                final String originalMobileUserId = EngageConfig.mobileUserId(getContext());

                final String customId = UUID.randomUUID().toString();

                // setup existing recipient on server with custom id but not a mobile user id
                XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(RECIPIENT_LIST_ID)
                        .column(CUSTOM_ID_COLUMN, customId)
                        .build();
                getXMLAPIManager().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        final String createdWithCustomId_RecipientId = addRecipientResponse.getRecipientId();
                        scheduleCleanup(createdWithCustomId_RecipientId);

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |
                        //    value    |              |  value

                        // look for an existing recipient with customId

                        Map<String, String> idFieldNamesToValues = new HashMap<String, String>();
                        idFieldNamesToValues.put(CUSTOM_ID_COLUMN, customId);
                        MobileConnectorManager.get().checkIdentity(idFieldNamesToValues, new CheckIdentityHandler() {
                            @Override
                            public void onSuccess(CheckIdentityResult result) {

                                String recipientId = result.getRecipientId();
                                String mobileUserId = result.getMobileUserId();

                                // check state of recipients on server
                                // check first recipient
                                getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                                .listId(RECIPIENT_LIST_ID).recipientId(createdWithMobileUserId_RecipientId).build(),
                                        new SelectRecipientResponseHandler() {
                                            @Override
                                            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                                // mobile id cleared
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName())).isNullOrEmpty();
                                                // merged properties set
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedRecipientIdColumnName())).isEqualTo(createdWithCustomId_RecipientId);
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedDateColumnName())).isNotEmpty();

                                                // check second recipient
                                                getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                                                .listId(RECIPIENT_LIST_ID).recipientId(createdWithCustomId_RecipientId).build(),
                                                        new SelectRecipientResponseHandler() {
                                                            @Override
                                                            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                                                // mobile id set to merged recipient id
                                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName())).isEqualTo(originalMobileUserId);
                                                                assertThat(selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN)).isEqualTo(customId);
                                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedRecipientIdColumnName())).isNullOrEmpty();
                                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedDateColumnName())).isNullOrEmpty();

                                                                signal.countDown();
                                                            }

                                                            @Override
                                                            public void onFailure(Exception exception) {
                                                                fail(exception.getMessage());
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onFailure(Exception exception) {
                                                fail(exception.getMessage());
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail(e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        fail(exception.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                fail(error.getMessage());
            }

            protected void scheduleCleanup(String createdRecipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getEngageConfigManager().engageListId())
                        .recipientId(createdRecipientId)
                        .build());
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    public void testCheckIdentity_s3_existingRecipientWithMobileUserId() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String RECIPIENT_LIST_ID = getEngageConfigManager().engageListId();

        // setup recipient on server with recipientId and mobileUserId set
        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(SetupRecipientResult result) {
                final String createdWithMobileUserId_RecipientId = result.getRecipientId();
                scheduleCleanup(createdWithMobileUserId_RecipientId);
                final String originalCurrentMobileUserId = EngageConfig.mobileUserId(getContext());

                final String customId = UUID.randomUUID().toString();
                final String originalExistingMobileUserId = UUID.randomUUID().toString();

                // setup existing recipient on server with custom id and a different mobileUserId
                XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(RECIPIENT_LIST_ID)
                        .column(CUSTOM_ID_COLUMN, customId)
                        .column(getEngageConfigManager().mobileUserIdColumnName(), originalExistingMobileUserId)
                        .build();
                getXMLAPIManager().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        final String createdWithCustomId_RecipientId = addRecipientResponse.getRecipientId();
                        scheduleCleanup(createdWithCustomId_RecipientId);

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |
                        //    value    |     value    |  value

                        // look for an existing recipient with customId

                        Map<String, String> idFieldNamesToValues = new HashMap<String, String>();
                        idFieldNamesToValues.put(CUSTOM_ID_COLUMN, customId);
                        MobileConnectorManager.get().checkIdentity(idFieldNamesToValues, new CheckIdentityHandler() {
                            @Override
                            public void onSuccess(CheckIdentityResult result) {
                                final String recipientId = result.getRecipientId();
                                final String mobileUserId = result.getMobileUserId();

                                // verify the app is now using the existing recipient
                                assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(originalExistingMobileUserId);
                                assertThat(EngageConfig.recipientId(getContext())).isEqualTo(createdWithCustomId_RecipientId);

                                // verify old recipient is marked as merged on the server
                                getXMLAPIManager().postXMLAPI(XMLAPI.builder().operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
                                                .listId(RECIPIENT_LIST_ID).recipientId(createdWithMobileUserId_RecipientId).build(),
                                        new SelectRecipientResponseHandler() {
                                            @Override
                                            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {

                                                // check that properties didn't change
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mobileUserIdColumnName())).isEqualTo(originalCurrentMobileUserId);
                                                assertThat(selectRecipientResponse.getColumnValue(CUSTOM_ID_COLUMN)).isNullOrEmpty();

                                                // check marked as merged
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedRecipientIdColumnName())).isEqualTo(createdWithCustomId_RecipientId);
                                                assertThat(selectRecipientResponse.getColumnValue(getEngageConfigManager().mergedDateColumnName())).isNotEmpty();

                                                signal.countDown();
                                            }

                                            @Override
                                            public void onFailure(Exception exception) {
                                                fail(exception.getMessage());
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail(e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        fail(exception.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                fail(error.getMessage());
            }

            protected void scheduleCleanup(String recipientId) {
                tearDownAPICalls.add(XMLAPI.builder()
                        .operation(XMLAPIOperation.REMOVE_RECIPIENT)
                        .listId(getEngageConfigManager().engageListId())
                        .recipientId(recipientId)
                        .build());
            }
        });

        signal.await(20, TimeUnit.SECONDS);
    }

    //[Lindsay Thurmond:1/13/15] TODO: tests with audit record

    //[Lindsay Thurmond:1/13/15] TODO: check that merged columns only used if not using audit record

    //[Lindsay Thurmond:1/13/15] TODO: scenario 2 add validation for EngageConfig properties set correctly

}