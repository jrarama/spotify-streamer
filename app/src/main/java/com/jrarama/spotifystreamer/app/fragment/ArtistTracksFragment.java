package com.jrarama.spotifystreamer.app.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.adapter.ArtistTrackAdapter;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.task.ArtistTracksFetcherTask;

import java.util.ArrayList;

public class ArtistTracksFragment extends Fragment {
    private static final String LOG_TAG = ArtistTracksFragment.class.getSimpleName();
    private ArtistTrackAdapter artistTrackAdapter;
    private ArrayList<TrackModel> mTracks;
    private static final String TRACKS_KEY = "tracks";
    private String mArtistId;
    private ListView tracksList;
    private TextView tracksText;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACKS_KEY, mTracks);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_UID);
        }

        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);
        } else {
            fetchTracks();
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

        tracksList = (ListView) rootView.findViewById(R.id.artist_tracks_list);
        tracksText = (TextView) rootView.findViewById(R.id.no_tracks_textview);

        if (savedInstanceState != null) {
            setViewVisibility();
        }

        tracksList.setAdapter(artistTrackAdapter);
        return rootView;
    }

    private void fetchTracks() {
        if (mArtistId != null) {
            new ArtistTrackFetcher().execute(mArtistId, getString(R.string.default_country_code));
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_invalid_artist_id), Toast.LENGTH_SHORT).show();
        }
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

    private void setViewVisibility() {
        boolean noTracks = mTracks == null || mTracks.isEmpty();
        tracksList.setVisibility(noTracks ? View.GONE : View.VISIBLE);
        tracksText.setVisibility(!noTracks ? View.GONE : View.VISIBLE);
    }

    class ArtistTrackFetcher extends ArtistTracksFetcherTask {

        @Override
        protected void onPostExecute(ArrayList<TrackModel> trackModels) {
            super.onPostExecute(trackModels);

            mTracks = trackModels;
            setViewVisibility();
            populateTracks();
        }
    }
}
