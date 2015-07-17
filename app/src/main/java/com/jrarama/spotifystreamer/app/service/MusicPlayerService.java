package com.jrarama.spotifystreamer.app.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jrarama.spotifystreamer.app.model.TrackModel;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Joshua on 11/7/2015.
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();
    public static final String MESSAGE_TAG = Status.class.getName();

    private MediaPlayer mediaPlayer;
    private LocalBroadcastManager broadcastManager;
    private static final String CURRENT_TRACK = "track";
    private static final String CURRENT_POSITION = "position";

    public static final String STATUS = "status";

    public enum Status {
        IDLE,
        INITIALIZED,
        PREPARED,
        PLAYING,
        PAUSED,
        STOPPED,
        COMPLETED,
        NEWTRACK
    }

    private Status status;
    private ArrayList<TrackModel> trackModels;
    private int currentTrack;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
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
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        status = Status.IDLE;
        sendStatus();
        return false;
    }

    public void sendStatus() {
        Intent intent = new Intent(MESSAGE_TAG);
        intent.putExtra(STATUS, status);
        intent.putExtra(CURRENT_TRACK, currentTrack);
        intent.putExtra(CURRENT_POSITION, mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0);
        broadcastManager.sendBroadcast(intent);
    }

    public Status getStatus() {
        return status;
    }

    public void setTrack(int index) {
        currentTrack = Math.max(0, Math.min(trackModels.size() - 1, index));
        sendStatus();
    }

    public void nextTrack() {
        mediaPlayer.pause();
        setTrack(currentTrack + 1);
        prepareTrack();
        sendStatus();
    }

    public void prevTrack() {
        mediaPlayer.pause();
        setTrack(currentTrack - 1);
        prepareTrack();
        sendStatus();
    }

    public void seekTo(int miliSec) {
        mediaPlayer.seekTo(miliSec);
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
        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.getTrackUrl()));
                status = Status.INITIALIZED;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error setting data source: " + track.getTrackUrl(), e);
            }
            mediaPlayer.prepareAsync();
        } else {
            mediaPlayer.pause();
        }
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
        mp.stop();
        if (currentTrack + 1 >= trackModels.size()) {
            return;
        } else {
            nextTrack();
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
