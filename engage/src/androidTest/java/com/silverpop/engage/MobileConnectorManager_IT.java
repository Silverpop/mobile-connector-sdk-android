package com.silverpop.engage;

import android.util.Log;
import com.silverpop.BaseAndroidTest;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIElement;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.recipient.SetupRecipientHandler;
import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MobileConnectorManager_IT extends BaseAndroidTest {

    private List<XMLAPI> tearDownAPICalls = new ArrayList<XMLAPI>();

    private static final String TAG = MobileConnectorManager_IT.class.getName();

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
            public void onSuccess(String recipientId) {
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
                XMLAPI removeRecipientXml = new XMLAPI(XMLAPIOperation.REMOVE_RECIPIENT);
                removeRecipientXml.addListIdParam(getEngageConfigManager().engageListId());
                removeRecipientXml.addParam(XMLAPIElement.RECIPIENT_ID, recipientId);
                removeRecipientXml.addColumn(getEngageConfigManager().mobileUserIdColumnName(), EngageConfig.mobileUserId(getContext()));
                tearDownAPICalls.add(removeRecipientXml);
            }
        });

        // The testing thread will wait here until the UI thread releases it
        // above with the countDown() or 30 seconds passes and it times out.
        signal.await(30, TimeUnit.SECONDS);
    }

    public void testSetupRecipient_recipientPreviouslySetup() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final String prevMobileUserId = MobileConnectorManagerImpl.get().generateMobileUserId();
        final String prevRecipientId = "00000";

        // pretend these were previously set
        EngageConfig.storeMobileUserId(getContext(), prevMobileUserId);
        EngageConfig.storeRecipientId(getContext(), prevRecipientId);

        assertThat(EngageConfig.mobileUserId(getContext())).isEqualTo(prevMobileUserId);
        assertThat(EngageConfig.recipientId(getContext())).isEqualTo(prevRecipientId);

        // calling setup recipient again should return the values that were previously set instead of generating new ones
        MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
            @Override
            public void onSuccess(String recipientId) {
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
                EngageConfig.storeRecipientId(getContext(), existingRecipientId);
                assertThat(EngageConfig.mobileUserId(getContext())).isEmpty();
                // setup complete, we now have an existing recipient with an email and recipient id only, not a mobile user id

                // start actual test
                MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
                    @Override
                    public void onSuccess(String recipientId) {
                        scheduleCleanup(addRecipientResponse.getRecipientId());

                        // recipient should have been updated with mobile user id
                        assertThat(recipientId).isEqualTo(existingRecipientId);
                        assertThat(EngageConfig.mobileUserId(getContext())).isNotEmpty();

                        // release countdown
                        signal.countDown();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        scheduleCleanup(addRecipientResponse.getRecipientId());

                        fail(error.getMessage());
                    }
                });
            }

            protected void scheduleCleanup(String recipientId) {
                XMLAPI removeRecipientXml = new XMLAPI(XMLAPIOperation.REMOVE_RECIPIENT);
                removeRecipientXml.addListIdParam(listId);
                removeRecipientXml.addParam(XMLAPIElement.EMAIL, email);
                removeRecipientXml.addParam(XMLAPIElement.RECIPIENT_ID, recipientId);
                tearDownAPICalls.add(removeRecipientXml);
            }

            @Override
            public void onFailure(Exception exception) {
                fail("Failed to setup test recipient: " + exception.getMessage());
            }
        });


        signal.await(30, TimeUnit.SECONDS);
    }

    public void testCheckIdentity() throws Exception {
        //[Lindsay Thurmond:1/12/15] TODO: implement me!
        fail();
    }
}