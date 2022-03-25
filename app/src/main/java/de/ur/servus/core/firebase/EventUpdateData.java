package de.ur.servus.core.firebase;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class EventUpdateData {
    @Nullable
    public final String name;
    @Nullable
    public final String description;
    @Nullable
    public final String genre;
    @Nullable
    public final String maxAttendees;

    public EventUpdateData(@Nullable String name, @Nullable String description, @Nullable String genre, @Nullable String maxAttendees) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.maxAttendees = maxAttendees;
    }


    public Map<String, Object> toUpdateMap() {
        var updateMap = new HashMap<String, Object>();
        putIfNotNull(updateMap, "name", name);
        putIfNotNull(updateMap, "description", description);
        putIfNotNull(updateMap, "genre", genre);
        putIfNotNull(updateMap, "maxAttendees", maxAttendees);

        return updateMap;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value){
        if(value != null){
            map.put(key, value);
        }
    }
}
