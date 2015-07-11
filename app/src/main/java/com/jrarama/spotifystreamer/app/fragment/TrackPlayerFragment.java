package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Joshua on 9/7/2015.
 */
public class TrackPlayerFragment extends Fragment implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = TrackPlayerFragment.class.getSimpleName();

    public static final String TRACKS = "tracks";
    public static final String POSITION = "position";
    public static final String ARTIST_NAME = "artist_name";

    private ArrayList<TrackModel> trackModels;
    private int currentTrack = 0;
    private String artistName;
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean trackFinished;
    private ViewHolder mHolder;
    private Timer mTimer;

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "Media finished playing");
        trackFinished = true;
        setNextTrack(1);
        prepareTrack(true);
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

    public TrackPlayerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        if (intent != null) {
            trackModels = intent.getParcelableArrayListExtra(TRACKS);
            artistName = intent.getStringExtra(ARTIST_NAME);
            currentTrack = intent.getIntExtra(POSITION, 0);
        }

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mHolder = new ViewHolder(rootView);

        mediaPlayer = mediaPlayer != null ? mediaPlayer : new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnCompletionListener(this);

        setNextTrack(0);
        prepareTrack(false);
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
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
        return false;
    }
}
