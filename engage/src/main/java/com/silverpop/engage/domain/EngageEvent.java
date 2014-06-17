package com.silverpop.engage.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageEvent
    implements Serializable {

    public static final int NOT_READY_TO_POST = 0;
    public static final int READY_TO_POST = 1;
    public static final int SUCCESSFULLY_POSTED = 2;
    public static final int FAILED_POST = 3;
    public static final int EXPIRED = 4;  //EX. obtaining location information timed out. Events of this type will be posted

    private long id;
    private long eventType;
    private String eventJson;
    private long eventStatus;
    private Date eventDate;
    private int eventFailedPostCount;

    public EngageEvent() {}

    public EngageEvent(UBF ubfEvent) {
        this.eventType = ubfEvent.getCode();
        this.eventJson = ubfEvent.toJSONString();
        this.eventDate = ubfEvent.getEventTimestamp();
        this.eventStatus = NOT_READY_TO_POST;
        this.eventFailedPostCount = 0;
    }

    public EngageEvent(long id, long eventType, String eventJson, long eventStatus, Date eventDate, int eventFailedPostCount) {
        this.id = id;
        this.eventType = eventType;
        this.eventJson = eventJson;
        this.eventStatus = eventStatus;
        this.eventDate = eventDate;
        this.eventFailedPostCount = eventFailedPostCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEventType() {
        return eventType;
    }

    public void setEventType(long eventType) {
        this.eventType = eventType;
    }

    public String getEventJson() {
        return eventJson;
    }

    public void setEventJson(String eventJson) {
        this.eventJson = eventJson;
    }

    public long getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(long eventStatus) {
        this.eventStatus = eventStatus;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public int getEventFailedPostCount() {
        return eventFailedPostCount;
    }

    public void setEventFailedPostCount(int eventFailedPostCount) {
        this.eventFailedPostCount = eventFailedPostCount;
    }
}
