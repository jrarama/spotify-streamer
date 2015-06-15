package com.jrarama.spotifystreamer.app;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Joshua on 14/6/2015.
 */
public class Utility {

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
}
