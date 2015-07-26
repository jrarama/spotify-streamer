package com.jrarama.spotifystreamer.app.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

/**
 * Created by joshua on 7/18/15.
 */
public abstract class MusicServiceActivity extends AppCompatActivity {

    private static final String LOG_TAG = MusicServiceActivity.class.getSimpleName();
    protected MusicPlayerService musicPlayerService;

    private BroadcastReceiver receiver;
    protected boolean musicBound = false;
    private Intent playIntent;

    private ServiceConnection trackServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();

            if (musicPlayerService != null) {
                musicBound = true;
                afterServiceConnected();
            } else {
                bindService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
            afterServiceDisconnected();
        }
    };

    abstract void afterServiceConnected();

    abstract void afterServiceDisconnected();

    abstract void getBroadcastStatus(Intent intent);

    private void bindService() {
        playIntent = new Intent(this, MusicPlayerService.class);
        bindService(playIntent, trackServiceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    private boolean isPlaying() {
        return musicPlayerService != null &&
                musicPlayerService.getStatus() == MusicPlayerService.Status.PLAYING;
    }

    @Override
    public void onStart() {
        super.onStart();

        bindService();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getBroadcastStatus(intent);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter(MusicPlayerService.MESSAGE_TAG)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        try {
            if (trackServiceConnection != null) {
                unbindService(trackServiceConnection);
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Unable to unbind service");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (playIntent != null && !isPlaying()) {
            stopService(playIntent);
            Utility.cancelNotification(this);
        }
        super.onDestroy();
    }
}
