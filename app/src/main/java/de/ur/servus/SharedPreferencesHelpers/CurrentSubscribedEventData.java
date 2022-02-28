package de.ur.servus.SharedPreferencesHelpers;

import androidx.annotation.Nullable;

public class CurrentSubscribedEventData {
    @Nullable
    public final String eventId;

    public CurrentSubscribedEventData(@Nullable String eventId) {
        this.eventId = eventId;
    }
}
