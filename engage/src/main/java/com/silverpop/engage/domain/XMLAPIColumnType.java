package com.silverpop.engage.domain;

/**
 * Created by Lindsay Thurmond on 12/31/14.
 */
public enum XMLAPIColumnType {

    TEXT_COLUMN(0, "TEXT"),
    YES_NO_COLUMN(1, "YESNO"),
    NUMERIC_COLUMN(2, "NUMERIC"),
    DATE_COLUMN(3, "DATE"),
    TIME_COLUMN(4, "TIME"),
    COUNTRY_COLUMN(5, "COUNTRY"),
    SELECT_ONE(6, "SELECTION"),
    EMAIL(7, "EMAIL"),
    SEGMENTING(8, ""),
    /**
     * used for defining EMAIL field only
     */
    SYSTEM(9, ""),
    SMS_OPT_IN(13, ""),
    SMS_OPTED_OUT_DATE(14, ""),
    SMS_PHONE_NUMBER(15, ""),
    PHONE_NUMBER(16, ""),
    TIMESTAMP(17, "DATE_TIME"),
    SYNC_ID(19, "SYNC_ID"),
    MULTI_SELECT(20, "");


    final int code;
    final String namedValue;

    XMLAPIColumnType(int code, String namedValue) {
        this.code = code;
        this.namedValue = namedValue;
    }

    /**
     * For requests that ask for column type as an int (example, AddListColumn)
     */
    public int code() {
        return code;
    }

    /**
     * Some requests ask for column type as a string name (example, CreateTable).
     * <p/>
     * If using this field verify that the column type you are using actually
     * has a namedValue defined.  If column type doesn't have a named value an
     * empty string is returned.
     */
    public String namedValue() {
        return namedValue;
    }
}
