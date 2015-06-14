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
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Joshua on 14/6/2015.
 */
public class ArtistTrackAdapter extends ArrayAdapter<TrackModel> {

    private Context context;
    private int layoutId;
    private List<TrackModel> trackModels;

    static class ViewHolder {
        ImageView albumImage;
        TextView tractName;
        TextView albumName;
    }

    public ArtistTrackAdapter(Context context, int resource, List<TrackModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutId = resource;
        this.trackModels = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);

            holder = new ViewHolder();
            holder.albumImage = (ImageView) row.findViewById(R.id.artist_track_image);
            holder.tractName = (TextView) row.findViewById(R.id.artist_track_name);
            holder.albumName = (TextView) row.findViewById(R.id.artist_track_album);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TrackModel trackModel = trackModels.get(position);

        holder.tractName.setText(trackModel.getTitle());
        holder.albumName.setText(trackModel.getAlbumName());

        if (trackModel.getImageUrl() != null) {
            Picasso.with(context).load(trackModel.getImageUrl()).fit().into(holder.albumImage);
        }
        return row;
    }
}
