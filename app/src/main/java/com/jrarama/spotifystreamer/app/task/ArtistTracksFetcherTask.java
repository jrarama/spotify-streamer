package com.jrarama.spotifystreamer.app.task;

import android.os.AsyncTask;
import android.util.Log;

import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.model.TrackModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Created by Joshua on 14/6/2015.
 */
public class ArtistTracksFetcherTask extends AsyncTask<String, Void, ArrayList<TrackModel>> {

    private static final String LOG_TAG = ArtistTracksFetcherTask.class.getSimpleName();
    private SpotifyService spotifyService;

    public ArtistTracksFetcherTask() {
        spotifyService = new SpotifyApi().getService();
    }

    @Override
    protected ArrayList<TrackModel> doInBackground(String... params) {
        final String artistId = params[0];
        final String country = params[1];

        Tracks tracks = null;

        try {
            Log.d(LOG_TAG, "Fetching tracks for Artist: " + artistId);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("country", country);

            tracks = spotifyService.getArtistTopTrack(artistId, paramMap);
            if (tracks != null && tracks.tracks != null) {
                ArrayList<TrackModel> trackModels = new ArrayList<>();
                Log.d(LOG_TAG, "Listing artists");
                for (Track track: tracks.tracks) {
                    String albumName = null;
                    String imageUrl = null;

                    if (track.album != null) {
                        albumName = track.album.name;
                        imageUrl = Utility.getImageUrlBySize(track.album.images, 64);
                    }

                    Log.d(LOG_TAG, "Track id: " + track.id + ", Track name: " + track.name +
                            ", Album name: " + albumName + ", Image Url: " + imageUrl +
                            ", Preview Url: " + track.preview_url);


                    TrackModel model = new TrackModel(track.id, track.name, albumName, imageUrl, track.preview_url);
                    trackModels.add(model);
                }

                return trackModels;
            }
        } catch (RetrofitError error) {
            if (error.getMessage().contains("400 Bad Request")) {
                Log.d(LOG_TAG, "Artist not found");
            } else {
                Log.e(LOG_TAG, "Error searching artist tracks", error);
            }
        }

        return null;
    }
}
