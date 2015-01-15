package com.silverpop.engage.domain;

import android.os.AsyncTask;
import android.test.AndroidTestCase;
import android.util.Log;
import com.android.volley.VolleyError;
import com.silverpop.engage.AnonymousMobileIdentityManager;
import com.silverpop.engage.response.EngageResponseXML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class XMLAPITest
        extends AndroidTestCase {

    public void testExample1() {

        String expected = "<Envelope><Body><SelectRecipientData><LIST_ID>45654</LIST_ID><EMAIL>someone@adomain.com</EMAIL><COLUMN><NAME>Customer Id</NAME><VALUE>123-45-6789</VALUE></COLUMN></SelectRecipientData></Body></Envelope>";

        XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

        //Map of XMLAPI top level parameters.
        Map<String, Object> xmlapiParams = new LinkedHashMap<String, Object>();
        xmlapiParams.put("LIST_ID", "45654");
        xmlapiParams.put("EMAIL", "someone@adomain.com");

        selectRecipientData.addParams(xmlapiParams);

        //Map of XMLAPI NAME/VALUE columns.
        Map<String, Object> columns = new HashMap<String, Object>();
        columns.put("Customer Id", "123-45-6789");

        selectRecipientData.addColumns(columns);

        assertTrue(expected.equals(selectRecipientData.envelope()));
    }

    public void testExample2() {
        String expected = "<Envelope><Body><SelectRecipientData><LIST_ID>45654</LIST_ID><RECIPIENT_ID>702003</RECIPIENT_ID></SelectRecipientData></Body></Envelope>";

        XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

        //Map of XMLAPI top level parameters.
        Map<String, Object> xmlapiParams = new LinkedHashMap<String, Object>();
        xmlapiParams.put("LIST_ID", "45654");
        xmlapiParams.put("RECIPIENT_ID", "702003");

        selectRecipientData.addParams(xmlapiParams);

        assertTrue(expected.equals(selectRecipientData.envelope()));
    }

    public void testExample3() {
        String expected = "<Envelope><Body><SelectRecipientData><LIST_ID>45654</LIST_ID><EMAIL>someone@adomain.com</EMAIL></SelectRecipientData></Body></Envelope>";

        XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

        //Map of XMLAPI top level parameters.
        Map<String, Object> xmlapiParams = new LinkedHashMap<String, Object>();
        xmlapiParams.put("LIST_ID", "45654");
        xmlapiParams.put("EMAIL", "someone@adomain.com");

        selectRecipientData.addParams(xmlapiParams);

        assertTrue(expected.equals(selectRecipientData.envelope()));
    }


    public void testXMLAPICreation() {
        String resourceName = "AddRecipient";
        XMLAPI api = new XMLAPI(resourceName, null);
        assertNotNull(api);
        assertEquals(resourceName, api.getNamedResource());
    }

    public void testResourceNamed() {
        String resourceName = "AddRecipient";
        XMLAPI api = new XMLAPI(resourceName, null);
        assertNotNull(api);
        assertEquals(resourceName, api.getNamedResource());
        String resourceName2 = "AddRecipient2";
        api.setNamedResource(resourceName2);
        assertEquals(resourceName2, api.getNamedResource());
    }

    public void testResourceNamedWithParams() {
        String resourceName = "AddRecipient";
        XMLAPI api = new XMLAPI(resourceName, null);
        assertNotNull(api);
        assertEquals(resourceName, api.getNamedResource());
        assertTrue(api.getBodyElements() != null);
        assertTrue(api.getBodyElements().size() == 0);

        Map<String, Object> namedParam = new HashMap<String, Object>();
        namedParam.put("UPDATE_IF_AVAILABLE", Boolean.TRUE);

        api = new XMLAPI(resourceName, namedParam);
        assertNotNull(api);
        assertEquals(resourceName, api.getNamedResource());
        assertTrue(api.getBodyElements() != null);
        assertTrue(api.getBodyElements().size() == 1);
        assertTrue(api.getBodyElements().containsKey("UPDATE_IF_AVAILABLE"));
    }

    public void testAddContactLists() {
        //addElements will blindly
        XMLAPI api = new XMLAPI("UnitTest");

        //Setup tests
        assertTrue(api != null);
        assertTrue(api.getNamedResource() != null);
        assertTrue(api.getBodyElements().size() == 0);

        String[] contacts = new String[2];
        contacts[0] = "289032";
        contacts[1] = "12345";
        api.addContactLists(contacts);

        String expected = "<Envelope><Body><UnitTest>" +
                "<CONTACT_LISTS>" +
                "<CONTACT_LIST_ID>289032</CONTACT_LIST_ID>" +
                "<CONTACT_LIST_ID>12345</CONTACT_LIST_ID>" +
                "</CONTACT_LISTS>" +
                "</UnitTest></Body></Envelope>";

        assertEquals(expected, api.envelope());
    }

    public void testAddElements() {
        //addElements will blindly
        XMLAPI api = new XMLAPI("UnitTest");

        //Setup tests
        assertTrue(api != null);
        assertTrue(api.getNamedResource() != null);
        assertTrue(api.getBodyElements().size() == 0);

        //Add a made up element.
        String madeUpElementName = "SAND_DESC";
        Map<String, Object> sandElement = new HashMap<String, Object>();
        sandElement.put("texture", "coarse");
        sandElement.put("region", "aquatic");
        api.addElements(sandElement, madeUpElementName);

        assertTrue(api.getBodyElements().size() == 1);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).size() == 2);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).get("texture").equals("coarse"));
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).get("region").equals("aquatic"));

        //Dup to make sure original was overwritten
        madeUpElementName = "SAND_DESC";
        sandElement = new HashMap<String, Object>();
        sandElement.put("texture", "coarse2");
        sandElement.put("region", "aquatic2");
        api.addElements(sandElement, madeUpElementName);

        assertTrue(api.getBodyElements().size() == 1);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).size() == 2);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).get("texture").equals("coarse2"));
        assertTrue(((Map<String, Object>) api.getBodyElements().get(madeUpElementName)).get("region").equals("aquatic2"));
    }

    public void testAddSyncFields() {
        XMLAPI api = new XMLAPI("UnitTest");

        assertTrue(api != null);
        assertTrue(api.getNamedResource() != null);
        assertTrue(api.getBodyElements().size() == 0);

        Map<String, Object> syncFields = new HashMap<String, Object>();
        syncFields.put("FNAME", "Joe");
        syncFields.put("LNAME", "Smith");
        syncFields.put("EMAIL", "joe.smith@somedomain.com");
        api.addSyncFields(syncFields);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString())).size() == 3);
        assertTrue(api.getBodyElements().size() == 1); //There should only be a XMLAPIEnum.SYNC_FIELDS instance in the map

        //Add some new unique fields.
        syncFields = new HashMap<String, Object>();
        syncFields.put("ADDRESS1", "1 Infinite Loop");
        api.addSyncFields(syncFields);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString())).size() == 4);

        //Add an existing sync field.
        syncFields = new HashMap<String, Object>();
        String newFname = "Frank";
        syncFields.put("FNAME", newFname);
        api.addSyncFields(syncFields);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString())).size() == 4);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString())).get("FNAME").equals(newFname));
    }

    public void testAddColumns() {
        XMLAPI api = new XMLAPI("UnitTest");

        //Setup tests
        assertTrue(api != null);
        assertTrue(api.getNamedResource() != null);
        assertTrue(api.getBodyElements().size() == 0);

        Map<String, Object> cols = new HashMap<String, Object>();
        String col1Name = "Name";
        String col1Value = "Tom";
        String col2Name = "Email";
        String col2Value = "test@somedomain.com";
        cols.put(col1Name, col1Value);
        cols.put(col2Name, col2Value);

        api.addColumns(cols);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.COLUMNS.toString())).size() == 2);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.COLUMNS.toString())).get(col1Name).equals(col1Value));
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.COLUMNS.toString())).get(col2Name).equals(col2Value));

        Map<String, Object> syncFields = new HashMap<String, Object>();
        syncFields.put("FNAME", "Joe");
        syncFields.put("LNAME", "Smith");
        syncFields.put("EMAIL", "joe.smith@somedomain.com");
        api.addSyncFields(syncFields);
        assertTrue(((Map<String, Object>) api.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString())).size() == 3);

        assertTrue(api.getBodyElements().size() == 2);  //Sync_fields and columns should be present
    }

    public void testEnvelope() {
        XMLAPI api = new XMLAPI("UnitTest");
        assertTrue(api.envelope().startsWith("<Envelope><Body><UnitTest>"));
        assertTrue(api.envelope().endsWith("</UnitTest></Body></Envelope>"));
    }

    public void testSelectRecipientData() {
        String email = "test@silverpop.com";
        String listId = "12345";

        //Create the base message
        XMLAPI api = XMLAPI.selectRecipientData(email, listId);
        api.addColumn("Customer Id", "123-45-6789");

        String request1 = "<Envelope><Body><SelectRecipientData>" +
                "<LIST_ID>12345</LIST_ID>" +
                "<EMAIL>test@silverpop.com</EMAIL>" +
                "<COLUMN><NAME>Customer Id</NAME><VALUE>123-45-6789</VALUE></COLUMN>" +
                "</SelectRecipientData></Body></Envelope>";

        assertEquals(request1, api.envelope());
    }

    public void testAddRecipientWithEmail() {
        String email = "somebody@domain.com";
        String listId = "85628";

        XMLAPI api = XMLAPI.addRecipientWithEmail(email, listId);

        //Adds the extra Params
        Map<String, Object> extraParams = new HashMap<String, Object>();
        extraParams.put("CREATED_FROM", "1");
        api.addParams(extraParams);

        String[] contacts = new String[2];
        contacts[0] = "289032";
        contacts[1] = "12345";
        api.addContactLists(contacts);

        Map<String, Object> columns = new HashMap<String, Object>();
        columns.put("Customer Id", "123-45-6789");
        columns.put("EMAIL", "somebody@domain.com");
        columns.put("Fname", "John");
        api.addColumns(columns);

        String request1 = "<Envelope><Body><AddRecipient>" +
                "<LIST_ID>85628</LIST_ID>" +
                "<COLUMN><NAME>Customer Id</NAME><VALUE>123-45-6789</VALUE></COLUMN>" +
                "<COLUMN><NAME>Fname</NAME><VALUE>John</VALUE></COLUMN>" +
                "<COLUMN><NAME>EMAIL</NAME><VALUE>somebody@domain.com</VALUE></COLUMN>" +
                "<CREATED_FROM>1</CREATED_FROM>" +
                "<CONTACT_LISTS><CONTACT_LIST_ID>289032</CONTACT_LIST_ID><CONTACT_LIST_ID>12345</CONTACT_LIST_ID></CONTACT_LISTS>" +
                "<SYNC_FIELDS><SYNC_FIELD><NAME>EMAIL</NAME><VALUE>somebody@domain.com</VALUE></SYNC_FIELD></SYNC_FIELDS>" +
                "</AddRecipient></Body></Envelope>";

        assertEquals(request1, api.envelope());
    }

    public void testUpdateRecipient() {

        String email = "somebody@domain.com";
        String listId = "85628";
        XMLAPI api = XMLAPI.updateRecipient(email, listId);

        //Adds the extra Params
        Map<String, Object> extraParams = new HashMap<String, Object>();
        extraParams.put("CREATED_FROM", "2");
        extraParams.put("OLD_EMAIL", "somebody@domain.com");
        api.addParams(extraParams);

        Map<String, Object> columns = new HashMap<String, Object>();
        columns.put("Customer Id", "123-45-6789");
        columns.put("EMAIL", "somebody@domain.com");
        columns.put("Street_Address", "123 New Street");
        api.addColumns(columns);

        String request1 = "<Envelope><Body><UpdateRecipient>" +
                "<LIST_ID>85628</LIST_ID>" +
                "<RECIPIENT_ID>somebody@domain.com</RECIPIENT_ID>" +
                "<CREATED_FROM>2</CREATED_FROM>" +
                "<OLD_EMAIL>somebody@domain.com</OLD_EMAIL>" +
                "<COLUMN><NAME>Customer Id</NAME><VALUE>123-45-6789</VALUE></COLUMN>" +
                "<COLUMN><NAME>EMAIL</NAME><VALUE>somebody@domain.com</VALUE></COLUMN>" +
                "<COLUMN><NAME>Street_Address</NAME><VALUE>123 New Street</VALUE></COLUMN>" +
                "</UpdateRecipient></Body></Envelope>";

        assertEquals(request1, api.envelope());


        api = XMLAPI.updateRecipient(email, listId);

        //Adds the extra Params
        extraParams = new HashMap<String, Object>();
        extraParams.put("CREATED_FROM", "2");
        extraParams.put("OLD_EMAIL", "somebody@domain.com");
        api.addParams(extraParams);

        columns = new HashMap<String, Object>();
        columns.put("OPT_OUT", "false");
        columns.put("Customer Id", "123-45-6789");
        columns.put("EMAIL", "somebody@domain.com");
        columns.put("Street_Address", "123 New Street");
        api.addColumns(columns);

        String request2 = "<Envelope><Body><UpdateRecipient>" +
                "<LIST_ID>85628</LIST_ID>" +
                "<RECIPIENT_ID>somebody@domain.com</RECIPIENT_ID>" +
                "<CREATED_FROM>2</CREATED_FROM>" +
                "<OLD_EMAIL>somebody@domain.com</OLD_EMAIL>" +
                "<COLUMN><NAME>Customer Id</NAME><VALUE>123-45-6789</VALUE></COLUMN>" +
                "<COLUMN><NAME>EMAIL</NAME><VALUE>somebody@domain.com</VALUE></COLUMN>" +
                "<COLUMN><NAME>OPT_OUT</NAME><VALUE>false</VALUE></COLUMN>" +
                "<COLUMN><NAME>Street_Address</NAME><VALUE>123 New Street</VALUE></COLUMN>" +
                "</UpdateRecipient></Body></Envelope>";

        assertEquals(request2, api.envelope());
    }

    public void testAddRecipient() {
        XMLAPI xml = XMLAPI.addRecipient("mobile_user_id", "11111", "22222", false);
        final String expected1 = "<Envelope><Body><AddRecipient>" +
                "<LIST_ID>22222</LIST_ID>" +
                "<COLUMN><NAME>mobile_user_id</NAME><VALUE>11111</VALUE></COLUMN>" +
                "</AddRecipient></Body></Envelope>";
        assertThat(xml.envelope()).isEqualTo(expected1);

        xml = XMLAPI.addRecipient("mobile_user_id", "11111", "22222", true);
        final String expected2 = "<Envelope><Body><AddRecipient>" +
                "<LIST_ID>22222</LIST_ID>" +
                "<COLUMN><NAME>mobile_user_id</NAME><VALUE>11111</VALUE></COLUMN>" +
                "<UPDATE_IF_FOUND>true</UPDATE_IF_FOUND>" +
                "<SYNC_FIELDS><SYNC_FIELD><NAME>mobile_user_id</NAME><VALUE>11111</VALUE></SYNC_FIELD></SYNC_FIELDS>" +
                "</AddRecipient></Body></Envelope>";
        assertThat(xml.envelope()).isEqualTo(expected2);
    }

    public void testAddRecipientAnonymousToList() {
        String listId = "85628";
        XMLAPI api = AnonymousMobileIdentityManager.addRecipientAnonymousToList(listId);

        String expected = "<Envelope><Body><AddRecipient>" +
                "<LIST_ID>85628</LIST_ID>" +
                "</AddRecipient></Body></Envelope>";

        assertEquals(expected, api.envelope());
    }

    public void testCreateLastKnownLocationXMLAPI() {
        Map<String, Object> bodyElements = new HashMap<String, Object>();
        bodyElements.put("LIST_ID", "samplelistid");
        bodyElements.put("COLUMN_NAME", "Last Known Address");
        bodyElements.put("COLUMN_TYPE", 3);
        bodyElements.put("DEFAULT", "");
        XMLAPI createLastKnownLocationColumns = new XMLAPI(XMLAPIOperation.ADD_LIST_COLUMN, bodyElements);

        String env = createLastKnownLocationColumns.envelope();
        System.out.println(env);
    }

    public void testInsertUpdateRelationalTable() throws Exception {
        XMLAPI insertUpdateRelationalTable = XMLAPI.insertUpdateRelationalTable("86767");

        RelationalTableRow row1 = new RelationalTableRow();
        row1.addColumn("Record Id", "11111");
        row1.addColumn("Purchase Date", "01/01/2015");
        insertUpdateRelationalTable.addRow(row1);

        RelationalTableRow row2 = new RelationalTableRow();
        row2.addColumn("Record Id", "22222");
        row2.addColumn("Purchase Date", "01/02/2015");
        insertUpdateRelationalTable.addRow(row2);

        String envelope = insertUpdateRelationalTable.envelope();

        final String expected = "<Envelope><Body><InsertUpdateRelationalTable>" +
                "<TABLE_ID>86767</TABLE_ID>" +
                "<ROWS>" +
                "<ROW><COLUMN name=\"Record Id\"><![CDATA[11111]]></COLUMN><COLUMN name=\"Purchase Date\"><![CDATA[01/01/2015]]></COLUMN></ROW>" +
                "<ROW><COLUMN name=\"Record Id\"><![CDATA[22222]]></COLUMN><COLUMN name=\"Purchase Date\"><![CDATA[01/02/2015]]></COLUMN></ROW>" +
                "</ROWS>" +
                "</InsertUpdateRelationalTable></Body></Envelope>";
        assertEquals(expected, envelope);
    }
}
