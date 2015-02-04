package com.silverpop.engage.domain;

/**
 * Created by Lindsay Thurmond on 1/12/15.
 */
public enum XMLAPIListType {

    DATABASES(0),
    QUERIES(1),
    DATABASES_CONTACT_LISTS_AND_QUERIES(2),
    TEST_LISTS(5),
    SEED_LISTS(6),
    SUPPRESSION_LISTS(13),
    RELATIONAL_TABLES(15),
    CONTACT_LISTS(18);

    private int value;

    XMLAPIListType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
