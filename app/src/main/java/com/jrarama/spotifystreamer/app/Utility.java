package com.jrarama.spotifystreamer.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;

import com.jrarama.spotifystreamer.app.activity.MainActivity;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.service.MusicPlayerService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Joshua on 14/6/2015.
 */
public class Utility {

    private static final String LOG_PREFIX = "JprSpotify.";

    public static String getLogTag(Class<?> cls) {
        return LOG_PREFIX + cls.getSimpleName();
    }

    public static String getImageUrlBySize(List<Image> images, int size) {

        if (images == null || images.isEmpty()) return null;

        for (int i = images.size() - 1; i >=0; i--) {
            Image image = images.get(i);
            if (image.width >= size) {
                return image.url;
            }
        }

        return images.get(0).url;
    }

    public static int clamp(int number, int min, int max) {
        return Math.max(min, Math.min(max, number));
    }

    public static Intent createShareIntent(String artistName, TrackModel track) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_TEXT, "Listen to " + artistName + "'s song : " +
                track.getTitle() + " at " + track.getTrackUrl());
        return shareIntent;
    }

    public static String getPreferredCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_key),
                context.getString(R.string.pref_country_default));
    }

    public static boolean isPreferredNotifControls(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notif_key),
                Boolean.parseBoolean(context.getString(R.string.pref_notif_default)));
    }

    public static void showNotification(Context context, List<TrackModel> trackModels, int currentTrack, MusicPlayerService.Status status) {
        TrackModel track = trackModels.get(currentTrack);
        boolean playing = status == MusicPlayerService.Status.PLAYING;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentText(track.getAlbumName());
        builder.setContentTitle(track.getTitle());
        builder.setSmallIcon(!playing ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        builder.setOngoing(true);


        Intent prevIntent = new Intent(context, MusicPlayerService.class).setAction(MusicPlayerService.ACTION_PREVIOUS);
        Intent nextIntent = new Intent(context, MusicPlayerService.class).setAction(MusicPlayerService.ACTION_NEXT);
        Intent playIntent = new Intent(context, MusicPlayerService.class)
            .setAction(playing ? MusicPlayerService.ACTION_PAUSE : MusicPlayerService.ACTION_PLAY);

        builder.addAction(android.R.drawable.ic_media_previous, "Previous", PendingIntent.getService(context, 0, prevIntent, 0));
        if (playing) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", PendingIntent.getService(context, 0, playIntent, 0));
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Play", PendingIntent.getService(context, 0, playIntent, 0));
        }
        builder.addAction(android.R.drawable.ic_media_next, "Next", PendingIntent.getService(context, 0, nextIntent, 0));

        NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();
        mediaStyle.setShowActionsInCompactView(1);

        builder.setStyle(mediaStyle);

        Intent resultIntent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                showNotification(bitmap, builder, mNotificationManager);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                showNotification(null, builder, mNotificationManager);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(context).load(track.getImageUrl()).into(target);
    }

    private static void showNotification(Bitmap bitmap, NotificationCompat.Builder builder, NotificationManager manager) {
        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }
        manager.notify(9999, builder.build());
    }
}
