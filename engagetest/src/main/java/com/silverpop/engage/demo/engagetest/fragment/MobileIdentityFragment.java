package com.silverpop.engage.demo.engagetest.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.silverpop.engage.MobileIdentityManager;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.demo.engagetest.R;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.domain.XMLAPIOperation;
import com.silverpop.engage.recipient.*;
import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;
import com.silverpop.engage.response.handler.XMLAPIResponseFailure;
import com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator;

import java.util.HashMap;
import java.util.Map;

public class MobileIdentityFragment extends Fragment {

    private static final String TAG = MobileIdentityFragment.class.getName();

    private TextView currentConfigValuesTextView;
    private Button clearConfigButton;
    private Button setupRecipientUserButton;
    private Button setupScenario1Button;
    private Button setupScenario2Button;
    private Button setupScenario3Button;
    private EditText customIdFieldName;
    private EditText customIdFieldValue;
    private Button checkIdentityButton;


    private MobileIdentityManager mobileIdentityManager = null;

    private static final String CUSTOM_ID_COLUMN = "Custom Integration Test Id";


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.mobileidentity_view, container, false);

        final Resources r = getActivity().getResources();

        mobileIdentityManager = MobileIdentityManager.get();

        currentConfigValuesTextView = (TextView) view.findViewById(R.id.currentConfigValuesTextView);

        customIdFieldName = (EditText) view.findViewById(R.id.customIdFieldText);
        customIdFieldValue = (EditText) view.findViewById(R.id.customIdValueText);


        createClearConfigButton(view);
        setupRecipientUserButton(view);

        createScenario1Button(view);
        createScenario2Button(view);
        createScenario3Button(view);
        createCheckIdentityButton(view);

        updateCheckIdentityEnabledState();
        customIdFieldName.setText(CUSTOM_ID_COLUMN);

        clearCurrentConfig();

        return view;
    }

    private void updateScreen() {
        updateCurrentConfig();
        updateCheckIdentityEnabledState();
    }

    private void updateCurrentConfig() {
        String recipientId = EngageConfig.recipientId(getActivity());
        if (TextUtils.isEmpty(recipientId)) {
            recipientId = "[Undefined]";
        }
        String mobileUserId = EngageConfig.mobileUserId(getActivity());
        if (TextUtils.isEmpty(mobileUserId)) {
            mobileUserId = "[Undefined]";
        }
        String currentConfig = String.format("Recipient Id:\n%s\nMobile User Id:\n%s", recipientId, mobileUserId);
        currentConfigValuesTextView.setText(currentConfig);
    }

    private void updateCheckIdentityEnabledState() {

        boolean checkIdentityEnabled = !TextUtils.isEmpty(EngageConfig.recipientId(getActivity()))
                && !TextUtils.isEmpty(EngageConfig.mobileUserId(getActivity()));

        setupScenario1Button.setEnabled(checkIdentityEnabled);
        setupScenario2Button.setEnabled(checkIdentityEnabled);
        setupScenario3Button.setEnabled(checkIdentityEnabled);
        checkIdentityButton.setEnabled(checkIdentityEnabled);
    }

    private void enableSetupButtons() {
        setupScenario1Button.setEnabled(true);
        setupScenario2Button.setEnabled(true);
        setupScenario3Button.setEnabled(true);
    }

    private void disableSetupButtons() {
        setupScenario1Button.setEnabled(false);
        setupScenario2Button.setEnabled(false);
        setupScenario3Button.setEnabled(false);
    }

    private void createClearConfigButton(final View view) {
        clearConfigButton = (Button) view.findViewById(R.id.clearConfigBtn);
        clearConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCurrentConfig();
            }
        });
    }

    protected void clearCurrentConfig() {
        EngageConfig.storeMobileUserId(getActivity(), "");
        EngageConfig.storeRecipientId(getActivity(), "");
        EngageConfig.storeAnonymousUserId(getActivity(), "");

        customIdFieldValue.setText("");
        updateScreen();
    }

    private void createScenario1Button(View view) {
        setupScenario1Button = (Button) view.findViewById(R.id.scenario1Btn);
        setupScenario1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // set custom id to one that won't exist
                customIdFieldValue.setText(new DefaultUUIDGenerator().generateUUID());

                disableSetupButtons();
            }
        });
    }

    private void createScenario2Button(View view) {
        setupScenario2Button = (Button) view.findViewById(R.id.scenario2Btn);
        setupScenario2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup recipient on server with recipientId and mobileUserId set

                final String customId = new DefaultUUIDGenerator().generateUUID();

                // setup existing recipient on server with custom id but not a mobile user id
                XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(EngageConfigManager.get(getActivity()).engageListId())
                        .column(CUSTOM_ID_COLUMN, customId)
                        .build();

                disableSetupButtons();

                XMLAPIManager.get().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        String createdWithCustomId_RecipientId = addRecipientResponse.getRecipientId();

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |             - previously created by setup recipient
                        //    value    |              |  value      - just created

                        String successMessage = String.format("Success\nSetup existing recipient.\nRecipient id: %s\nCustom id: %s",
                                createdWithCustomId_RecipientId, customId);

                        Log.i(TAG, "Scenario 2 Successful setup");
                        Log.i(TAG, successMessage);

                        customIdFieldValue.setText(customId);
                        disableSetupButtons();

                        showToast(successMessage);
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        String message = "Failed to setup scenario 2";
                        Log.e(TAG, message, failure.getException());
                        showToast(message);

                        enableSetupButtons();
                    }
                });

            }
        });
    }

    private void createScenario3Button(View view) {
        setupScenario3Button = (Button) view.findViewById(R.id.scenario3Btn);
        setupScenario3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String customId = new DefaultUUIDGenerator().generateUUID();
                final String existingMobileUserId = new DefaultUUIDGenerator().generateUUID();

                disableSetupButtons();

                // setup existing recipient on server with custom id(s) and a different mobileUserId
                final XMLAPI addRecipientWithCustomIdXml = XMLAPI.builder()
                        .operation(XMLAPIOperation.ADD_RECIPIENT)
                        .listId(EngageConfigManager.get(getActivity()).engageListId())
                        .column(EngageConfigManager.get(getActivity()).mobileUserIdColumnName(), existingMobileUserId)
                        .column(CUSTOM_ID_COLUMN, customId)
                        .build();

                XMLAPIManager.get().postXMLAPI(addRecipientWithCustomIdXml, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        String createdWithCustomId_RecipientId = addRecipientResponse.getRecipientId();

                        // we now have 2 recipients configured as:
                        // recipientId | mobileUserId | customId
                        //    value    |     value    |             - previously created by setup recipient
                        //    value    |     value    |  value      - just created

                        String successMessge = String.format("Success\nSetup existing recipient.\nRecipient id: %s\nMobile User id: %s\nCustom id: %s",
                                createdWithCustomId_RecipientId, existingMobileUserId, customId);

                        Log.i(TAG, "Scenario 3 Successful setup");
                        Log.i(TAG, successMessge);

                        customIdFieldValue.setText(customId);
                        disableSetupButtons();

                        showToast(successMessge);
                    }

                    @Override
                    public void onFailure(XMLAPIResponseFailure failure) {
                        String message = "Failed to setup scenario 3";
                        Log.e(TAG, message, failure.getException());
                        showToast(message);

                        enableSetupButtons();
                    }
                });

            }
        });
    }

    private void createCheckIdentityButton(View view) {
        checkIdentityButton = (Button) view.findViewById(R.id.checkIdentityButton);
        checkIdentityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customFieldName = customIdFieldName.getText().toString();
                String customFieldValue = customIdFieldValue.getText().toString();

                // in your app you'd pass your own value(s) here
                Map<String, String> fieldToValue = new HashMap<String, String>();
                fieldToValue.put(customFieldName, customFieldValue);

                checkIdentityButton.setEnabled(false);

                mobileIdentityManager.checkIdentity(fieldToValue, new CheckIdentityHandler() {
                    @Override
                    public void onSuccess(CheckIdentityResult result) {
                        checkIdentityButton.setEnabled(true);

                        String newRecipientId = result.getRecipientId();
                        if (TextUtils.isEmpty(newRecipientId)) {
                            newRecipientId = "Unknown";
                        }
                        String mergedRecipientId = result.getMergedRecipientId();
                        if (TextUtils.isEmpty(mergedRecipientId)) {
                            mergedRecipientId = "Unknown";
                        }
                        String mobileUserId = result.getMobileUserId();
                        if (TextUtils.isEmpty(mobileUserId)) {
                            mobileUserId = "Unknown";
                        }

                        String successMessage = String.format("Success\nCurrent recipient id: %s\nMerged recipient id: %s\nMobile user id: %s",
                                newRecipientId, mergedRecipientId, mobileUserId);

                        updateCurrentConfig();

                        Log.i(TAG, successMessage);
                        showToast(successMessage);
                    }

                    @Override
                    public void onFailure(CheckIdentityFailure failure) {
                        checkIdentityButton.setEnabled(true);
                        String message = "ERROR in check identity";
                        Log.e(TAG, message + ": " + failure.getMessage(), failure.getException());
                        showToast(message);
                    }
                });
            }
        });
    }

    private void setupRecipientUserButton(View view) {
        setupRecipientUserButton = (Button) view.findViewById(R.id.setupRecipientButton);
        setupRecipientUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setupRecipientUserButton.setEnabled(false);

                mobileIdentityManager.setupRecipient(new SetupRecipientHandler() {
                    @Override
                    public void onSuccess(SetupRecipientResult result) {
                        setupRecipientUserButton.setEnabled(true);

                        String message = String.format("Recipient Id: %s\nMobile User Id: %s",
                                result.getRecipientId(), EngageConfig.mobileUserId(getActivity()));
                        Log.i(TAG, message);
                        showToast(message);

                        updateScreen();
                    }

                    @Override
                    public void onFailure(SetupRecipientFailure failure) {
                        setupRecipientUserButton.setEnabled(true);

                        String message = "Failed to setup recipient";
                        Log.e(TAG, message + ": " + failure.getMessage(), failure.getException());
                        showToast(message);
                    }
                });
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
