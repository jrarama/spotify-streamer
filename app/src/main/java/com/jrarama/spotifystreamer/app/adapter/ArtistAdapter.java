package com.jrarama.spotifystreamer.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.model.Artist;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Joshua on 14/6/2015.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {

    private Context context;
    private int layoutResource;
    private List<Artist> artists;

    static class ViewHolder {
        ImageView artistImage;
        TextView artistName;

    }

    public ArtistAdapter(Context context, int layoutResource, List<Artist> artists) {
        super(context, layoutResource, artists);

        this.context = context;
        this.layoutResource = layoutResource;
        this.artists = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResource, parent, false);

            holder = new ViewHolder();
            holder.artistImage = (ImageView) row.findViewById(R.id.artist_image);
            holder.artistName = (TextView) row.findViewById(R.id.artist_name);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Artist artist = artists.get(position);

        holder.artistName.setText(artist.getName());
        holder.artistImage.setImageURI(artist.getImageUrl());

        Picasso.with(context).load(artist.getImageUrl()).fit().into(holder.artistImage);
        return row;
    }
}
