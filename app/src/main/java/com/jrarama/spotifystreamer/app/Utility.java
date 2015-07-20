package com.jrarama.spotifystreamer.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;

import com.jrarama.spotifystreamer.app.activity.MainActivity;
import com.jrarama.spotifystreamer.app.model.TrackModel;

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

    public static void showNotification(Context context, List<TrackModel> trackModels, int currentTrack) {
        TrackModel track = trackModels.get(currentTrack);
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(track.getImageUrl()));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentText(track.getAlbumName());
        builder.setContentTitle(track.getTitle());
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        builder.addAction(android.R.drawable.ic_media_previous, "Previous", null);
        builder.addAction(android.R.drawable.ic_media_pause, "Pause", null);
        builder.addAction(android.R.drawable.ic_media_next, "Next", null);

        NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();
        mediaStyle.setShowActionsInCompactView(1);

        builder.setStyle(mediaStyle);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(9999, builder.build());

    }
}
