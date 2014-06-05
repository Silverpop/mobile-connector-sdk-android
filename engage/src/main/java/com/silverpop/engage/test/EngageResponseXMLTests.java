package com.silverpop.engage.test;

import android.test.AndroidTestCase;

import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.response.EngageResponseXML;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageResponseXMLTests
        extends AndroidTestCase {

    public void testDateStructure() {
        String xml = "<Envelope>\n" +
                "<Body>\n" +
                "<RESULT>\n" +
                "<SUCCESS>TRUE</SUCCESS>\n" +
                "<RecipientId>7297719</RecipientId>\n" +
                "<ORGANIZATION_ID>2b223cb6-13e31f3b860-adc7aae00aa079a6d33c00914c8e3999</ORGANIZATION_ID>\n" +
                "</RESULT>\n" +
                "</Body>\n" +
                "</Envelope>";
        EngageResponseXML responseXML = new EngageResponseXML(xml);

        try {
            String response = responseXML.valueForKeyPath("Envelope.Body.RESULT.SUCCESS");
            assertTrue(response.equals("TRUE"));
        } catch (XMLResponseParseException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

        //Expect exception due to element described not being a leaf
        try {
            String response = responseXML.valueForKeyPath("Envelope.Body.RESULT");
            assertTrue(false);
        } catch (XMLResponseParseException ex) {
            assertTrue(true);
        }

        //Expect exception due to element described not being a leaf
        try {
            String response = responseXML.valueForKeyPath("Body.RESULT");
            assertTrue(false);
        } catch (XMLResponseParseException ex) {
            assertTrue(true);
        }
    }
}
