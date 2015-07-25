package com.jrarama.spotifystreamer.app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.fragment.ArtistListFragment;
import com.jrarama.spotifystreamer.app.fragment.ArtistTracksFragment;
import com.jrarama.spotifystreamer.app.fragment.TrackPlayerFragment;
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

import java.util.ArrayList;


public class MainActivity extends MusicServiceActivity implements ArtistListFragment.Callback, ArtistTracksFragment.Callback, OnDismissListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean twoPane;
    private boolean dialogShown = false;
    private static final String ARTISTTRACKS_TAG = "tracks_list";
    private static final String PLAYER_TAG = "track_player";
    public static final String ACTION_FROM_NOTIFICATION = "from_notification";

    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenu;
    private boolean fromNotification = false;

    private void changeTrack(int currentTrack) {
        if (twoPane) {
            FragmentManager fm = getSupportFragmentManager();
            ArtistTracksFragment fragment = (ArtistTracksFragment) fm.findFragmentByTag(ARTISTTRACKS_TAG);
            fragment.setSelectedTrack(currentTrack);
        }
    }

    @Override
    void afterServiceConnected() {

        Log.d(LOG_TAG, "Service connected");
        musicPlayerService.setTwoPane(twoPane);

        Intent intent = getIntent();
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_FROM_NOTIFICATION.equals(action) && musicPlayerService != null) {
            fromNotification = true;
            TrackModel track = musicPlayerService.getCurrentTrackModel();
            if (track != null) {
                showTracksFragment(track.getId(), musicPlayerService.getArtistName(), musicPlayerService.getTrackModels());
                onTrackSelected(musicPlayerService.getCurrentTrack(), musicPlayerService.getArtistName(), musicPlayerService.getTrackModels());
            }
        } else {
            fromNotification = false;
        }
    }

    @Override
    void afterServiceDisconnected() {
        Log.d(LOG_TAG, "Service disconnected");
    }

    void getBroadcastStatus(Intent intent) {
        if (intent == null || musicPlayerService == null) return;
        MusicPlayerService.Status status = (MusicPlayerService.Status) intent.getSerializableExtra(MusicPlayerService.STATUS);
        int currentTrack = musicPlayerService.getCurrentTrack();
        Log.d(LOG_TAG, "Broadcast received: " + status.name());
        switch (status) {
            case CHANGETRACK:
                changeTrack(currentTrack);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTracksFragment(String id, String name, ArrayList<TrackModel> trackModels) {
        Bundle args = new Bundle();
        args.putString(ArtistTracksFragment.ARTIST_ID, id);
        args.putString(ArtistTracksFragment.ARTIST_NAME, name);
        args.putParcelableArrayList(ArtistTracksFragment.ARTIST_TRACKS, trackModels);

        ArtistTracksFragment fragment = new ArtistTracksFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.artist_tracks_container, fragment, ARTISTTRACKS_TAG)
                .commit();
    }

    @Override
    public void onArtistSelected(String id, String name) {
        if (!twoPane) {
            Intent intent = new Intent(this, ArtistTracksActivity.class)
                    .putExtra(Intent.EXTRA_TITLE, name)
                    .putExtra(Intent.EXTRA_UID, id);
            startActivity(intent);
        } else {
            showTracksFragment(id, name, null);
        }
    }

    @Override
    public void onArtistSearched(String queryString, ArrayList<ArtistModel> artistModels) {
        if (musicBound && musicPlayerService != null) {
            musicPlayerService.setQueryString(queryString);
            musicPlayerService.setArtistModels(artistModels);
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
