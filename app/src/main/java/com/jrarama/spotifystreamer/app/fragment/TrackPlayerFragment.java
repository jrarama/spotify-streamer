package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Joshua on 9/7/2015.
 */
public class TrackPlayerFragment extends DialogFragment {

    private static final String LOG_TAG = TrackPlayerFragment.class.getSimpleName();

    private MusicPlayerService musicPlayerService;
    private BroadcastReceiver receiver;
    private boolean musicBound = false;

    private ArrayList<TrackModel> trackModels;

    private int currentTrack = 0;
    private String artistName;
    private ViewHolder mHolder;
    private Timer mTimer;
    private MusicPlayerService.Status status;

    public static final String TRACKS = "tracks";
    public static final String POSITION = "position";
    public static final String ARTIST_NAME = "artist_name";
    public static final String TABLET = "tablet";

    private ServiceConnection trackServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.setTracks(trackModels);
            musicBound = true;

            setNextTrack(currentTrack);
            playMedia();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void bindService() {
        Activity activity = getActivity();
        Intent playIntent = new Intent(activity, MusicPlayerService.class);
        activity.bindService(playIntent, trackServiceConnection, Context.BIND_AUTO_CREATE);
        activity.startService(playIntent);
    }

    public static TrackPlayerFragment newInstance(ArrayList<TrackModel> tracks, String artistName, int currentTrack, boolean tablet) {
        TrackPlayerFragment fragment = new TrackPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TRACKS, tracks);
        args.putString(ARTIST_NAME, artistName);
        args.putInt(POSITION, currentTrack);
        args.putBoolean(TABLET, tablet);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
        return fragment;
    }

    public TrackPlayerFragment() {
    }

    private void getBroadcastStatus(Intent intent) {
        if (intent == null) return;
        status = (MusicPlayerService.Status) intent.getSerializableExtra(MusicPlayerService.STATUS);

        Log.d(LOG_TAG, "Broadcast received: " + status.name());
        switch (status) {
            case PREPARED:
                onPrepared();
                break;
            case PLAYING:
                playMediaReady();
                break;
            case PAUSED:
            case STOPPED:
            case COMPLETED:
                mediaPaused();
                break;
            case CHANGETRACK:
                onChangeTrack();
                break;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            trackModels = args.getParcelableArrayList(TRACKS);
            artistName = args.getString(ARTIST_NAME);
            currentTrack = args.getInt(POSITION, 0);
        }

        bindService();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getBroadcastStatus(intent);
            }
        };

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mHolder = new ViewHolder(rootView);

        mHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Starting media player");
                playMedia();
            }
        });

        mHolder.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Playing next track");
                setNextTrack(1);
                playMedia();
            }
        });

        mHolder.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Playing previous track");
                setNextTrack(-1);
                playMedia();
            }
        });

        setSeekBarEvent(mHolder.seekBar);
        return rootView;
    }

    private void playMedia() {
        switch (musicPlayerService.getStatus()) {
            case PLAYING:
                mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                musicPlayerService.pauseTrack();
                break;
            default:
                mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_play);
                musicPlayerService.playTrack();
                break;
        }
    }

    private void onChangeTrack() {
        Activity activity = getActivity();
        TrackModel track = trackModels.get(currentTrack);

        Log.d(LOG_TAG, "ArtistName: " + artistName + ", Position: " + currentTrack + ", Track: " +
                track.getTitle() + ", url: " + track.getTrackUrl());

        mHolder.albumName.setText(track.getAlbumName());
        mHolder.artistName.setText(artistName);
        mHolder.trackTitle.setText(track.getTitle());
        if (track.getImageUrl() != null) {
            Picasso.with(activity).load(track.getImageUrl()).fit().into(mHolder.trackImage);
        }
        String trackUrl = trackModels.get(currentTrack).getTrackUrl();

        setTimeLabels(0, 0);
        Log.d(LOG_TAG, "Changing track: " + trackUrl);
    }

    private void setNextTrack(int increment) {
        currentTrack += increment;
        currentTrack = Math.max(0, Math.min(trackModels.size() - 1, currentTrack));
        musicPlayerService.setTrack(currentTrack);
    }

    private void setSeekBarEvent(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "Seeking media position");
                musicPlayerService.seekTo(seekBar.getProgress());
            }
        });
    }

    private void playMediaReady() {
        int playPos = mHolder.seekBar.getProgress();
        Log.d(LOG_TAG, "Playing media at : " + playPos);

        musicPlayerService.seekTo(playPos);
        mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        final int duration = musicPlayerService.getDuration();

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final TimerTask task = this;
                if (status != MusicPlayerService.Status.PLAYING) {
                    boolean cancel = task.cancel();
                    if(!cancel) mTimer.cancel();
                    return;
                }

                final int curPos = musicPlayerService.getTrackCurrentPosition();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTimeLabels(duration, curPos);

                        if (curPos >= duration) {
                            pauseMedia();
                            setTimeLabels(duration, 0);
                            task.cancel();
                        }
                    }
                });
            }
        };
        mTimer.scheduleAtFixedRate(task, 0, 1000);
    }

    private void pauseMedia() {
        Log.d(LOG_TAG, "Pausing media");
        musicPlayerService.pauseTrack();
    }

    private void mediaPaused() {
        mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_play);
        mTimer.cancel();
    }

    private String formatSeconds(int sec) {
        int mins = sec / 60;
        int mod = sec % 60;

        return String.format("%d:%02d", mins, mod);
    }

    private void setTimeLabels(int duration, int curPos) {
        int sec = duration / 1000;
        int pos = curPos / 1000;

        mHolder.seekBar.setMax(duration);
        mHolder.seekBar.setProgress(curPos);
        mHolder.currentSec.setText(formatSeconds(pos));
        mHolder.duration.setText(formatSeconds(sec));


        Log.d(LOG_TAG, "Current Position: " + pos + ", Duration: " + sec);
    }

    private void initMediaPlayer() {
        Log.d(LOG_TAG, "Initializing media player.");
        int duration = musicPlayerService.getDuration();
        setTimeLabels(duration, 0);
    }

    public void onPrepared() {
        Log.d(LOG_TAG, "Media player is prepared.");
        initMediaPlayer();
        playMediaReady();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                receiver, new IntentFilter(MusicPlayerService.MESSAGE_TAG)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    class ViewHolder {
        private TextView artistName;
        private TextView albumName;
        private TextView trackTitle;
        private ImageView trackImage;
        private Button prevButton;
        private Button playButton;
        private Button nextButton;
        private SeekBar seekBar;
        private TextView currentSec;
        private TextView duration;

        public ViewHolder(View view) {
            artistName = (TextView) view.findViewById(R.id.player_artist_name);
            albumName = (TextView) view.findViewById(R.id.player_album_name);
            trackTitle = (TextView) view.findViewById(R.id.player_track_name);
            currentSec = (TextView) view.findViewById(R.id.player_seek_textview);
            duration = (TextView) view.findViewById(R.id.player_duration_textview);
            trackImage = (ImageView) view.findViewById(R.id.player_image);
            prevButton = (Button) view.findViewById(R.id.player_prev_btn);
            playButton = (Button) view.findViewById(R.id.player_play_btn);
            nextButton = (Button) view.findViewById(R.id.player_next_btn);
            seekBar = (SeekBar) view.findViewById(R.id.player_seekbar);
        }
    }
}
