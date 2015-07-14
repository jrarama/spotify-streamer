package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Joshua on 9/7/2015.
 */
public class TrackPlayerFragment extends DialogFragment implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = TrackPlayerFragment.class.getSimpleName();

    private MusicPlayerService musicPlayerService;
    private Intent playIntent;
    private boolean musicBound = false;

    private ArrayList<TrackModel> trackModels;

    private int currentTrack = 0;
    private String artistName;
    private MediaPlayer mediaPlayer;
    private boolean trackFinished;
    private ViewHolder mHolder;
    private Timer mTimer;

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void bindService() {
        Activity activity = getActivity();
        if (playIntent != null) {

            playIntent = new Intent(activity, MusicPlayerService.class);
            activity.bindService(playIntent, trackServiceConnection, Context.BIND_AUTO_CREATE);
            activity.startService(playIntent);
        }
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

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mHolder = new ViewHolder(rootView);

        mediaPlayer = mediaPlayer != null ? mediaPlayer : new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnCompletionListener(this);

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
                prepareTrack(true);
            }
        });

        mHolder.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Playing previous track");
                setNextTrack(-1);
                prepareTrack(true);
            }
        });

        setSeekBarEvent(mHolder.seekBar);
        setNextTrack(0);
        prepareTrack(true);
        return rootView;
    }

    private void setNextTrack(int increment) {
        currentTrack += increment;
        currentTrack = Math.max(0, Math.min(trackModels.size() - 1, currentTrack));
    }

    private void prepareTrack(boolean play) {
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

        Log.d(LOG_TAG, "Starting track: " + trackUrl);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(trackUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Track not found: " + trackUrl);
            Toast.makeText(activity, "Track file not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if (play) {
            playMedia();
        }
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
                mediaPlayer.seekTo(seekBar.getProgress() * 1000);
            }
        });
    }

    private void playMediaReady() {
        int playPos = mHolder.seekBar.getProgress() * 1000;
        Log.d(LOG_TAG, "Playing media at : " + playPos);

        mediaPlayer.seekTo(playPos);
        mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        final int duration = mediaPlayer.getDuration() / 1000;

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final TimerTask task = this;
                if (mediaPlayer == null) return;
                final int curPos = mediaPlayer.getCurrentPosition() / 1000;
                Log.d(LOG_TAG, "Current Position: " + curPos + ", Duration: " + duration);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHolder.seekBar.setProgress(curPos);
                        mHolder.currentSec.setText(formatSeconds(curPos));

                        if (curPos >= duration) {
                            task.cancel();
                            pauseMedia();
                            initMediaPlayer();
                        }
                    }
                });
            }
        };
        mTimer.scheduleAtFixedRate(task, 0, 500);
    }

    private void pauseMedia() {
        Log.d(LOG_TAG, "Pausing media");
        mediaPlayer.pause();
        mHolder.playButton.setBackgroundResource(android.R.drawable.ic_media_play);
        mTimer.cancel();
    }

    public void playMedia() {
        if (mediaPlayer.isPlaying()) {
            pauseMedia();
        } else {
            try {
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IllegalStateException ex) {

                Log.e(LOG_TAG, "Media player is already prepared. " + ex.getMessage());
                playMediaReady();
            }
        }
    }

    private String formatSeconds(int sec) {
        int mins = sec / 60;
        int mod = sec % 60;

        return String.format("%d:%02d", mins, mod);
    }


    @Override
    public void onSeekComplete(MediaPlayer mp) {
        /*if (!mp.isPlaying() && !trackFinished) {
            Log.d(LOG_TAG, "Not playing after seek. Playing media.");
            mp.start();
        }*/
    }

    private void initMediaPlayer() {
        Log.d(LOG_TAG, "Initializing media player.");
        int duration = mediaPlayer.getDuration();
        int sec = duration / 1000;

        mHolder.seekBar.setMax(sec);
        mHolder.seekBar.setProgress(0);
        mHolder.currentSec.setText(formatSeconds(0));
        mHolder.duration.setText(formatSeconds(sec));
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "Media player is prepared.");
        trackFinished = false;
        initMediaPlayer();
        mediaPlayer.start();
        playMediaReady();
    }

    @Override
    public void onPause() {
        if(mTimer != null) {
            mTimer.cancel();
        }
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "Media finished playing");
        trackFinished = true;

        if (currentTrack + 1 < trackModels.size()) {
            setNextTrack(1);
            prepareTrack(true);
        }
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
