package com.silverpop.engage.domain;

import java.util.*;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class XMLAPI {

    private String namedResource;
    private Map<String, Object> bodyElements;

    public XMLAPI(XMLAPIOperation operation) {
        this(operation.toString(), null);
    }

    public XMLAPI(XMLAPIOperation operation, Map<String, Object> bodyElements) {
        this(operation.toString(), bodyElements);
    }

    public XMLAPI(String resourceName) {
        this(resourceName, null);
    }

    public XMLAPI(String resourceName, Map<String, Object> bodyElements) {
        this.setNamedResource(resourceName);
        if (bodyElements == null) {
            bodyElements = new LinkedHashMap<String, Object>();
        }
        this.setBodyElements(bodyElements);
    }

    /**
     * Calling add elements with and existing elementName will result in all existing elements
     * with that same name being replaced. One should take care to make sure that all existing elements
     * are merged with the new ones before this is called. Refer to addSyncFields method for example
     * of this happening.
     *
     * @param elements
     * @param elementName
     */
    public void addElements(Map<String, Object> elements, String elementName) {
        Map<String, Object> existing = this.getBodyElements();
        existing.put(elementName, elements);
        this.setBodyElements(existing);
    }

    /**
     * Adds the parameters provided to the parent element. IF elements are already present in the
     * parent element they are overwritten.
     *
     * @param params
     */
    public void addParams(Map<String, Object> params) {
        Map<String, Object> existing = getBodyElements();
        existing.putAll(params);
        setBodyElements(existing);
    }

    public void addListIdParam(String listId) {
        Map<String, Object> existing = getBodyElements();
        existing.put(XMLAPIElement.LIST_ID.toString(), listId);
    }

    public void addParam(String key, Object value) {
        Map<String, Object> existing = getBodyElements();
        existing.put(key, value);
    }

    public void addSyncFields(Map<String, Object> fields) {
        Map<String, Object> existingSyncFields = (Map<String, Object>) this.getBodyElements().get(XMLAPIElement.SYNC_FIELDS.toString());
        if (existingSyncFields != null) {
            existingSyncFields.putAll(fields);
            this.addElements(existingSyncFields, XMLAPIElement.SYNC_FIELDS.toString());
        } else {
            this.addElements(fields, XMLAPIElement.SYNC_FIELDS.toString());
        }
    }

    public void addContactLists(String[] contactLists) {
        String[] existingContacts = (String[]) this.getBodyElements().get(XMLAPIElement.CONTACT_LISTS.toString());
        String[] merged = null;
        if (existingContacts != null) {
            merged = Arrays.copyOf(existingContacts, existingContacts.length + contactLists.length);
            System.arraycopy(contactLists, 0, merged, existingContacts.length, contactLists.length);
        } else {
            merged = contactLists;
        }

        Map<String, Object> existing = this.getBodyElements();
        existing.put(XMLAPIElement.CONTACT_LISTS.toString(), merged);
        setBodyElements(existing);
    }

    /**
     * Replaces the current columns list with the provided values.
     * @param cols
     */
    public void addColumns(Map<String, Object> cols) {
        this.addElements(cols, XMLAPIElement.COLUMNS.toString());
    }

    /**
     * Adds an additional column to the current list of columns.
     * @param key
     * @param value
     */
    public void addColumn(String key, Object value) {
        Map<String, Object> existingElements = this.getBodyElements();
        Object existingColumns = existingElements.get(XMLAPIElement.COLUMNS.toString());
        if (existingColumns == null) {
            existingColumns = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> existingColumnsMap =  (Map<String, Object>) existingColumns;
        // update current list with new column
        existingColumnsMap.put(key, value);
        // replace column list in case we created it as new
        addColumns(existingColumnsMap);
    }

    /**
     * Replaces the current rows with the ones provided.
     * @param tableRows
     */
    public void setRows(List<RelationalTableRow> tableRows) {
        Map<String, Object> existing = this.getBodyElements();
        existing.put(XMLAPIElement.ROWS.toString(), tableRows);
        this.setBodyElements(existing);

    }

    /**
     * Adds an additional row to the list of existing rows.
     * @param tableRow
     */
    public void addRow(RelationalTableRow tableRow) {
        Map<String, Object> existingElements = this.getBodyElements();
        Object existingRowsObject = existingElements.get(XMLAPIElement.ROWS.toString());
        if (existingRowsObject == null) {
            existingRowsObject = new ArrayList<RelationalTableRow>();
        }
        List<RelationalTableRow> existingRows = (List<RelationalTableRow>) existingRowsObject;
        existingRows.add(tableRow);
        setRows(existingRows);
    }


    public static XMLAPI selectRecipientData() {
        return new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);
    }

    public static XMLAPI selectRecipientData(String emailAddress, String listId) {
        XMLAPI api = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

        Map<String, Object> rd = new LinkedHashMap<String, Object>();
        rd.put(XMLAPIElement.LIST_ID.toString(), listId);
        rd.put(XMLAPIElement.EMAIL.toString(), emailAddress);

        Map<String, Object> in = new LinkedHashMap<String, Object>();
        in.put(XMLAPIElement.EMAIL.toString(), emailAddress);
        rd.put(XMLAPIElement.COLUMNS.toString(), in);

        api.setBodyElements(rd);
        return api;
    }

    public static XMLAPI addRecipientWithEmail(String emailAddress, String listId) {
        XMLAPI api = new XMLAPI(XMLAPIOperation.ADD_RECIPIENT);

        Map<String, Object> obs = new LinkedHashMap<String, Object>();
        obs.put(XMLAPIElement.LIST_ID.toString(), listId);

        Map<String, Object> emailCol = nameValueMap(XMLAPIElement.EMAIL.toString(), emailAddress);

        obs.put(XMLAPIElement.SYNC_FIELDS.toString(), emailCol);
        obs.put(XMLAPIElement.COLUMNS.toString(), emailCol);

        api.setBodyElements(obs);
        return api;
    }


    public static XMLAPI addRecipient(String mobileUserIdColumnName, String mobileUserId, String listId, boolean updateIfFound) {
        XMLAPI api = new XMLAPI(XMLAPIOperation.ADD_RECIPIENT);

        Map<String, Object> obs = new LinkedHashMap<String, Object>();
        obs.put(XMLAPIElement.LIST_ID.toString(), listId);
        obs.put(XMLAPIElement.UPDATE_IF_FOUND.toString(), updateIfFound);

        Map<String, Object> mobileUserIdDetails = nameValueMap(mobileUserIdColumnName, mobileUserId);

        obs.put(XMLAPIElement.SYNC_FIELDS.toString(), mobileUserIdDetails);
        obs.put(XMLAPIElement.COLUMNS.toString(), mobileUserIdDetails);

        api.setBodyElements(obs);
        return api;
    }

    private static Map<String, Object> nameValueMap(String name, Object value) {
        Map<String, Object> nameValueMap = new LinkedHashMap<String, Object>();
        nameValueMap.put(name, value);
        return nameValueMap;
    }


    public static XMLAPI updateRecipient(String recipientId, String listId) {
        Map<String, Object> bodyElements = new LinkedHashMap<String, Object>();
        bodyElements.put(XMLAPIElement.LIST_ID.toString(), listId);
        bodyElements.put(XMLAPIElement.RECIPIENT_ID.toString(), recipientId);

        XMLAPI api = new XMLAPI(XMLAPIOperation.UPDATE_RECIPIENT, bodyElements);
        return api;
    }

    public static XMLAPI insertUpdateRelationalTable(String tableId) {
        XMLAPI xmlapi = new XMLAPI(XMLAPIOperation.INSERT_UPDATE_RELATIONAL_TABLE.toString(),
                nameValueMap(XMLAPIElement.TABLE_ID.toString(), tableId));
        return xmlapi;
    }

    //[Lindsay Thurmond:1/6/15] TODO: enable, but throw exception?
//    public static XMLAPI addRecipientAnonymousToList(String listId) {
//        Map<String, Object> bodyElements = new LinkedHashMap<String, Object>();
//        bodyElements.put(XMLAPIElement.LIST_ID.toString(), listId);
//
//        XMLAPI api = new XMLAPI(XMLAPIOperation.ADD_RECIPIENT, bodyElements);
//        return api;
//    }

    public String envelope() {
        StringBuilder envelope = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder syncFields = new StringBuilder();
        StringBuilder contactLists = new StringBuilder();

        Map<String, Object> bodyElements = this.getBodyElements();
        for (String key : bodyElements.keySet()) {
            Object element = bodyElements.get(key);

            if (XMLAPIElement.COLUMNS.toString().equals(key)) {
                Map<String, Object> innerDict = (Map<String, Object>) element;
                body = this.buildInnerElementFromMapWithName(innerDict, body, XMLAPIElement.COLUMN.toString());
            }
            else if (XMLAPIElement.ROWS.toString().equals(key)) {
                List<RelationalTableRow> rows = (List<RelationalTableRow>) element;
                this.appendRows(rows, body);
            }
            else if (XMLAPIElement.SYNC_FIELDS.toString().equals(key)) {
                Map<String, Object> innerDict = (Map<String, Object>) element;
                syncFields = this.buildInnerElementFromMapWithName(innerDict, syncFields, XMLAPIElement.SYNC_FIELD.toString());
            } else if (XMLAPIElement.CONTACT_LISTS.toString().equals(key)) {
                String[] contacts = (String[]) element;
                contactLists = buildContactsList(contactLists, contacts);
            }  else {
                body.append("<");
                body.append(key);
                body.append(">");
                body.append(bodyElements.get(key));
                body.append("</");
                body.append(key);
                body.append(">");
            }
        }

        if (contactLists.length() > 0) {
            body.append("<" + XMLAPIElement.CONTACT_LISTS.toString() + ">");
            body.append(contactLists.toString());
            body.append("</" + XMLAPIElement.CONTACT_LISTS.toString() + ">");
        }

        if (syncFields.length() > 0) {
            body.append("<" + XMLAPIElement.SYNC_FIELDS.toString() + ">");
            body.append(syncFields.toString());
            body.append("</" + XMLAPIElement.SYNC_FIELDS.toString() + ">");
        }

        envelope.append("<Envelope><Body><");
        envelope.append(this.getNamedResource());
        envelope.append(">");
        envelope.append(body.toString());
        envelope.append("</");
        envelope.append(this.getNamedResource());
        envelope.append("></Body></Envelope>");

        return envelope.toString();
    }

    private StringBuilder buildContactsList(StringBuilder body, String[] contacts) {
        for (String contact : contacts) {
            body.append("<CONTACT_LIST_ID>");
            body.append(contact);
            body.append("</CONTACT_LIST_ID>");
        }
        return body;
    }

    /**
     * Utility for building nested XML internal structure.
     *
     * @param innerMap
     * @param body
     *
     * @return
     *      StringBuilder containing nested structure.
     */
    private StringBuilder buildInnerElementFromMapWithName(Map<String, Object> innerMap, StringBuilder body, String elementName) {
        for (String innerKey : innerMap.keySet()) {
            body.append("<").append(elementName).append(">");
            body.append("<NAME>").append(innerKey).append("</NAME>");
            body.append("<VALUE>").append(innerMap.get(innerKey)).append("</VALUE>");
            body.append("</").append(elementName).append(">");
        }
        return body;
    }

    private void appendRows(List<RelationalTableRow> tableRows, StringBuilder builder) {
        if (tableRows != null && !tableRows.isEmpty()) {
            builder.append("<").append(XMLAPIElement.ROWS.toString()).append(">");

            for (RelationalTableRow tableRow : tableRows) {
                builder.append("<").append(XMLAPIElement.ROW.toString()).append(">");
                for (RelationalTableRow.Column column : tableRow.getColumns()) {
                    //[Lindsay Thurmond:1/9/15] TODO: data formats?
                    builder.append("<COLUMN name=\"").append(column.getName()).append("\"><![CDATA[").append(column.getValue().toString()).append("]]></COLUMN>");
                }
                builder.append("</").append(XMLAPIElement.ROW.toString()).append(">");
            }
            builder.append("</").append(XMLAPIElement.ROWS.toString()).append(">");
        }
    }

    public String getNamedResource() {
        return namedResource;
    }

    public void setNamedResource(String namedResource) {
        this.namedResource = namedResource;
    }

    public Map<String, Object> getBodyElements() {
        return bodyElements;
    }

    public void setBodyElements(Map<String, Object> bodyElements) {
        this.bodyElements = bodyElements;
    }
}
