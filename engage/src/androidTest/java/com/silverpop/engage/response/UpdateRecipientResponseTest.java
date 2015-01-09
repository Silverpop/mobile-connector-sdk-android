package com.silverpop.engage.response;

import android.test.AndroidTestCase;

public class UpdateRecipientResponseTest extends AndroidTestCase {

    private UpdateRecipientResponse response;

    private static final String UPDATE_RECIPIENT_RESULT =
            "<Envelope>\n" +
            "\t<Body>\n" +
            "\t\t<RESULT>\n" +
            "\t\t\t<SUCCESS>TRUE</SUCCESS>\n" +
            "\t\t\t<RecipientId>33439394</RecipientId>\n" +
            "\t\t</RESULT>\n" +
            "\t</Body>\n" +
            "</Envelope>";

    public void setUp() throws Exception {
        super.setUp();
        response = new UpdateRecipientResponse(UPDATE_RECIPIENT_RESULT);
    }

    public void testIsSuccess() throws Exception {
        assertTrue(response.isSuccess());
    }

    public void testGetRecipientId() throws Exception {
        assertEquals(response.getRecipientId(), "33439394");
    }
}