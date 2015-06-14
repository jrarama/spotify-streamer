package com.jrarama.spotifystreamer.app.model;

/**
 * Created by Joshua on 14/6/2015.
 */
public class TrackModel {

    private String id;
    private String title;
    private String albumName;
    private String imageUrl;

    public TrackModel(String id, String title, String albumName, String imageUrl) {
        setId(id);
        setTitle(title);
        setAlbumName(albumName);
        setImageUrl(imageUrl);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
