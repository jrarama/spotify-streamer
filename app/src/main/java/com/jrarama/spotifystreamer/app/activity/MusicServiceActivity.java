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

import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

/**
 * Created by joshua on 7/18/15.
 */
public abstract class MusicServiceActivity extends AppCompatActivity {

    protected MusicPlayerService musicPlayerService;

    private BroadcastReceiver receiver;
    protected boolean musicBound = false;

    private ServiceConnection trackServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            musicBound = true;
            afterServiceConnected();
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
        Intent playIntent = new Intent(this, MusicPlayerService.class);
        bindService(playIntent, trackServiceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getBroadcastStatus(intent);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter(MusicPlayerService.MESSAGE_TAG)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (trackServiceConnection != null) {
            unbindService(trackServiceConnection);
        }
        super.onDestroy();
    }
}
