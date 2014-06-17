package com.silverpop.engage.network;

import android.test.AndroidTestCase;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.network.UBFClient;

import org.json.JSONObject;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBFClientTests
        extends AndroidTestCase {

    private UBFClient ubfClient;

    //TODO: Move these values to external configuration file
    private static final String host = "apipilot.silverpop.com";
    private static final String clientId = "02eb567b-3674-4c48-8418-dbf17e0194fc";
    private static final String clientSecret = "9c650c5b-bcb8-4eb3-bf0a-cc8ad9f41580";
    private static final String refreshToken = "676476e8-2d1f-45f9-9460-a2489640f41a";
    private static final String list_id = "23949";

    @Override
    public void setUp() throws Exception {
        ubfClient = UBFClient.init(getContext(), clientId, clientSecret, refreshToken, host);
    }


    public void testAuthentication() {
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void testValidUBFClientCreation() {
//        assertTrue(ubfClient != null);
//    }
//
//    public void testUBFClientOAuth2ConnectionCredentials() {
//        assertFalse(ubfClient.isAuthTokenExpired());
//    }
//
//    public void testUBFPostOffline() {
//        assertTrue(false);
//    }
//
//    public void testUBFPostOnline() {
//        UBF installed = UBF.installed(getContext(), null);
//        ubfClient.postUBFEvent(installed, successListener(), errorListener());
//
//        //Refactoring. test no longer valid
////        assertTrue(ubfClient.getEventsQueuedCounter() == 1);
////        assertTrue(ubfClient.getEventsSentCounter() == 0);
////
////        assertTrue(ubfClient.getEventsQueuedCounter() == 1);
////        assertTrue(ubfClient.getEventsSentCounter() == 1);
//    }
//
//    /**
//     * Handles the successful completion of the UBF post
//     *
//     * @return
//     */
//    private Response.Listener<JSONObject> successListener() {
//        return new Response.Listener<JSONObject>() {
//            public void onResponse(JSONObject response) {
//                System.out.println("RESPONSE " + response);
//            }
//        };
//    }
//
//    /**
//     * Handles the failure of a UBF event post.
//     *
//     * @return
//     */
//    private Response.ErrorListener errorListener() {
//        return new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println("ERROR " + error);
//            }
//        };
//    }
//
//    public void testUBFEventPostsAfterNetworkRegained() {
//        assertTrue(false);
//    }
//
//    public void testUBFEventQueuedWhenNetworkOffline() {
//        assertTrue(false);
//    }
//
//    public void testUBFEnqueueEngageEvent() {
//        UBF installed = UBF.installed(getContext(), null);
//        ubfClient.postUBFEvent(installed, successListener(), errorListener());
//
//        //Refactoring. test no longer valid
////        assertTrue(ubfClient.getEventsQueuedCounter() == 1);
////        assertTrue(ubfClient.getEventsSentCounter() == 0);
//    }
//
//    public void testUBFPostEventCache() {
//        UBF installed = UBF.installed(getContext(), null);
//        //Refactoring. test no longer valid
////        assertTrue(ubfClient.getEventsQueuedCounter() == 0);
////        assertTrue(ubfClient.getEventsSentCounter() == 0);
////
////        ubfClient.postUBFEvent(installed, successListener(), errorListener());
////        assertTrue(ubfClient.getEventsQueuedCounter() == 1);
////        assertTrue(ubfClient.getEventsSentCounter() == 1);
//    }
//
//    public void testUBFOAuth2TokenRefresh() {
//        assertTrue(false);
//    }
}
