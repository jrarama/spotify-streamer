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
    private ArrayList<TrackModel> mTracks;
    private String mArtistId;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("tracks", mTracks);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList("tracks");
        } else {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
                fetchTracks();
            }
        }

        artistTrackAdapter = new ArtistTrackAdapter(
                getActivity(),
                R.layout.list_item_artist_track,
                mTracks != null ? mTracks : new ArrayList<TrackModel>()
        );

        artistTrackAdapter.setNotifyOnChange(false);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.artist_tracks_list);
        listView.setAdapter(artistTrackAdapter);

        return rootView;
    }

    private void fetchTracks() {
        new ArtistTrackFetcher().execute(mArtistId, getString(R.string.default_country_code));
    }

    private void populateTracks() {
        artistTrackAdapter.clear();

        if (mTracks != null) {
            for (TrackModel model: mTracks) {
                artistTrackAdapter.add(model);
            }
        }

        artistTrackAdapter.notifyDataSetChanged();
    }

    class ArtistTrackFetcher extends ArtistTracksFetcherTask {

        @Override
        protected void onPostExecute(ArrayList<TrackModel> trackModels) {
            super.onPostExecute(trackModels);
            mTracks = trackModels;
            populateTracks();
        }
    }
}
