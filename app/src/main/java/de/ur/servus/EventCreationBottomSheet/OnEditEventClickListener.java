package de.ur.servus.EventCreationBottomSheet;

import java.util.function.BiConsumer;

import de.ur.servus.core.Event;

public interface OnEditEventClickListener extends BiConsumer<Event, EventCreationData> {
}
