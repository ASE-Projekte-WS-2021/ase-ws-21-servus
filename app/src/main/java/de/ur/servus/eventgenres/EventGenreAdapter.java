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
    private Context context;
    private Genre[] genres;

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

        name.setText(genres[i].getName());
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

