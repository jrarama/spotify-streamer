package com.jrarama.spotifystreamer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.fragment.ArtistTracksFragment;
import com.jrarama.spotifystreamer.app.fragment.TrackPlayerFragment;
import com.jrarama.spotifystreamer.app.model.TrackModel;

import java.util.ArrayList;

public class ArtistTracksActivity extends AppCompatActivity implements ArtistTracksFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.artist_track_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra(Intent.EXTRA_TITLE);
            toolbar.setSubtitle(name);
        }
    }

    @Override
    public void onItemSelected(int position, String artistName, ArrayList<TrackModel> tracks) {
        Intent intent = new Intent(this, TrackPlayerActivity.class);
        intent.putExtra(TrackPlayerFragment.ARTIST_NAME, artistName);
        intent.putExtra(TrackPlayerFragment.POSITION, position);
        intent.putParcelableArrayListExtra(TrackPlayerFragment.TRACKS, tracks);
        startActivity(intent);
    }
}
