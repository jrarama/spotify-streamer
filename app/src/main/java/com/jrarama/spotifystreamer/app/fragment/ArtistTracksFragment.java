package com.jrarama.spotifystreamer.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.adapter.ArtistTrackAdapter;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.task.ArtistTracksFetcherTask;

import java.util.ArrayList;

public class ArtistTracksFragment extends Fragment {

    private static final String LOG_TAG = ArtistTracksFragment.class.getSimpleName();
    private ArtistTrackAdapter artistTrackAdapter;
    private String mArtistId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        artistTrackAdapter = new ArtistTrackAdapter(
                getActivity(),
                R.layout.list_item_artist_track,
                new ArrayList<TrackModel>()
        );

        ListView listView = (ListView) rootView.findViewById(R.id.artist_tracks_list);

        artistTrackAdapter.setNotifyOnChange(false);
        listView.setAdapter(artistTrackAdapter);


        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            fetchTracks();
        }

        return rootView;
    }

    private void fetchTracks() {
        new ArtistTrackFetcher().execute(mArtistId, getString(R.string.default_country_code));
    }

    private void populateTracks(ArrayList<TrackModel> trackModels) {
        artistTrackAdapter.clear();

        if (trackModels != null) {
            for (TrackModel model: trackModels) {
                artistTrackAdapter.add(model);
            }
        }

        artistTrackAdapter.notifyDataSetChanged();
    }

    class ArtistTrackFetcher extends ArtistTracksFetcherTask {

        @Override
        protected void onPostExecute(ArrayList<TrackModel> trackModels) {
            super.onPostExecute(trackModels);

            populateTracks(trackModels);
        }
    }
}
