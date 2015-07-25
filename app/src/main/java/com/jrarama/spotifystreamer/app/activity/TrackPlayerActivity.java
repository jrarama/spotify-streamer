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
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

import java.util.ArrayList;

public class TrackPlayerActivity extends MusicServiceActivity {

    private static final String LOG_TAG = TrackPlayerActivity.class.getName();
    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenu;

    @Override
    void afterServiceConnected() {

    }

    @Override
    void afterServiceDisconnected() {

    }

    @Override
    void getBroadcastStatus(Intent intent) {
        if (intent == null) return;
        MusicPlayerService.Status status = (MusicPlayerService.Status) intent.getSerializableExtra(MusicPlayerService.STATUS);
        switch (status) {
            case PREPARED:
            case CHANGETRACK:
                setShareOptions();
                break;
        }
    }

    private void setShareOptions() {
        boolean visible = mShareActionProvider != null && musicPlayerService != null;
        if (mShareMenu != null) {
            mShareMenu.setVisible(visible);
        }

        if (visible) {
            mShareActionProvider.setShareIntent(Utility.createShareIntent(
                    musicPlayerService.getArtistName(),
                    musicPlayerService.getCurrentTrackModel()
            ));
        }
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

        if (savedInstanceState == null) {
            Intent intent = getIntent();

            int position = intent.getIntExtra(TrackPlayerFragment.POSITION, 0);
            String name = intent.getStringExtra(TrackPlayerFragment.ARTIST_NAME);
            ArrayList<TrackModel> tracks = intent.getParcelableArrayListExtra(TrackPlayerFragment.TRACKS);

            TrackPlayerFragment fragment = TrackPlayerFragment.newInstance(tracks, name, position, false);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_player_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_player, menu);
        mShareMenu = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
