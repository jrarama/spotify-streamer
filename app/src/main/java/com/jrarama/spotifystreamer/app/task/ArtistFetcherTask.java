package com.jrarama.spotifystreamer.app.task;

import android.os.AsyncTask;
import android.util.Log;

import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.model.ArtistModel;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;

/**
 * Created by Joshua on 14/6/2015.
 */
public class ArtistFetcherTask extends AsyncTask<String, Void, ArrayList<ArtistModel>> {

    private static final String LOG_TAG = ArtistFetcherTask.class.getSimpleName();
    private SpotifyService spotifyService;

    public ArtistFetcherTask() {
        spotifyService = new SpotifyApi().getService();
    }

    @Override
    protected ArrayList<ArtistModel> doInBackground(String... params) {
        ArtistsPager artistsPager = null;

        try {
            artistsPager = spotifyService.searchArtists(params[0]);

            Log.d(LOG_TAG, "Searching Artist: " + params[0]);
            if (artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null) {

                ArrayList<ArtistModel> artistModels = new ArrayList<>();
                Log.d(LOG_TAG, "Listing artists");
                for (Artist artist: artistsPager.artists.items) {
                    List<Image> images = artist.images;
                    String imageUrl = Utility.getImageUrlBySize(images, 64);
                    ArtistModel model = new ArtistModel(artist.id, artist.name, imageUrl);

                    Log.d(LOG_TAG, "Artist id: " + artist.id + ", Artist name: " + artist.name + ", Image Url: " + imageUrl);
                    artistModels.add(model);
                }

                return artistModels;
            }

        } catch (RetrofitError error) {
            if (error.getMessage().contains("400 Bad Request")) {
                Log.d(LOG_TAG, "No Artists found");
            } else {
                Log.e(LOG_TAG, "Error searching artists", error);
            }
        }
        return null;
    }
}
