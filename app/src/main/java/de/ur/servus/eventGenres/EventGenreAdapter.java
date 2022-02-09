package de.ur.servus.eventGenres;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.ur.servus.R;

public class EventGenreAdapter extends BaseAdapter {
    private Context context;
    private List<Genre> genreList;

    public EventGenreAdapter(Context context,List<Genre> genreList) {
        this.context = context;
        this.genreList = genreList;
    }

    @Override
    public int getCount() {
        return genreList != null ? genreList.size():0;
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

        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(context).inflate(R.layout.spinner_item_event_genre,viewGroup,false);
        TextView name = rootView.findViewById(R.id.event_spinner_genreName);
        ImageView image = rootView.findViewById(R.id.spinnerImage);

        name.setText(genreList.get(i).getName());
        image.setImageResource(genreList.get(i).getImage());

        return rootView;
    }
}

