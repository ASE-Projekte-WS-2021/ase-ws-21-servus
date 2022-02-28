package de.ur.servus.eventgenres;

import de.ur.servus.R;

public class GenreData {

    public static final Genre ACTIVITY = new Genre("Activity", R.drawable.ic_event_creation_genre_activity);
    public static final Genre FOOD = new Genre("Food", R.drawable.ic_event_creation_genre_food);
    public static final Genre HANGOUT = new Genre("Hang-out", R.drawable.ic_event_creation_genre_hangout);
    public static final Genre PARTY = new Genre("Party", R.drawable.ic_event_creation_genre_party);
    public static final Genre SPORT = new Genre("Sport", R.drawable.ic_event_creation_genre_sport);

    public static Genre[] allGenres = new Genre[]{
            ACTIVITY,
            FOOD,
            HANGOUT,
            PARTY,
            SPORT
    };
}
