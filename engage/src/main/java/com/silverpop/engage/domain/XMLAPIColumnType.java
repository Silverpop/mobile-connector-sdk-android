package com.silverpop.engage.domain;

/**
 * Created by Lindsay Thurmond on 12/31/14.
 */
public enum XMLAPIColumnType {

    TEXT_COLUMN(0),
    YES_NO_COLUMN(1),
    NUMERIC_COLUMN(2),
    DATE_COLUMN(3),
    TIME_COLUMN(4),
    COUNTRY_COLUMN(5),
    SELECT_ONE(6),
    SEGMENTING(8),
    SMS_OPT_IN(13),
    SMS_OPTED_OUT_DATE(14),
    SMS_PHONE_NUMBER(15),
    PHONE_NUMBER(16),
    TIMESTAMP(17),
    MULTI_SELECT(20);

    final int value;

    XMLAPIColumnType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
