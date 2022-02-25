package de.ur.servus.EventCreationBottomSheet;

import java.util.HashMap;
import java.util.Map;

/*
 * Types for callbacks
 */
public class EventCreationData {
    // Don't forget to add properties to the toUpdateMap() method.
    public final String name;
    public final String description;
    public final String genre;

    public static final EventCreationData EMPTY = new EventCreationData("", "", "");

    EventCreationData(String name, String description, String genre) {
        this.name = name;
        this.description = description;
        this.genre = genre;
    }

    public Map<String, Object> toUpdateMap(){
        var updateMap = new HashMap<String, Object>();
        updateMap.put("name", name);
        updateMap.put("description", description);
        updateMap.put("genre", genre);

        return updateMap;
    }
}
