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
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Joshua on 14/6/2015.
 */
public class ArtistAdapter extends ArrayAdapter<ArtistModel> {

    private Context context;
    private int layoutResource;
    private List<ArtistModel> artistModels;

    static class ViewHolder {
        ImageView artistImage;
        TextView artistName;

    }

    public ArtistAdapter(Context context, int layoutResource, List<ArtistModel> artistModels) {
        super(context, layoutResource, artistModels);

        this.context = context;
        this.layoutResource = layoutResource;
        this.artistModels = artistModels;
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

        ArtistModel artistModel = artistModels.get(position);

        holder.artistName.setText(artistModel.getName());

        if (artistModel.getImageUrl() != null) {
            Picasso.with(context).load(artistModel.getImageUrl()).fit().into(holder.artistImage);
        }
        return row;
    }
}
