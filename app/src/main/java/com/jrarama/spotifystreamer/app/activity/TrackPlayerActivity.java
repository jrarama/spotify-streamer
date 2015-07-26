package com.jrarama.spotifystreamer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.fragment.ArtistTracksFragment;
import com.jrarama.spotifystreamer.app.fragment.TrackPlayerFragment;
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

import java.util.ArrayList;

public class TrackPlayerActivity extends MusicServiceActivity {

    private static final String LOG_TAG = TrackPlayerActivity.class.getName();
    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenu;

    private String artistName;
    private TrackModel trackModel;


    private boolean fromNotification = false;

    @Override
    void afterServiceConnected() {
        setData();
    }

    @Override
    void afterServiceDisconnected() {

    }

    private void setData() {
        if (musicPlayerService != null) {
            artistName = musicPlayerService.getArtistName();
            trackModel = musicPlayerService.getCurrentTrackModel();
        }
    }

    @Override
    void getBroadcastStatus(Intent intent) {
        if (intent == null) return;
        MusicPlayerService.Status status = (MusicPlayerService.Status) intent.getSerializableExtra(MusicPlayerService.STATUS);

        setData();
        switch (status) {
            case PREPARED:
            case CHANGETRACK:
                setShareOptions();
                break;
        }
    }

    private void setShareOptions() {
        if (artistName == null || trackModel == null || mShareActionProvider == null) return;

        mShareActionProvider.setShareIntent(Utility.createShareIntent(
                artistName, trackModel
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.mipmap.ic_launcher);

        boolean hasSavedInstance = savedInstanceState != null;
        Log.d(LOG_TAG, "Saved Instance: " + hasSavedInstance);

        Intent intent = getIntent();
        fromNotification = MainActivity.ACTION_FROM_NOTIFICATION.equals(intent.getAction());

        if (savedInstanceState == null) {
            int position = intent.getIntExtra(TrackPlayerFragment.POSITION, 0);
            artistName = intent.getStringExtra(TrackPlayerFragment.ARTIST_NAME);
            ArrayList<TrackModel> tracks = intent.getParcelableArrayListExtra(TrackPlayerFragment.TRACKS);
            String id = intent.getStringExtra(TrackPlayerFragment.ARTIST_ID);
            trackModel = tracks.get(position);

            TrackPlayerFragment fragment = TrackPlayerFragment.newInstance(tracks, id, artistName, position, false);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_player_container, fragment)
                    .commit();
        }
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

        String artistName = musicPlayerService.getArtistName();
        String artistId = musicPlayerService.getArtistId();

        Intent intent = new Intent(this, ArtistTracksActivity.class)
                .setAction(MainActivity.ACTION_FROM_NOTIFICATION)
                .putExtra(Intent.EXTRA_TITLE, artistName)
                .putExtra(Intent.EXTRA_UID, artistId);

        Log.d(LOG_TAG, "Starting new " + ArtistTracksActivity.class.getSimpleName() +
                " with Artist Name: " + artistName + ", ArtistId: " + artistId) ;
        startActivity(intent);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_player, menu);
        mShareMenu = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenu);

        setShareOptions();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        int up = android.R.id.home;
        if (id == up) {
            Log.d(LOG_TAG, "Home button is pressed");
            if (upFromNotification()) {
                return true;
            }
        }

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "Resumed");
        setData();
        setShareOptions();
    }
}
