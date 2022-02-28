package de.ur.servus.eventcreationbottomsheet;

import java.util.function.BiConsumer;

import de.ur.servus.core.Event;

public interface OnEditEventClickListener extends BiConsumer<Event, EventCreationData> {
}
