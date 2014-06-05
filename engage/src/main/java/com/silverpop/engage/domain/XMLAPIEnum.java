package com.silverpop.engage.domain;

/**
 * Created by jeremydyer on 5/19/14.
 */
public enum XMLAPIEnum {
    SYNC_FIELDS ("SYNC_FIELDS"),
    COLUMNS ("COLUMNS"),
    LIST_ID ("LIST_ID"),
    CONTACTS_LIST ("CONTACT_LISTS"),
    EMAIL ("EMAIL"),
    UPDATE_IF_FOUND ("UPDATE_IF_FOUND"),
    RECIPIENT_ID ("RECIPIENT_ID");

    private final String name;

    private XMLAPIEnum(String n) {
        name = n;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
        return name;
    }
}
