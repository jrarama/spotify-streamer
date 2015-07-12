package com.jrarama.spotifystreamer.app.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
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

    private MediaPlayer mediaPlayer;
    private ArrayList<TrackModel> trackModels;
    private int trackPos;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        trackPos = 0;
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    public void initMediaPlayer() {
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
        return false;
    }

    public void setTrack(int index) {
        trackPos = Math.max(0, Math.min(trackModels.size() - 1, index));
    }

    public void playTrack() {
        mediaPlayer.reset();
        TrackModel track = trackModels.get(trackPos);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.getTrackUrl()));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting data source: " + track.getTrackUrl(), e);
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
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
