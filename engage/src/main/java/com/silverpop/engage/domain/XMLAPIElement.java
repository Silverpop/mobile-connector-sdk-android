package com.silverpop.engage.domain;

/**
 * Created by jeremydyer on 5/19/14.
 */
public enum XMLAPIElement {
    SYNC_FIELDS ("SYNC_FIELDS"),
    SYNC_FIELD("SYNC_FIELD"),
    COLUMNS ("COLUMNS"),
    COLUMN("COLUMN"),
    LIST_ID ("LIST_ID"),
    CONTACT_LISTS("CONTACT_LISTS"),
    EMAIL ("EMAIL"),
    UPDATE_IF_FOUND ("UPDATE_IF_FOUND"),
    RECIPIENT_ID ("RECIPIENT_ID"),
    COLUMN_NAME("COLUMN_NAME"),
    COLUMN_TYPE("COLUMN_TYPE"),
    DEFAULT("DEFAULT"),
    ROWS("ROWS"),
    ROW("ROW"),
    TABLE_ID("TABLE_ID"),
    VISIBILITY("VISIBILITY"),
    LIST_TYPE("LIST_TYPE"),
    TABLE_NAME("TABLE_NAME"),
    CREATED_FROM("CREATED_FROM");

    private final String name;

    private XMLAPIElement(String n) {
        name = n;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
        return name;
    }
}
