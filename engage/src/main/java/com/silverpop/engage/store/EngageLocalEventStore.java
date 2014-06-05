package com.silverpop.engage.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.EngageEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageLocalEventStore {

    private int DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION = 30;
    private Date oldEventsExpirationDate = null;
    private boolean connected = false;

    // Database fields
    private SQLiteDatabase database;
    private EngageSQLiteHelper dbHelper;
    private String[] allColumns = {
            EngageSQLiteHelper.COLUMN_ID,
            EngageSQLiteHelper.COLUMN_EVENT_TYPE,
            EngageSQLiteHelper.COLUMN_EVENT_JSON,
            EngageSQLiteHelper.COLUMN_EVENT_STATUS,
            EngageSQLiteHelper.COLUMN_EVENT_DATE};

    private static EngageLocalEventStore engageLocalEventStore = null;

    private EngageLocalEventStore(Context context) {
        dbHelper = new EngageSQLiteHelper(context);
        DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION = EngageConfigManager.get(context).expireLocalEventsAfterNumDays();
        setEventExpiration(DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION);

        if (!isConnected()) {
            try {
                open();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static EngageLocalEventStore get(Context context) {
        if (engageLocalEventStore == null) {
            engageLocalEventStore = new EngageLocalEventStore(context);
        }
        return engageLocalEventStore;
    }

    public EngageLocalEventStore(Context context, int daysBeforeExpiration) {
        this(context);
        DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION = daysBeforeExpiration;
        setEventExpiration(DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION);
    }

    private void setEventExpiration(int daysBeforeExpiration) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, -DEFAULT_DAYS_BEFORE_EVENT_EXPIRATION);
        oldEventsExpirationDate = calendar.getTime();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        if (database != null) {
            setConnected(true);
        }
    }

    public void close() {
        dbHelper.close();
        setConnected(false);
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Returns the number of events in the local table that are of the type specified.
     *
     * @param eventType
     *      IF event type is <= 0 then all events are counted in the table.
     *
     * @return
     *      Number of events found.
     */
    public long countForEventType(long eventType) {
        if (eventType <= 0) {
            //Count all of the rows in the table
            return DatabaseUtils.queryNumEntries(database, EngageSQLiteHelper.TABLE_ENGAGE_EVENTS);
        } else {
            //Only count the events with a particular status
            return DatabaseUtils.queryNumEntries(database, EngageSQLiteHelper.TABLE_ENGAGE_EVENTS,
                    "eventType=?", new String[]{new Long(eventType).toString()});
        }
    }

    public long countEventsReadyToPost() {
        return DatabaseUtils.queryNumEntries(database, EngageSQLiteHelper.TABLE_ENGAGE_EVENTS,
                "eventStatus=? OR eventStatus=?", new String[]{new Long(EngageEvent.EXPIRED).toString(), new Long(EngageEvent.READY_TO_POST).toString()});
    }

    public long countForStatus(long status) {
        if (status <= 0) {
            //Count rows with the required status.
            return DatabaseUtils.queryNumEntries(database, EngageSQLiteHelper.TABLE_ENGAGE_EVENTS,
                    "eventStatus=?", new String[]{new Long(status).toString()});
        }
        return 0;
    }

    public void deleteAllUBFEvents() {
        database.delete(EngageSQLiteHelper.TABLE_ENGAGE_EVENTS, null, null);
    }

    public EngageEvent findEventByIdentifier(long eventId) {
        EngageEvent event = null;
        Cursor cursor = database.rawQuery("select * from " + EngageSQLiteHelper.TABLE_ENGAGE_EVENTS +
                " where " + EngageSQLiteHelper.COLUMN_ID + " = " + eventId, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                event = cursorToEngageEvent(cursor);
            }
            cursor.close();
        }

        return event;
    }

    private EngageEvent cursorToEngageEvent(Cursor cursor) {
        EngageEvent event = new EngageEvent();
        event.setId(cursor.getLong(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_ID)));
        event.setEventType(cursor.getLong(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_EVENT_TYPE)));
        event.setEventJson(cursor.getString(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_EVENT_JSON)));
        event.setEventStatus(cursor.getLong(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_EVENT_STATUS)));
        event.setEventDate(new Date(cursor.getLong(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_EVENT_DATE))));
        event.setEventFailedPostCount(cursor.getInt(cursor.getColumnIndex(EngageSQLiteHelper.COLUMN_EVENT_FAILURE_COUNT)));
        return event;
    }

    /**
     * Returns all of the EngageEvents that have a have a particular status. Mostly
     * envisioned this would be used to find errored events. Keep in mind all events are
     * returned so mind your memory! The number should always be low but it is up to you to consider
     * that.
     *
     * @param eventStatus
     * @return
     */
    public EngageEvent[] findEngageEventsWithStatus(long eventStatus) {
        ArrayList<EngageEvent> events = new ArrayList<EngageEvent>();
        Cursor cursor = database.rawQuery("select * from " + EngageSQLiteHelper.TABLE_ENGAGE_EVENTS +
                " where " + EngageSQLiteHelper.COLUMN_EVENT_STATUS + " = " + eventStatus, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    events.add(cursorToEngageEvent(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }

        return events.toArray(new EngageEvent[events.size()]);
    }

    /**
     * Gathers all of the failed and unposted events.
     *
     * @return
     */
    public EngageEvent[] findUnpostedEvents() {
        ArrayList<EngageEvent> events = new ArrayList<EngageEvent>();
        Cursor cursor = database.rawQuery("select * from " + EngageSQLiteHelper.TABLE_ENGAGE_EVENTS +
                " where " + EngageSQLiteHelper.COLUMN_EVENT_STATUS +
                " = " + EngageEvent.READY_TO_POST + " OR "
                + EngageSQLiteHelper.COLUMN_EVENT_STATUS + " = " + EngageEvent.EXPIRED, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    events.add(cursorToEngageEvent(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }

        return events.toArray(new EngageEvent[events.size()]);
    }

    public void deleteExpiredLocalEvents() {
        database.delete(EngageSQLiteHelper.TABLE_ENGAGE_EVENTS,
                EngageSQLiteHelper.COLUMN_EVENT_DATE + "<= ?",
                new String[]{new Long(oldEventsExpirationDate.getTime()).toString()});
    }

    public void deleteEngageEventById(long engageEventId) {
        database.delete(EngageSQLiteHelper.TABLE_ENGAGE_EVENTS, EngageSQLiteHelper.COLUMN_ID
                + " = " + engageEventId, null);
    }

    public EngageEvent saveUBFEvent(EngageEvent event) {
        ContentValues values = new ContentValues();
        values.put(EngageSQLiteHelper.COLUMN_EVENT_TYPE, event.getEventType());
        values.put(EngageSQLiteHelper.COLUMN_EVENT_JSON, event.getEventJson());
        values.put(EngageSQLiteHelper.COLUMN_EVENT_STATUS, event.getEventStatus());
        if (event.getEventDate() == null) {
            event.setEventDate(new Date());
        }
        values.put(EngageSQLiteHelper.COLUMN_EVENT_DATE, event.getEventDate().getTime());
        values.put(EngageSQLiteHelper.COLUMN_EVENT_FAILURE_COUNT, event.getEventFailedPostCount());

        if (event != null && event.getId() > 0) {
            //Update the Event
            values.put(EngageSQLiteHelper.COLUMN_ID, event.getId());
            database.update(EngageSQLiteHelper.TABLE_ENGAGE_EVENTS, values,
                    EngageSQLiteHelper.COLUMN_ID + " = ?",
                    new String[]{new Long(event.getId()).toString()});
        } else {
            //Insert a new event.
            long insertId = database.insert(EngageSQLiteHelper.TABLE_ENGAGE_EVENTS, null,
                    values);

            event.setId(insertId);
        }

        return event;
    }

    public Date getEventExpirationDate() {
        return oldEventsExpirationDate;
    }

    public void setEventExpirationDate(Date eventExpirationDate) {
        this.oldEventsExpirationDate = eventExpirationDate;
    }
}
