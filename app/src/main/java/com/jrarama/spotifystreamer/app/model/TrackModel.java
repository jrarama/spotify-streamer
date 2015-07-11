package com.jrarama.spotifystreamer.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joshua on 14/6/2015.
 */
public class TrackModel implements Parcelable {

    private String id;
    private String title;
    private String albumName;
    private String imageUrl;
    private String trackUrl;

    public TrackModel(String id, String title, String albumName, String imageUrl, String trackUrl) {
        setId(id);
        setTitle(title);
        setAlbumName(albumName);
        setImageUrl(imageUrl);
        setTrackUrl(trackUrl);
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

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.albumName);
        dest.writeString(this.imageUrl);
        dest.writeString(this.trackUrl);
    }

    public TrackModel(Parcel parcel) {
        this.id = parcel.readString();
        this.title = parcel.readString();
        this.albumName = parcel.readString();
        this.imageUrl = parcel.readString();
        this.trackUrl = parcel.readString();
    }

    public static Parcelable.Creator CREATOR = new Creator<TrackModel>() {
        @Override
        public TrackModel createFromParcel(Parcel source) {
            return new TrackModel(source);
        }

        @Override
        public TrackModel[] newArray(int size) {
            return new TrackModel[size];
        }
    };
}
