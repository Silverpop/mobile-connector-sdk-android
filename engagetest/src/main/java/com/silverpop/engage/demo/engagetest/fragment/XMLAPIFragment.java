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
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.demo.engagetest.R;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class XMLAPIFragment
    extends Fragment {

    private static final String TAG = XMLAPIFragment.class.getName();

    private Button mCreateAnonymousUserButton;
    private Button mUpgradeAnonymousUserButton;
    private Button mMergeAnonymousUserButton;

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
        View v = inflater.inflate(R.layout.xmlapi_view, container, false);

        final Resources r = getActivity().getResources();

        xmlapiManager = XMLAPIManager.get();

        xmlApiResultTextView = (TextView)v.findViewById(R.id.xmlApiResultTextView);
        mCreateAnonymousUserButton = (Button)v.findViewById(R.id.createAnonymousUserButton);
        mCreateAnonymousUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xmlapiManager.createAnonymousUserList(EngageConfigManager.get(getActivity()).engageListId(), new AsyncTask<EngageResponseXML, Void, Object>() {
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

                            Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "Error creating anonymous user: "
                                + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mUpgradeAnonymousUserButton = (Button)v.findViewById(R.id.upgradeAnonymousUserButton);
        mUpgradeAnonymousUserButton.setEnabled(false);
        mUpgradeAnonymousUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "Ok gonna add the recipient now");
                XMLAPI addRecipient = XMLAPI.addRecipient("jeremy.dyer@makeandbuild.com", EngageConfigManager.get(getActivity()).engageListId());
                xmlapiManager.postXMLAPI(addRecipient, new AsyncTask<EngageResponseXML, Void, Object>() {

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
                                Toast.makeText(getActivity().getApplicationContext(), responseXML.getXml(), Toast.LENGTH_LONG).show();
                                String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                                recipientId = id;
                                xmlApiResultTextView.setText("Anonymous User RecipientID : " + id);
                            } else {
                                String faultString = responseXML.valueForKeyPath("envelope.body.fault.faultstring");
                                xmlApiResultTextView.setText("ERROR: " + faultString);
                            }

                            Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "Error creating anonymous user: "
                                + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        mMergeAnonymousUserButton = (Button)v.findViewById(R.id.mergeAnonymousUserButton);
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
                                Toast.makeText(getActivity().getApplicationContext(), responseXML.getXml(), Toast.LENGTH_LONG).show();
                                String id = responseXML.valueForKeyPath("envelope.body.result.recipientid");
                                recipientId = id;
                                xmlApiResultTextView.setText("Anonymous User Merged to known user : " + id);
                            } else {
                                String faultString = responseXML.valueForKeyPath("envelope.body.fault.faultstring");
                                xmlApiResultTextView.setText("ERROR: " + faultString);
                            }

                            Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "Error creating anonymous user: "
                                + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return v;
    }
}
