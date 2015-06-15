package com.jrarama.spotifystreamer.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joshua on 13/6/2015.
 */
public class ArtistModel implements Parcelable {

    private String id;

    private String imageUrl;

    private String name;

    public ArtistModel(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        imageUrl = parcel.readString();
    }

    public ArtistModel(String id, String name, String imageUrl) {
        setId(id);
        setImageUrl(imageUrl);
        setName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
    }

    public static final Parcelable.Creator<ArtistModel> CREATOR = new Creator<ArtistModel>() {
        @Override
        public ArtistModel createFromParcel(Parcel source) {
            return new ArtistModel(source);
        }

        @Override
        public ArtistModel[] newArray(int size) {
            return new ArtistModel[size];
        }
    };
}
