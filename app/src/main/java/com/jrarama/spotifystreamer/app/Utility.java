package com.jrarama.spotifystreamer.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jrarama.spotifystreamer.app.model.TrackModel;

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
}
