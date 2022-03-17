package de.ur.servus.eventcreationbottomsheet;

/*
 * Types for callbacks
 */
public class EventCreationData {
    // Don't forget to add properties to the toUpdateMap() method.
    public final String name;
    public final String description;
    public final String genre;
    public final String maxAttendees;

    EventCreationData(String name, String description, String genre, String maxAttendees) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.maxAttendees = maxAttendees;
    }

}
