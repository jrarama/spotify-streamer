package com.jrarama.spotifystreamer.app.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.fragment.ArtistListFragment;
import com.jrarama.spotifystreamer.app.fragment.ArtistTracksFragment;
import com.jrarama.spotifystreamer.app.fragment.TrackPlayerFragment;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

import java.util.ArrayList;


public class MainActivity extends MusicServiceActivity implements ArtistListFragment.Callback, ArtistTracksFragment.Callback, OnDismissListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean twoPane;
    private boolean dialogShown = false;
    private static final String ARTISTTRACKS_TAG = "tracks_list";
    private static final String PLAYER_TAG = "track_player";

    private void changeTrack(int currentTrack) {
        FragmentManager fm = getSupportFragmentManager();
        ArtistTracksFragment fragment = (ArtistTracksFragment) fm.findFragmentByTag(ARTISTTRACKS_TAG);
        fragment.setSelectedTrack(currentTrack);
    }

    @Override
    void afterServiceConnected() {
        Log.d(LOG_TAG, "Service connected");
    }

    @Override
    void afterServiceDisconnected() {
        Log.d(LOG_TAG, "Service disconnected");
    }

    void getBroadcastStatus(Intent intent) {
        if (intent == null) return;
        MusicPlayerService.Status status = (MusicPlayerService.Status) intent.getSerializableExtra(MusicPlayerService.STATUS);
        int currentTrack = musicPlayerService.getCurrentTrack();
        Log.d(LOG_TAG, "Broadcast received: " + status.name());
        switch (status) {
            case CHANGETRACK:
                changeTrack(currentTrack);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        if (findViewById(R.id.artist_tracks_container) != null) {
            twoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.artist_tracks_container, new ArtistTracksFragment(), ARTISTTRACKS_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onArtistSelected(String id, String name) {
        if (!twoPane) {
            Intent intent = new Intent(this, ArtistTracksActivity.class)
                    .putExtra(Intent.EXTRA_TITLE, name)
                    .putExtra(Intent.EXTRA_UID, id);
            startActivity(intent);
        } else {
            Bundle args = new Bundle();
            args.putString(ArtistTracksFragment.ARTIST_ID, id);
            args.putString(ArtistTracksFragment.ARTIST_NAME, name);

            ArtistTracksFragment fragment = new ArtistTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_tracks_container, fragment, ARTISTTRACKS_TAG)
                    .commit();
        }
    }

    @Override
    public void onTrackSelected(int position, String artistName, ArrayList<TrackModel> tracks) {
        if (dialogShown) {
            return;
        }
        dialogShown = true;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(PLAYER_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        TrackPlayerFragment dialog = TrackPlayerFragment.newInstance(tracks, artistName, position, true);
        dialog.show(fm, PLAYER_TAG);

        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicBound && musicPlayerService != null) {
            changeTrack(musicPlayerService.getCurrentTrack());
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialogShown = false;
    }
}
