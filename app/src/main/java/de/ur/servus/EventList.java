package de.ur.servus;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.ur.servus.core.Event;

public class EventList {

    private List<Event> events = new ArrayList<>();

    public EventList() {
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEventsAttendedByUser(String userId) {
        return events.stream()
                .filter(e -> e.isUserAttending(userId))
                .collect(Collectors.toList());
    }

    public boolean isUserAttendingAnyEvent(String userId) {
        return getEventsAttendedByUser(userId).size() > 0;
    }

    public void ifUserIsAttendingEvents(String userId, @Nullable Consumer<List<Event>> then, @Nullable Runnable els) {
        var attendedEvents = events.stream().filter(e -> e.isUserAttending(userId)).collect(Collectors.toList());
        var attendedAnyEvent = attendedEvents.size() > 0;

        if (attendedAnyEvent) {
            if (then != null) {
                then.accept(attendedEvents);
            }
        } else {
            if (els != null) {
                els.run();
            }
        }
    }
}
