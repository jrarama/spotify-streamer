package com.jrarama.spotifystreamer.app.model;

import android.net.Uri;

/**
 * Created by Joshua on 13/6/2015.
 */
public class Artist {

    private Uri imageUrl;

    private String name;

    public Artist() {}

    public Artist(String name, Uri imageUrl) {
        setName(name);
        setImageUrl(imageUrl);
    }

    public Uri getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
