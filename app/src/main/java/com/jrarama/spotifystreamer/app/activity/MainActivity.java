package com.jrarama.spotifystreamer.app.activity;

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
    private MenuItem menuNowPlaying;

    private void changeTrack(int currentTrack) {
        if (twoPane) {
            FragmentManager fm = getSupportFragmentManager();
            ArtistTracksFragment fragment = (ArtistTracksFragment) fm.findFragmentByTag(ARTISTTRACKS_TAG);
            fragment.setSelectedTrack(currentTrack);
        }
    }

    @Override
    void afterServiceConnected() {
        if (musicPlayerService == null) return;

        Log.d(LOG_TAG, "Service connected");

        musicPlayerService.setTwoPane(twoPane);

        Intent intent = getIntent();
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_FROM_NOTIFICATION.equals(action)) {
            fromNotification = true;
            TrackModel track = musicPlayerService.getCurrentTrackModel();
            if (track != null && twoPane) {
                showTracksFragment(track.getId(), musicPlayerService.getArtistName(), musicPlayerService.getTrackModels(),
                        musicPlayerService.getCurrentTrack());
                onTrackSelected(musicPlayerService.getCurrentTrack(), musicPlayerService.getArtistId(),
                        musicPlayerService.getArtistName(), musicPlayerService.getTrackModels());
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
        if (intent == null || !musicBound || musicPlayerService == null) return;
        Utility.setNowPlayingMenuVisibility(musicPlayerService, menuNowPlaying);

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
        Intent intent =  Utility.createShareIntent(
                musicPlayerService.getArtistName(),
                musicPlayerService.getCurrentTrackModel()
        );
        
        if (intent != null && mShareMenu != null) {
            mShareMenu.setVisible(visible);
            mShareActionProvider.setShareIntent(intent);
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

        menuNowPlaying = menu.findItem(R.id.action_now_playing);

        if (musicPlayerService != null) {
            Utility.setNowPlayingMenuVisibility(musicPlayerService, menuNowPlaying);
        }
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
        } else if (id == R.id.action_now_playing) {

            if (musicPlayerService != null) {
                if (twoPane) {
                    onTrackSelected(musicPlayerService.getCurrentTrack(), musicPlayerService.getArtistId(),
                            musicPlayerService.getArtistName(), musicPlayerService.getTrackModels());
                } else {
                    Intent intent = Utility.getNowPlayingIntent(this, twoPane, musicPlayerService);
                    startActivity(intent);
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTracksFragment(String id, String name, ArrayList<TrackModel> trackModels, int currentTrack) {
        Bundle args = new Bundle();
        args.putString(ArtistTracksFragment.ARTIST_ID, id);
        args.putString(ArtistTracksFragment.ARTIST_NAME, name);
        args.putParcelableArrayList(ArtistTracksFragment.ARTIST_TRACKS, trackModels);
        args.putInt(ArtistTracksFragment.CURRENT_TRACK, currentTrack);

        ArtistTracksFragment fragment = new ArtistTracksFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.artist_tracks_container, fragment, ARTISTTRACKS_TAG)
                .commit();
    }

    @Override
    public void onArtistSelected(String id, String name) {
        if (musicBound && musicPlayerService != null) {
            musicPlayerService.setArtistId(id);
            musicPlayerService.setArtistName(name);
        }

        if (!twoPane) {
            Intent intent = new Intent(this, ArtistTracksActivity.class)
                    .putExtra(Intent.EXTRA_TITLE, name)
                    .putExtra(Intent.EXTRA_UID, id);
            startActivity(intent);
        } else {
            showTracksFragment(id, name, null, -1);
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
    public void onTrackSelected(int position, String artistId, String artistName, ArrayList<TrackModel> tracks) {
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

        TrackPlayerFragment dialog = TrackPlayerFragment.newInstance(tracks, artistId, artistName, position, true);
        dialog.show(fm, PLAYER_TAG);

        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicBound && musicPlayerService != null) {
            changeTrack(musicPlayerService.getCurrentTrack());
            setShareOptions();
            Utility.setNowPlayingMenuVisibility(musicPlayerService, menuNowPlaying);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialogShown = false;
    }

}
