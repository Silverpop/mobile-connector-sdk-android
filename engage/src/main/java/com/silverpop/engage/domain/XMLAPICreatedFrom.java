package com.silverpop.engage.domain;

/**
 * Created by Lindsay Thurmond on 1/14/15.
 */
public enum XMLAPICreatedFrom {

    IMPORTED_FROM_DATABASE(0),
    ADDED_MANUALLY(1),
    OPTED_IN(2),
    CREATED_FROM_TRACKING_DATABASE(3);

    final int value;

    XMLAPICreatedFrom(int value) {
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
