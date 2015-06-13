package com.jrarama.spotifystreamer.app.model;

/**
 * Created by Joshua on 13/6/2015.
 */
public class ArtistModel {

    private String imageUrl;

    private String name;

    public ArtistModel() {}

    public ArtistModel(String name, String imageUrl) {
        setName(name);
        setImageUrl(imageUrl);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
