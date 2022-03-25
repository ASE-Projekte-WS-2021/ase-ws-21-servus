package de.ur.servus.eventgenres;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import de.ur.servus.R;

public class EventGenreAdapter extends BaseAdapter {
    private final Context context;
    private final Genre[] genres;

    public EventGenreAdapter(Context context, @NonNull Genre[] genres) {
        this.context = context;
        this.genres = genres;
    }

    @Override
    public int getCount() {
        return genres.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        @SuppressLint("ViewHolder")
        View rootView = LayoutInflater.from(context).inflate(R.layout.spinner_item_event_genre, viewGroup, false);
        TextView name = rootView.findViewById(R.id.event_spinner_genreName);
        ImageView image = rootView.findViewById(R.id.spinnerImage);

        // Style visual name representation, not the actual genre name (for multilingualism)
        if (genres[i].getName().equals(GenreData.allGenres[0].getName())){
            name.setText(context.getResources().getString(R.string.event_creation_genre_hangout));
        } else if (genres[i].getName().equals(GenreData.allGenres[1].getName())){
            name.setText(context.getResources().getString(R.string.event_creation_genre_food));
        } else if (genres[i].getName().equals(GenreData.allGenres[2].getName())){
            name.setText(context.getResources().getString(R.string.event_creation_genre_party));
        } else if (genres[i].getName().equals(GenreData.allGenres[3].getName())){
            name.setText(context.getResources().getString(R.string.event_creation_genre_sport));
        } else {
            name.setText(context.getResources().getString(R.string.event_creation_genre_activity));
        }
        
        image.setImageResource(genres[i].getImage());

        return rootView;
    }

    public int getPositionFromName(String genreName) {
        var index = 0;
        for (var genre : genres) {
            if (genre.getName().equals(genreName)) {
                return index;
            }
            index++;
        }
        return 0;
    }
}

