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

    private String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.artist_track_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        artistName = intent.getStringExtra(Intent.EXTRA_TITLE);
        String id = intent.getStringExtra(Intent.EXTRA_UID);
        toolbar.setSubtitle(artistName);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString(ArtistTracksFragment.ARTIST_ID, id);
            args.putString(ArtistTracksFragment.ARTIST_NAME, artistName);

            ArtistTracksFragment fragment = new ArtistTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_tracks_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onTrackSelected(int position, ArrayList<TrackModel> tracks) {
        Intent intent = new Intent(this, TrackPlayerActivity.class);
        intent.putExtra(TrackPlayerFragment.ARTIST_NAME, artistName);
        intent.putExtra(TrackPlayerFragment.POSITION, position);
        intent.putParcelableArrayListExtra(TrackPlayerFragment.TRACKS, tracks);
        startActivity(intent);
    }
}
