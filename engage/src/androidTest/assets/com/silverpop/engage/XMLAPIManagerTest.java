package com.silverpop.engage;

import android.content.Context;
import android.content.SharedPreferences;
import com.silverpop.BaseAndroidTest;

public class XMLAPIManagerTest extends BaseAndroidTest {

    private XMLAPIManager xmlapiManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();

//        userManager = XMLAPIManager.initialize(testContext());
    }

    private void clearSharedPreferences() throws Exception {
        SharedPreferences settings = testContext().getSharedPreferences("PreferencesName", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
//        EngageConfig.storeMobileUserId(getContext(), null);
//        EngageConfig.storeAnonymousUserId(getContext(), null);
    }

//    public void testSetupRecipient() throws Exception {
//        clearSharedPreferences();
//
//        EngageConfigManager.get(testContext()).engageListId();
//
//        userManager.setupRecipient(new SetupUserHandler() {
//            @Override
//            public void onSuccess(String recipientId) {
//                assertNotNull(recipientId);
//                assertFalse(recipientId.isEmpty());
//            }
//
//            @Override
//            public void onCreateFailure(String error) {
//                fail(error);
//            }
//        });
//    }

}