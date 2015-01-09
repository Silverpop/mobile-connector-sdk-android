package com.silverpop.engage.response;

import android.test.AndroidTestCase;

import java.util.Map;

public class SelectRecipientResponseTest extends AndroidTestCase {

   private SelectRecipientResponse successResponse;
   private SelectRecipientResponse failureResponse;

    private static final String SELECT_RECIPIENT_DATA_RESULT_SUCCESS = "<Envelope>\n" +
            "\t<Body>\n" +
            "\t\t<RESULT>\n" +
            "\t\t\t<SUCCESS>TRUE</SUCCESS>\n" +
            "\t\t\t<EMAIL>somebody@domain.com</EMAIL>\n" +
            "\t\t\t<Email>somebody@domain.com</Email>\n" +
            "\t\t\t<RecipientId>33439394</RecipientId>\n" +
            "\t\t\t<EmailType>0</EmailType>\n" +
            "\t\t\t<LastModified>6/25/04 3:29 PM</LastModified>\n" +
            "\t\t\t<CreatedFrom>1</CreatedFrom>\n" +
            "\t\t\t<OptedIn>6/25/04 3:29 PM</OptedIn>\n" +
            "\t\t\t<OptedOut/>\n" +
            "\t\t\t<COLUMNS>￼￼￼\n" +
            "\t\t\t\t<COLUMN>\n" +
            "\t\t\t\t\t<NAME>Fname</NAME>\n" +
            "\t\t\t\t\t<VALUE>Somebody</VALUE></COLUMN>\n" +
            "\t\t\t\t<COLUMN>\n" +
            "\t\t\t\t\t<NAME>Lname</NAME>\n" +
            "\t\t\t\t\t<VALUE>Special</VALUE>\n" +
            "\t\t\t\t</COLUMN>\n" +
            "\t\t\t</COLUMNS>\n" +
            "\t\t</RESULT>\n" +
            "\t</Body>\n" +
            "</Envelope>";

    private static final String SELECT_RECIPIENT_DATA_RESULT_FAILURE =
            "<Envelope>\n" +
            "\t<Body>\n" +
            "\t\t<RESULT>\n" +
            "\t\t\t<SUCCESS>false</SUCCESS>\n" +
            "\t\t</RESULT>\n" +
            "\t\t<Fault>\n" +
            "\t\t\t<Request/>\n" +
            "\t\t\t<FaultCode/>\n" +
            "\t\t\t<FaultString><![CDATA[Recipient not found or is not a member of the list.]]></FaultString>\n" +
            "\t\t\t<detail>\n" +
            "\t\t\t\t<error>\n" +
            "\t\t\t\t\t<errorid>128</errorid>\n" +
            "\t\t\t\t\t<module/>\n" +
            "\t\t\t\t\t<class>SP.API</class>\n" +
            "\t\t\t\t\t<method/>\n" +
            "\t\t\t\t</error>\n" +
            "\t\t\t</detail>\n" +
            "\t\t</Fault>\n" +
            "\t</Body>\n" +
            "</Envelope>";

    @Override
    public void setUp() throws Exception {
        successResponse = new SelectRecipientResponse(SELECT_RECIPIENT_DATA_RESULT_SUCCESS);
        failureResponse = new SelectRecipientResponse(SELECT_RECIPIENT_DATA_RESULT_FAILURE);
    }

    public void testIsSuccess() throws Exception {
        assertTrue(successResponse.isSuccess());
    }

    public void testGetEmail() throws Exception {
        assertEquals(successResponse.getEmail(), "somebody@domain.com");
    }

    public void testGetRecipientId() throws Exception {
        assertEquals(successResponse.getRecipientId(), "33439394");
    }

    public void testGetEmailType() throws Exception {
        assertEquals(successResponse.getEmailType().intValue(), 0);
    }

    public void testGetLastModified() throws Exception {
        assertEquals(successResponse.getLastModified(), "6/25/04 3:29 PM");
    }

    public void testGetCreatedFrom() throws Exception {
        assertEquals(successResponse.getCreatedFrom().intValue(), 1);
    }

    public void testGetOptedIn() throws Exception {
        assertEquals(successResponse.getOptedIn(), "6/25/04 3:29 PM");
    }

    public void testGetOptedOut() throws Exception {
        assertNull(successResponse.getOptedOut());
    }

    public void testGetFaultString() throws Exception {
        assertNull(successResponse.getFaultString());
        assertEquals(failureResponse.getFaultString(), "Recipient not found or is not a member of the list.");
    }

    public void testGetErrorId() throws Exception {
        assertNull(successResponse.getErrorId());
        assertEquals(failureResponse.getErrorId().intValue(), 128);
    }

    public void testGetErrorCode() throws Exception {
        assertNull(successResponse.getErrorCode());
        assertEquals(failureResponse.getErrorCode(), ErrorCode.RECIPIENT_NOT_LIST_MEMBER);

    }

    public void testGetColumns() throws Exception {
        Map<String, String> columns = successResponse.getColumns();
        assertEquals(columns.size(), 2);
        assertEquals(columns.get("Fname"), "Somebody");
        assertEquals(columns.get("Lname"), "Special");
    }
}