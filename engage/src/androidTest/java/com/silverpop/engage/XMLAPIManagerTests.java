package com.silverpop.engage;

import android.os.AsyncTask;
import android.test.AndroidTestCase;

import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by jeremydyer on 5/21/14.
 */
public class XMLAPIManagerTests
    extends AndroidTestCase {

//    private XMLAPIManager xmlapiManager;
//
//    public void setUp() throws Exception {
//        xmlapiManager = XMLAPIManager.initialize(getContext(), TestingValues.clientId, TestingValues.clientSecret, TestingValues.refreshToken, TestingValues.host);
//    }
//
//    public void testPostXMLAPIDefault() {
//        try {
//            XMLAPI api = XMLAPI.addRecipient("test@somedomain.com", TestingValues.list_id);
//            xmlapiManager.postXMLAPI(api, null, null);
//            assertTrue(true);
//        } catch (Exception ex) {
//            assertTrue(false);
//        }
//    }
//
//    public void testPostXMLAPIWithSuccessTask() {
//        try {
//            XMLAPI api = XMLAPI.addRecipient("test@somedomain.com", TestingValues.list_id);
//
//            AsyncTask<EngageResponseXML, Void, Object> success = new AsyncTask<EngageResponseXML, Void, Object>() {
//                @Override
//                protected Object doInBackground(EngageResponseXML... engageResponseXMLs) {
//                    if (engageResponseXMLs != null && engageResponseXMLs.length == 1) {
//                        EngageResponseXML response = engageResponseXMLs[0];
//                        String successKeyPath = "Envelope.Body.RESULT.SUCCESS";
//                        try {
//                            assertTrue(response.valueForKeyPath(successKeyPath).equalsIgnoreCase("TRUE"));
//                        } catch (XMLResponseParseException e) {
//                            assertTrue(false);
//                        }
//                    } else {
//                        assertTrue(false);
//                    }
//
//                    return "ALL GOOD";
//                }
//            };
//
//            xmlapiManager.postXMLAPI(api, success, null);
//            Thread.sleep(10000);
//        } catch (Exception ex) {
//            assertTrue(false);
//        }
//    }
//
//    public void testAddAnonymousUser() {
//        try {
//            AsyncTask<EngageResponseXML, Void, Object> success = new AsyncTask<EngageResponseXML, Void, Object>() {
//                @Override
//                protected Object doInBackground(EngageResponseXML... engageResponseXMLs) {
//                    if (engageResponseXMLs != null && engageResponseXMLs.length == 1) {
//                        EngageResponseXML response = engageResponseXMLs[0];
//                        String successKeyPath = "Envelope.Body.RESULT.SUCCESS";
//                        try {
//                            assertTrue(response.valueForKeyPath(successKeyPath).equalsIgnoreCase("TRUE"));
//                        } catch (XMLResponseParseException e) {
//                            assertTrue(false);
//                        }
//                    } else {
//                        assertTrue(false);
//                    }
//
//                    return "ALL GOOD";
//                }
//            };
//
//            xmlapiManager.createAnonymousUserList(TestingValues.list_id, success, null);
//            Thread.sleep(10000);
//        } catch (Exception ex) {
//            assertTrue(false);
//        }
//    }
}
