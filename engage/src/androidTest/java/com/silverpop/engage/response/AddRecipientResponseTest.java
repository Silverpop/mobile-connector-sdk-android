package com.silverpop.engage.response;

import android.test.AndroidTestCase;

public class AddRecipientResponseTest extends AndroidTestCase {

    private AddRecipientResponse response;

    private static final String ADD_RECIPIENT_RESPONSE_SUCCESS =
            "<Envelope>\n" +
            "\t<Body>\n" +
            "\t\t<RESULT>\n" +
            "\t\t\t<SUCCESS>TRUE</SUCCESS>\n" +
            "\t\t\t<RecipientId>33535067</RecipientId>\n" +
            "\t\t</RESULT>\n" +
            "\t</Body>\n" +
            "</Envelope>";

    public void setUp() throws Exception {
        super.setUp();

        response = new AddRecipientResponse(ADD_RECIPIENT_RESPONSE_SUCCESS);
    }

    public void testIsSuccess() throws Exception {
        assertTrue(response.isSuccess());
    }

    public void testGetRecipientId() throws Exception {
        assertEquals(response.getRecipientId(), "33535067");
    }

    public void testGetFaultString() throws Exception {
        assertNull(response.getFaultString());

        //[Lindsay Thurmond:1/7/15] TODO: add example with actual fault string
    }
}