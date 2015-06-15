package com.jrarama.spotifystreamer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jrarama.spotifystreamer.app.R;

public class ArtistTracksActivity extends AppCompatActivity {

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

}
