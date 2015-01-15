package com.silverpop.engage.demo.engagetest.fragment;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.silverpop.engage.AnonymousMobileConnectorManager;
import com.silverpop.engage.MobileConnectorManager;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.demo.engagetest.R;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.XMLAPIResponseException;
import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.recipient.SetupRecipientHandler;
import com.silverpop.engage.recipient.SetupRecipientResult;
import com.silverpop.engage.response.AddRecipientResponse;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.handler.AddRecipientResponseHandler;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class XMLAPIFragment
    extends Fragment {

    private static final String TAG = XMLAPIFragment.class.getName();

    private Button mCreateAnonymousUserButton;
    private Button mUpgradeAnonymousUserButton;
    private Button mMergeAnonymousUserButton;
    private Button setupRecipientUserButton;

    private XMLAPIManager xmlapiManager = null;
    private TextView xmlApiResultTextView = null;
    private String recipientId = null;


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.xmlapi_view, container, false);

        final Resources r = getActivity().getResources();

        xmlapiManager = XMLAPIManager.get();

        xmlApiResultTextView = (TextView)view.findViewById(R.id.xmlApiResultTextView);

        createAnonymousUserButton(view);
        createUpgradeAnonymousUserButton(view);
        createMergeAnonymousUserButton(view);
        setupRecipientUserButton(view);

        return view;
    }

    private void setupRecipientUserButton(View view) {
        setupRecipientUserButton = (Button) view.findViewById(R.id.setupRecipientButton);
        setupRecipientUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MobileConnectorManager.get().setupRecipient(new SetupRecipientHandler() {
                    @Override
                    public void onSuccess(SetupRecipientResult result) {
                        String message = "Setup recipient auto configured recipient id: " + result.getRecipientId();
                        Log.i(TAG, message);
                        showToast(message);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        String message = "Failed to setup recipient";
                        Log.e(TAG, message + ": " + exception.getMessage(), exception);
                        showToast(message);
                    }
                });
            }
        });
    }

    private void createMergeAnonymousUserButton(View view) {
        mMergeAnonymousUserButton = (Button)view.findViewById(R.id.mergeAnonymousUserButton);
        mMergeAnonymousUserButton.setEnabled(false);
        mMergeAnonymousUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XMLAPI mergeRecipient = XMLAPI.updateRecipient(recipientId, EngageConfigManager.get(getActivity()).engageListId());
                xmlapiManager.postXMLAPI(mergeRecipient, new AsyncTask<EngageResponseXML, Void, Object>() {

                    @Override
                    protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                        return engageResponseXMLs[0];
                    }

                    @Override
                    protected void onPostExecute(Object responseObject) {
                        try {
                            EngageResponseXML responseXML = (EngageResponseXML)responseObject;
                            String result = responseXML.valueForKeyPath("envelope.body.result.success");
                            if (result.equalsIgnoreCase("true")) {
                                showToast(responseXML.getXml());
                                String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                                recipientId = id;
                                xmlApiResultTextView.setText("Anonymous User Merged to known user : " + id);
                            } else {
                                String faultString = responseXML.valueForKeyPath("envelope.body.fault.faultstring");
                                xmlApiResultTextView.setText("ERROR: " + faultString);
                            }

                            showToast(result);
                        } catch (XMLResponseParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new AsyncTask<VolleyError, Void, Object>() {
                    @Override
                    protected Object doInBackground(VolleyError... volleyErrors) {
                        Log.e(TAG, "Failure is posting create anonymous user event to silverpop");
                        return volleyErrors[0];
                    }

                    @Override
                    protected void onPostExecute(Object responseObject) {
                        VolleyError error = (VolleyError)responseObject;
                        showToast("Error creating anonymous user: "
                                + error.getMessage());
                    }
                });
            }
        });
    }

    private void createUpgradeAnonymousUserButton(View view) {
        mUpgradeAnonymousUserButton = (Button) view.findViewById(R.id.upgradeAnonymousUserButton);
        mUpgradeAnonymousUserButton.setEnabled(false);
        mUpgradeAnonymousUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final XMLAPI addRecipient = XMLAPI.addRecipientWithEmail(EngageConfig.mobileUserId(getActivity()), EngageConfigManager.get(getActivity()).engageListId());
                xmlapiManager.postXMLAPI(addRecipient, new AddRecipientResponseHandler() {
                    @Override
                    public void onAddRecipientSuccess(AddRecipientResponse addRecipientResponse) {
                        Toast.makeText(getActivity().getApplicationContext(), addRecipientResponse.getResponseXml().getXml(), Toast.LENGTH_LONG).show();
                        String id = addRecipientResponse.getRecipientId();
                        recipientId = id;
                        xmlApiResultTextView.setText("Anonymous User RecipientID : " + id);
                        showToast("SUCCESS: Recipient id = " + recipientId);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (error instanceof XMLAPIResponseException) {
                            String faultString = ((XMLAPIResponseException) error).getXmlResponse().getFaultString();
                            xmlApiResultTextView.setText("ERROR: " + faultString);
                            showToast("ERROR");
                        } else {
                            showToast("Error creating anonymous user: " + error.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void createAnonymousUserButton(View view) {
        mCreateAnonymousUserButton = (Button)view.findViewById(R.id.createAnonymousUserButton);
        mCreateAnonymousUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnonymousMobileConnectorManager.get().createAnonymousUserList(EngageConfigManager.get(getActivity()).engageListId(), new AsyncTask<EngageResponseXML, Void, Object>() {
                    @Override
                    protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                        return engageResponseXMLs[0];
                    }

                    @Override
                    protected void onPostExecute(Object responseObject) {
                        try {
                            EngageResponseXML responseXML = (EngageResponseXML) responseObject;
                            String result = responseXML.valueForKeyPath("envelope.body.result.success");
                            if (result.equalsIgnoreCase("true")) {
                                String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                                recipientId = id;
                                EngageConfig.storeAnonymousUserId(getActivity(), recipientId);
                                xmlApiResultTextView.setText("Anonymous User RecipientID : " + id);
                                mUpgradeAnonymousUserButton.setEnabled(true);
                                mMergeAnonymousUserButton.setEnabled(true);
                            } else {
                                String faultString = responseXML.valueForKeyPath("envelope.body.fault.faultstring");
                                xmlApiResultTextView.setText("ERROR: " + faultString);
                            }

                            showToast(result);
                        } catch (XMLResponseParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new AsyncTask<VolleyError, Void, Object>() {
                    @Override
                    protected Object doInBackground(VolleyError... volleyErrors) {
                        Log.e(TAG, "Failure is posting create anonymous user event to silverpop");
                        return volleyErrors[0];
                    }

                    @Override
                    protected void onPostExecute(Object responseObject) {
                        VolleyError error = (VolleyError) responseObject;
                        showToast("Error creating anonymous user: "
                                + error.getMessage());
                    }
                });
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
