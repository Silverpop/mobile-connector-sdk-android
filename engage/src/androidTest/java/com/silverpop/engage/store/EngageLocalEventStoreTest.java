package com.silverpop.engage.store;

import android.test.AndroidTestCase;

import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.domain.UBF;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageLocalEventStoreTest
        extends AndroidTestCase {

    private EngageLocalEventStore engageLocalEventStore;

    @Override
    protected void setUp() throws Exception{
        engageLocalEventStore = EngageLocalEventStore.get(getContext());
        engageLocalEventStore.open();

        //Delete all of the existing events.
        engageLocalEventStore.deleteAllUBFEvents();
    }

    public void testCountForEventType() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        engageLocalEventStore.saveUBFEvent(event);

        assertTrue(engageLocalEventStore.countForEventType(UBF.INSTALLED) == 1);
        assertTrue(engageLocalEventStore.countForEventType(UBF.SESSION_ENDED) == 0);
    }

    public void testDeleteAllUBFEvents() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        engageLocalEventStore.saveUBFEvent(event);

        assertEquals(1l, engageLocalEventStore.countForEventType(-1));
        engageLocalEventStore.deleteAllUBFEvents();
        assertEquals(0l, engageLocalEventStore.countForEventType(-1));
    }

    public void testFindEventByIdentifier() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        EngageEvent foundEvent = engageLocalEventStore.findEventByIdentifier(event.getId());
        assertTrue(event.getEventJson().equals(foundEvent.getEventJson()));
        assertTrue(event.getEventStatus() == foundEvent.getEventStatus());
        assertEquals(event.getEventType(), foundEvent.getEventType());
    }

    public void testFindEngageEventWithStatus() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        EngageEvent[] events = engageLocalEventStore.findEngageEventsWithStatus(event.getEventStatus());
        assertTrue(events != null && events.length == 1);

    }

    public void testFindUnpostedEvents() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.FAILED_POST);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        EngageEvent[] foundEvents = engageLocalEventStore.findEngageEventsWithStatus(EngageEvent.FAILED_POST);
        assertTrue(foundEvents != null && foundEvents.length == 1);

        event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.FAILED_POST);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        foundEvents = engageLocalEventStore.findEngageEventsWithStatus(EngageEvent.FAILED_POST);
        assertTrue(foundEvents != null && foundEvents.length == 2);
    }

    public void testDeleteExpiredLocalEvents() {
        EngageEvent event = new EngageEvent();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, -31);

        event.setEventDate(calendar.getTime());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);
        event = engageLocalEventStore.saveUBFEvent(event);

        assertTrue(engageLocalEventStore.findEventByIdentifier(event.getId()) != null);
        engageLocalEventStore.deleteExpiredLocalEvents();
        assertTrue(engageLocalEventStore.findEventByIdentifier(event.getId()) == null);
        assertTrue(engageLocalEventStore.countForEventType(EngageEvent.SUCCESSFULLY_POSTED) == 0);
    }

    public void testSaveUBFEvent() {
        EngageEvent event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);

        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() > -1);

        event = new EngageEvent();
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);

        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() > -1);

        event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);
        event.setEventType(UBF.INSTALLED);

        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() == -1);

        event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventType(UBF.INSTALLED);

        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() > -1);

        event = new EngageEvent();
        event.setEventDate(new Date());
        event.setEventJson(UBF.installed(getContext(), null).toJSONString());
        event.setEventStatus(EngageEvent.SUCCESSFULLY_POSTED);

        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() > -1);

        event = new EngageEvent();
        event = engageLocalEventStore.saveUBFEvent(event);
        assertTrue(event.getId() == -1);

        assertTrue(engageLocalEventStore.countForEventType(-1) == 4);
        engageLocalEventStore.deleteAllUBFEvents();
        assertTrue(engageLocalEventStore.countForEventType(-1) == 0);
    }

    @Override
    public void tearDown() throws Exception {
        engageLocalEventStore.close();
    }
}
