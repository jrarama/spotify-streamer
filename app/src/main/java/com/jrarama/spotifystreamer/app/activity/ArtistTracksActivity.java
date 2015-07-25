package com.jrarama.spotifystreamer.app.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.fragment.ArtistTracksFragment;
import com.jrarama.spotifystreamer.app.fragment.TrackPlayerFragment;
import com.jrarama.spotifystreamer.app.model.TrackModel;

import java.util.ArrayList;

public class ArtistTracksActivity extends MusicServiceActivity implements ArtistTracksFragment.Callback {

    private static final String LOG_TAG = ArtistTracksActivity.class.getSimpleName();

    private String artistName;


    private boolean fromNotification;

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

        fromNotification = MainActivity.ACTION_FROM_NOTIFICATION.equals(intent.getAction());

        if (savedInstanceState == null) {
            showTracks(id, artistName, false);
        }
    }

    private void showTracks(String id, String artistName, boolean replace) {
        Bundle args = new Bundle();
        args.putString(ArtistTracksFragment.ARTIST_ID, id);
        args.putString(ArtistTracksFragment.ARTIST_NAME, artistName);

        ArtistTracksFragment fragment = new ArtistTracksFragment();
        fragment.setArguments(args);

        FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
        if(!replace) {
            manager.add(R.id.artist_tracks_container, fragment);
        } else {
            manager.replace(R.id.artist_tracks_container, fragment);
        }
        manager.commit();
    }

    @Override
    public void onTrackSelected(int position, String artistId, String artistName, ArrayList<TrackModel> tracks) {
        Intent intent = new Intent(this, TrackPlayerActivity.class);
        intent.putExtra(TrackPlayerFragment.ARTIST_NAME, artistName);
        intent.putExtra(TrackPlayerFragment.POSITION, position);
        intent.putExtra(TrackPlayerFragment.ARTIST_ID, artistId);
        intent.putParcelableArrayListExtra(TrackPlayerFragment.TRACKS, tracks);
        startActivity(intent);
    }

    @Override

    public void onBackPressed() {
        Log.d(LOG_TAG, "Back button is pressed");
        if (upFromNotification()) {
            return;
        }

        super.onBackPressed();
    }

    private boolean upFromNotification() {
        if (musicPlayerService == null || !fromNotification) return false;

        Intent intent = new Intent(this, MainActivity.class)
                .setAction(MainActivity.ACTION_FROM_NOTIFICATION);

        Log.d(LOG_TAG, "Starting new " + MainActivity.class.getSimpleName()) ;
        startActivity(intent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        int up = android.R.id.home;
        if (id == up) {
            Log.d(LOG_TAG, "Home button is pressed");
            if (upFromNotification()) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    void afterServiceConnected() {

    }

    @Override
    void afterServiceDisconnected() {

    }

    @Override
    void getBroadcastStatus(Intent intent) {

    }
}
