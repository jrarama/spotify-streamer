package com.jrarama.spotifystreamer.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Joshua on 11/7/2015.
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();
    public static final String MESSAGE_TAG = Status.class.getName();

    private MediaPlayer mediaPlayer;
    private LocalBroadcastManager broadcastManager;
    private static final String CURRENT_TRACK = "track";
    private static final String CURRENT_POSITION = "position";
    private String artistName;

    public static final String STATUS = "status";

    public enum Status {
        IDLE,
        INITIALIZED,
        PREPARED,
        PLAYING,
        PAUSED,
        STOPPED,
        COMPLETED,
        CHANGETRACK
    }

    private Status status;
    private ArrayList<TrackModel> trackModels;
    private int currentTrack;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Calling onCreate");
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        currentTrack = 0;
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    public void initMediaPlayer() {
        status = Status.IDLE;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void setTracks(ArrayList<TrackModel> tracks) {
        trackModels = tracks;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    pauseTrack();
                    break;
                case ACTION_PLAY:
                    playTrack();
                    break;
                case ACTION_NEXT:
                    setTrack(currentTrack + 1);
                    break;
                case ACTION_PREVIOUS:
                    setTrack(currentTrack - 1);
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");

        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        status = Status.IDLE;
        sendStatus();
        super.onDestroy();
    }

    public void sendStatus() {
        Intent intent = new Intent(MESSAGE_TAG);
        intent.putExtra(STATUS, status);
        intent.putExtra(CURRENT_TRACK, currentTrack);
        intent.putExtra(CURRENT_POSITION, mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0);

        manageNotification(getApplicationContext());
        broadcastManager.sendBroadcast(intent);
    }

    private void manageNotification(Context context) {
        switch (status) {
            case PLAYING:
            case PAUSED:
            case CHANGETRACK:
                Utility.showNotification(context, trackModels, currentTrack, status);
        }
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistName() {
        return artistName;
    }

    public Status getStatus() {
        return status;
    }

    public void setTrack(int index) {
        if (index == currentTrack && (status == Status.PLAYING || status == Status.PAUSED)) {
            Log.d(LOG_TAG, "Same track is set");
            sendStatus();
        } else {
            currentTrack = Utility.clamp(index, 0, trackModels.size() - 1);
            status = Status.CHANGETRACK;
            if (status == Status.PLAYING) {
                mediaPlayer.pause();
            }
            sendStatus();
            prepareTrack();
        }
    }

    public void seekTo(int miliSec) {
        mediaPlayer.seekTo(miliSec);
    }

    public int getCurrentTrack() {
        return currentTrack;
    }

    public TrackModel getCurrentTrackModel() {
        return trackModels.get(currentTrack);
    }

    public int getDuration() {
        if (mediaPlayer == null) return  0;
        switch (status) {
            case IDLE:
            case INITIALIZED:
                return 0;
            default:
                return mediaPlayer.getDuration();
        }
    }

    public int getTrackCurrentPosition() {
        if (mediaPlayer == null) return  0;
        switch (status) {
            case IDLE:
            case INITIALIZED:
                return 0;
            default:
                return mediaPlayer.getCurrentPosition();
        }
    }

    public void prepareTrack() {
        TrackModel track = trackModels.get(currentTrack);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.getTrackUrl()));
            status = Status.INITIALIZED;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting data source: " + track.getTrackUrl(), e);
        }
        mediaPlayer.prepareAsync();
    }

    public void playTrack() {
        if (status == Status.PREPARED || status == Status.PAUSED) {
            mediaPlayer.start();
            status = Status.PLAYING;
        } else if (status == Status.STOPPED || status == Status.COMPLETED) {
            prepareTrack();
        }
        sendStatus();
    }

    public void pauseTrack() {
        if (status == Status.PLAYING) {
            mediaPlayer.pause();
            status = Status.PAUSED;
        }
        sendStatus();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            mp.stop();
        } catch (IllegalStateException ex) {
            Log.d(LOG_TAG, "Media player stopped when not initialized", ex);
        }
        status = Status.COMPLETED;
        sendStatus();
        if (currentTrack + 1 < trackModels.size()) {
            setTrack(currentTrack + 1);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        status = Status.IDLE;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        status = Status.PREPARED;
        sendStatus();
        playTrack();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}
