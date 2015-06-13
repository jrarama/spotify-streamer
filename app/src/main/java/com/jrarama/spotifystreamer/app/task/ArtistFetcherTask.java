package com.jrarama.spotifystreamer.app.task;

import android.os.AsyncTask;
import android.util.Log;

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

            if (artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null) {

                ArrayList<ArtistModel> artistModels = new ArrayList<>();
                for (Artist artist: artistsPager.artists.items) {
                    List<Image> images = artist.images;
                    ArtistModel model = new ArtistModel(artist.name,
                            !images.isEmpty() ? images.get(0).url : null);
                    artistModels.add(model);
                }

                return artistModels;
            }

        } catch (RetrofitError error) {
            Log.e(LOG_TAG, "Error searching artists", error);
            return null;
        }
        return null;
    }
}
