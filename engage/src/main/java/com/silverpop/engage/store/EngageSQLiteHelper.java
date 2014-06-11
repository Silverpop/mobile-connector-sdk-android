package com.silverpop.engage.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageSQLiteHelper
    extends SQLiteOpenHelper {

    public static final String TABLE_ENGAGE_EVENTS = "engageevents";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EVENT_TYPE = "eventType";
    public static final String COLUMN_EVENT_JSON = "eventJson";
    public static final String COLUMN_EVENT_STATUS = "eventStatus";
    public static final String COLUMN_EVENT_DATE = "eventDate";
    public static final String COLUMN_EVENT_FAILURE_COUNT = "eventFailureCount";

    public static final String DATABASE_NAME = "EngageTesting.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_ENGAGE_EVENTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_EVENT_TYPE
            + " int not null, " + COLUMN_EVENT_JSON + " text not null, " + COLUMN_EVENT_STATUS
            + " int not null, " + COLUMN_EVENT_DATE + " int not null, "
            + COLUMN_EVENT_FAILURE_COUNT + " int not null);";

    public EngageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(EngageSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ENGAGE_EVENTS);
        onCreate(sqLiteDatabase);
    }
}
