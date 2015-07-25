package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.jrarama.spotifystreamer.app.service.MusicPlayerService;

/**
 * Created by joshua on 7/18/15.
 */
public abstract class MusicServiceFragment extends Fragment {

    protected MusicPlayerService musicPlayerService;

    private BroadcastReceiver receiver;
    protected boolean musicBound = false;
    private Intent playIntent;

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
        Activity activity = getActivity();
        playIntent = new Intent(activity, MusicPlayerService.class);
        activity.bindService(playIntent, trackServiceConnection, Context.BIND_AUTO_CREATE);
        activity.startService(playIntent);
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                receiver, new IntentFilter(MusicPlayerService.MESSAGE_TAG)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);

        if (trackServiceConnection != null) {
            getActivity().unbindService(trackServiceConnection);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (playIntent != null && !isPlaying()) {
            getActivity().stopService(playIntent);
        }
        super.onDestroy();
    }
}
