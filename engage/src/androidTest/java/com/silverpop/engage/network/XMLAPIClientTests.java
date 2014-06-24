package com.silverpop.engage.network;

import android.test.AndroidTestCase;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.silverpop.engage.network.XMLAPIClient;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class XMLAPIClientTests
        extends AndroidTestCase {

//    private XMLAPIClient xmlapiClient;
//
//    public void setUp() {
//        xmlapiClient = XMLAPIClient.init(getContext(), TestingValues.clientId, TestingValues.clientSecret, TestingValues.refreshToken, TestingValues.host);
//    }
//
//    public void testValidXMLAPIClientCreation() throws InterruptedException {
//        assertTrue(xmlapiClient != null);
//    }
//
////    public void testXMLAPIClientOAuth2ConnectionCredentials() {
////        assertFalse(xmlapiClient.isAuthTokenExpired());
////    }

    public void testXMLAPIOAuth2TokenRefresh() {
        assertTrue(false);
    }

//    public void testXMLAPICreateAnonymousUserList() throws InterruptedException {
//        final XMLAPI api = XMLAPI.addRecipientAnonymousToList(TestingValues.list_id);
//
//        String url = "http://apipilot.silverpop.com/XMLAPI";
//        StringRequest req = new StringRequest(Request.Method.POST,
//                url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                EngageResponseXML responseXML = new EngageResponseXML(response);
//                String successKeyPath = "Envelope.Body.RESULT.SUCCESS";
//                try {
//                    assertTrue(responseXML.valueForKeyPath(successKeyPath).equalsIgnoreCase("TRUE"));
//                } catch (XMLResponseParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                assertTrue(false);
//            }
//        })
//        {
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("xml", api.envelope());
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> params = new HashMap<String, String>();
//                params.put("Authorization", "Bearer " + xmlapiClient.getOAuthClient());
//                return params;
//            }
//        };
//
//        xmlapiClient.request(req);
//        Thread.sleep(10000);
//    }
}
