package de.ur.servus.eventGenres;

import java.util.ArrayList;
import java.util.List;

import de.ur.servus.R;

public class GenreData {
    public static List<Genre> getGenreList(){
        List<Genre> genreList = new ArrayList<>();

        Genre activity = new Genre();
        activity.setName("Activity");
        activity.setImage(R.drawable.ic_event_creation_genre_activity);
        genreList.add(activity);

        Genre food = new Genre();
        food.setName("Food");
        food.setImage(R.drawable.ic_event_creation_genre_food);
        genreList.add(food);

        Genre hangout = new Genre();
        hangout.setName("Hang-out");
        hangout.setImage(R.drawable.ic_event_creation_genre_hangout);
        genreList.add(hangout);

        Genre party = new Genre();
        party.setName("Party");
        party.setImage(R.drawable.ic_event_creation_genre_party);
        genreList.add(party);

        Genre sport = new Genre();
        sport.setName("Sport");
        sport.setImage(R.drawable.ic_event_creation_genre_sport);
        genreList.add(sport);

        return genreList;
    }
}
