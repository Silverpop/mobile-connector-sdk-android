package com.silverpop.engage.domain;

import android.text.TextUtils;
import com.silverpop.engage.AnonymousMobileConnectorManager;

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

    public void addEmail(String email) {
        Map<String, Object> existing = getBodyElements();
        existing.put(XMLAPIElement.EMAIL.toString(), email);
    }

    public void addRecipientId(String recipientId) {
        Map<String, Object> existing = getBodyElements();
        existing.put(XMLAPIElement.RECIPIENT_ID.toString(), recipientId);
    }

    public void addParam(XMLAPIElement keyElement, Object value) {
        addParam(keyElement.toString(), value);
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
     *
     * @param cols
     */
    public void addColumns(Map<String, Object> cols) {
        this.addElements(cols, XMLAPIElement.COLUMNS.toString());
    }

    /**
     * Adds an additional column to the current list of columns.
     *
     * @param key
     * @param value
     */
    public void addColumn(String key, Object value) {
        Map<String, Object> existingElements = this.getBodyElements();
        Object existingColumns = existingElements.get(XMLAPIElement.COLUMNS.toString());
        if (existingColumns == null) {
            existingColumns = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> existingColumnsMap = (Map<String, Object>) existingColumns;
        // update current list with new column
        existingColumnsMap.put(key, value);
        // replace column list in case we created it as new
        addColumns(existingColumnsMap);
    }

    /**
     * Replaces the current sync fields with the ones provided.
     * @param syncFields
     */
    public void setSyncFields(Map<String, Object> syncFields) {
        Map<String, Object> existing = this.getBodyElements();
        existing.put(XMLAPIElement.SYNC_FIELDS.toString(), syncFields);
        this.setBodyElements(existing);
    }

    /**
     * Adds an additional sync field to the list of existing sync fields
     * @param key
     * @param value
     */
    public void addSyncField(String key, Object value) {
        Map<String, Object> existing = this.getBodyElements();
        Object existingSyncFieldsObject = existing.get(XMLAPIElement.SYNC_FIELDS.toString());
        if (existingSyncFieldsObject == null) {
            existingSyncFieldsObject = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> syncFields = (Map<String, Object>) existingSyncFieldsObject;
        syncFields.put(key, value);
        setSyncFields(syncFields);
    }


    /**
     * Replaces the current rows with the ones provided.
     *
     * @param tableRows
     */
    public void setRows(List<RelationalTableRow> tableRows) {
        Map<String, Object> existing = this.getBodyElements();
        existing.put(XMLAPIElement.ROWS.toString(), tableRows);
        this.setBodyElements(existing);

    }

    /**
     * Adds an additional row to the list of existing rows.
     *
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

        XMLAPI addRecipientXml = builder()
                .operation(XMLAPIOperation.ADD_RECIPIENT)
                .listId(listId)
                .column(mobileUserIdColumnName, mobileUserId)
                .build();

        if (updateIfFound) {
            addRecipientXml.addParam(XMLAPIElement.UPDATE_IF_FOUND, updateIfFound);
            // SYNC_FIELDS required if list has no unique identifier and UPDATE_IF_FOUND set to true
            addRecipientXml.addSyncField(mobileUserIdColumnName, mobileUserId);
        }

        return addRecipientXml;
    }

    private static Map<String, Object> nameValueMap(String name, Object value) {
        Map<String, Object> nameValueMap = new LinkedHashMap<String, Object>();
        nameValueMap.put(name, value);
        return nameValueMap;
    }


    public static XMLAPI updateRecipient(String recipientId, String listId) {
        XMLAPI updateRecipientXml = builder().operation(XMLAPIOperation.UPDATE_RECIPIENT)
                .recipientId(recipientId).listId(listId).build();
        return updateRecipientXml;
    }

    public static XMLAPI insertUpdateRelationalTable(String tableId) {
        XMLAPI xmlapi = new XMLAPI(XMLAPIOperation.INSERT_UPDATE_RELATIONAL_TABLE.toString(),
                nameValueMap(XMLAPIElement.TABLE_ID.toString(), tableId));
        return xmlapi;
    }

    /**
     * Method left here for backwards compatibility, but functionality has been moved to
     * {@link com.silverpop.engage.AnonymousMobileConnectorManager#addRecipientAnonymousToList(String)}
     * which you are encouraged to use instead.
     * @param listId
     * @return
     */
    public static XMLAPI addRecipientAnonymousToList(String listId) {
       return AnonymousMobileConnectorManager.addRecipientAnonymousToList(listId);
    }

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
            } else if (XMLAPIElement.ROWS.toString().equals(key)) {
                List<RelationalTableRow> rows = (List<RelationalTableRow>) element;
                this.appendRows(rows, body);
            } else if (XMLAPIElement.SYNC_FIELDS.toString().equals(key)) {
                Map<String, Object> innerDict = (Map<String, Object>) element;
                syncFields = this.buildInnerElementFromMapWithName(innerDict, syncFields, XMLAPIElement.SYNC_FIELD.toString());
            } else if (XMLAPIElement.CONTACT_LISTS.toString().equals(key)) {
                String[] contacts = (String[]) element;
                contactLists = buildContactsList(contactLists, contacts);
            } else {
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
     * @return StringBuilder containing nested structure.
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

    public static Builder builder() {
        return new Builder();
    }

    //[Lindsay Thurmond:1/12/15] TODO: finish building this out to replace all the static methods with various params
    public static class Builder {

        private String listId;
        private String email;
        private String recipientId;
        private XMLAPIOperation operation;
        private Map<String, Object> params;
        private Map<String, Object> columns;
        private List<RelationalTableRow> rows;
        private Map<String, Object> syncFields;

        public Builder() {
        }

        public Builder listId(String listId) {
            this.listId = listId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder recipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder operation(XMLAPIOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder param(String element, Object value) {
            if (params == null) {
                this.params = new LinkedHashMap<String, Object>();
            }
            this.params.put(element, value);
            return this;
        }

        public Builder param(XMLAPIElement element, Object value) {
           return param(element.toString(), value);
        }

        public Builder column(String key, Object value) {
            if (this.columns == null) {
                this.columns = new LinkedHashMap<String, Object>();
            }
            this.columns.put(key, value);
            return this;
        }

        public Builder row(RelationalTableRow relationalTableRow) {
            if (this.rows == null) {
                this.rows = new ArrayList<RelationalTableRow>();
            }
            this.rows.add(relationalTableRow);
            return this;
        }

        public Builder syncField(String key, Object value) {
            if (this.syncFields == null) {
                this.syncFields = new LinkedHashMap<String, Object>();
            }
            this.syncFields.put(key, value);
            return this;
        }

        public XMLAPI build() {
            XMLAPI xml = new XMLAPI(operation);
            if (!TextUtils.isEmpty(listId)) {
                xml.addListIdParam(listId);
            }
            if (!TextUtils.isEmpty(email)) {
                xml.addEmail(email);
            }
            if (!TextUtils.isEmpty(recipientId)) {
                xml.addRecipientId(recipientId);
            }
            if (params != null) {
                xml.addParams(params);
            }
            if (columns != null) {
                xml.addColumns(columns);
            }
            if (rows != null) {
                xml.setRows(rows);
            }

            return xml;
        }

    }

}
