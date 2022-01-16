package de.ur.servus.core;

import java.util.List;

public interface BackendHandler {
    ListenerRegistration subscribeEvents(EventListener<List<Event>> listener);
}
