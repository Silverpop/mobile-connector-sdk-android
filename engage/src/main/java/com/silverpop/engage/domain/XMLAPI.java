package com.silverpop.engage.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class XMLAPI {

    private String namedResource;
    private Map<String, Object> bodyElements;

    public XMLAPI(String resourceName) {
        this.setNamedResource(resourceName);
        this.setBodyElements(new HashMap<String, Object>());
    }

    public XMLAPI(String resourceName, Map<String, Object> bodyElements) {
        this.setNamedResource(resourceName);
        if (bodyElements == null) {
            bodyElements = new HashMap<String, Object>();
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

    public void addSyncFields(Map<String, Object> fields) {
        Map<String, Object> existingSyncFields = (Map<String, Object>) this.getBodyElements().get(XMLAPIEnum.SYNC_FIELDS.toString());
        if (existingSyncFields != null) {
            existingSyncFields.putAll(fields);
            this.addElements(existingSyncFields, XMLAPIEnum.SYNC_FIELDS.toString());
        } else {
            this.addElements(fields, XMLAPIEnum.SYNC_FIELDS.toString());
        }
    }

    public void addContactLists(String[] contactLists) {
        String[] existingContacts = (String[]) this.getBodyElements().get(XMLAPIEnum.CONTACTS_LIST.toString());
        String[] merged = null;
        if (existingContacts != null) {
            merged = Arrays.copyOf(existingContacts, existingContacts.length + contactLists.length);
            System.arraycopy(contactLists, 0, merged, existingContacts.length, contactLists.length);
        } else {
            merged = contactLists;
        }

        Map<String, Object> existing = this.getBodyElements();
        existing.put(XMLAPIEnum.CONTACTS_LIST.toString(), merged);
        setBodyElements(existing);
    }

    public void addColumns(Map<String, Object> cols) {
        this.addElements(cols, XMLAPIEnum.COLUMNS.toString());
    }

    public static XMLAPI selectRecipientData(String emailAddress, String listId) {
        XMLAPI api = new XMLAPI("SelectRecipientData");

        Map<String, Object> rd = new HashMap<String, Object>();
        rd.put(XMLAPIEnum.LIST_ID.toString(), listId);
        rd.put(XMLAPIEnum.EMAIL.toString(), emailAddress);

        Map<String, Object> in = new HashMap<String, Object>();
        in.put(XMLAPIEnum.EMAIL.toString(), emailAddress);
        rd.put(XMLAPIEnum.COLUMNS.toString(), in);

        api.setBodyElements(rd);
        return api;
    }

    public static XMLAPI addRecipient(String emailAddress, String listId) {
        XMLAPI api = new XMLAPI("AddRecipient");

        Map<String, Object> obs = new HashMap<String, Object>();
        obs.put(XMLAPIEnum.LIST_ID.toString(), listId);

        Map<String, Object> emailCol = new HashMap<String, Object>();
        emailCol.put(XMLAPIEnum.EMAIL.toString(), emailAddress);

        obs.put(XMLAPIEnum.SYNC_FIELDS.toString(), emailCol);
        obs.put(XMLAPIEnum.COLUMNS.toString(), emailCol);

        api.setBodyElements(obs);
        return api;
    }


    public static XMLAPI updateRecipient(String recipientId, String listId) {
        XMLAPI api = new XMLAPI("UpdateRecipient");

        Map<String, Object> obs = new HashMap<String, Object>();
        obs.put(XMLAPIEnum.LIST_ID.toString(), listId);
        obs.put(XMLAPIEnum.RECIPIENT_ID.toString(), recipientId);

        api.setBodyElements(obs);
        return api;
    }

    public static XMLAPI addRecipientAnonymousToList(String listId) {
        XMLAPI api = new XMLAPI("AddRecipient");

        Map<String, Object> obs = new HashMap<String, Object>();
        obs.put(XMLAPIEnum.LIST_ID.toString(), listId);

        api.setBodyElements(obs);
        return api;
    }


    public String envelope() {
        StringBuilder envelope = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder syncFields = new StringBuilder();
        StringBuilder contactLists = new StringBuilder();

        Map<String, Object> bodyElements = this.getBodyElements();
        Iterator<String> itr = bodyElements.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            Object element = bodyElements.get(key);

            if (key.equals(XMLAPIEnum.COLUMNS.toString())) {
                Map<String, Object> innerDict = (Map<String, Object>) element;
                body = this.buildInnerElementFromMapWithName(innerDict, body, "COLUMN");
            } else if (key.equals(XMLAPIEnum.SYNC_FIELDS.toString())) {
                Map<String, Object> innerDict = (Map<String, Object>) element;
                syncFields = this.buildInnerElementFromMapWithName(innerDict, syncFields, "SYNC_FIELD");
            } else if (key.equals(XMLAPIEnum.CONTACTS_LIST.toString())) {
                String[] contacts = (String[])element;
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
            body.append("<CONTACT_LISTS>");
            body.append(contactLists.toString());
            body.append("</CONTACT_LISTS>");
        }

        if (syncFields.length() > 0) {
            body.append("<SYNC_FIELDS>");
            body.append(syncFields.toString());
            body.append("</SYNC_FIELDS>");
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
        Iterator<String> innerItr = innerMap.keySet().iterator();
        while (innerItr.hasNext()) {
            String innerKey = innerItr.next();
            body.append("<");
            body.append(elementName);
            body.append(">");
            body.append("<NAME>");
            body.append(innerKey);
            body.append("</NAME>");
            body.append("<VALUE>");
            body.append(innerMap.get(innerKey));
            body.append("</VALUE>");
            body.append("</");
            body.append(elementName);
            body.append(">");
        }
        return body;
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
