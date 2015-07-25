package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.adapter.ArtistTrackAdapter;
import com.jrarama.spotifystreamer.app.model.TrackModel;
import com.jrarama.spotifystreamer.app.task.ArtistTracksFetcherTask;

import java.util.ArrayList;

public class ArtistTracksFragment extends Fragment {
    public static final String ARTIST_ID = "artist_id";
    public static final String ARTIST_NAME = "artist_name";
    public static final String ARTIST_TRACKS = "artist_tracks";

    private static final String LOG_TAG = Utility.getLogTag(ArtistTracksFragment.class);

    private ArtistTrackAdapter artistTrackAdapter;
    private ListView tracksList;
    private TextView tracksText;


    private ArrayList<TrackModel> mTracks;
    private String mArtistId;
    private String mArtistName;

    public interface Callback {
        void onTrackSelected(int position, String artistName, ArrayList<TrackModel> tracks);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARTIST_TRACKS, mTracks);
        outState.putString(ARTIST_ID, mArtistId);
        outState.putString(ARTIST_NAME, mArtistName);
    }

    private boolean init(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mArtistId = args.getString(ARTIST_ID);
            mArtistName = args.getString(ARTIST_NAME);
            mTracks = args.getParcelableArrayList(ARTIST_TRACKS);
        } else {
            return false;
        }

        if (savedInstanceState != null && mTracks == null) {
            mTracks = savedInstanceState.getParcelableArrayList(ARTIST_TRACKS);
            mArtistId = savedInstanceState.getString(ARTIST_ID);
            mArtistName = savedInstanceState.getString(ARTIST_NAME);
        } else if (mTracks == null) {
            fetchTracks();
        }

        artistTrackAdapter = new ArtistTrackAdapter(
                getActivity(),
                R.layout.list_item_artist_track,
                mTracks != null ? mTracks : new ArrayList<TrackModel>()
        );

        artistTrackAdapter.setNotifyOnChange(false);

        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);
        if (!init(savedInstanceState)) return rootView;

        tracksList = (ListView) rootView.findViewById(R.id.artist_tracks_list);
        tracksText = (TextView) rootView.findViewById(R.id.no_tracks_textview);

        if (savedInstanceState != null) {
            setViewVisibility();
        }

        final Activity activity = getActivity();
        tracksList.setAdapter(artistTrackAdapter);
        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "Selected track no: " + position + ", Track name: " + mTracks.get(position).getTitle());
                Callback callback = (Callback) activity;
                if (callback == null) {
                    Log.e(LOG_TAG, "Parent activity does not implement " + Callback.class.getName());
                    return;
                }
                callback.onTrackSelected(position, mArtistName, mTracks);
            }
        });
        return rootView;
    }

    public void setSelectedTrack(int position) {
        if (mTracks == null) {
            Log.d(LOG_TAG, "No tracks listed");
            return;
        }
        Log.d(LOG_TAG, "Selecting track item orig: " + position);
        int pos = Utility.clamp(position, 0, mTracks.size() - 1);
        Log.d(LOG_TAG, "Selecting track item: " + pos);
        tracksList.setSelection(pos);
        tracksList.setItemChecked(pos, true);
        tracksList.smoothScrollToPosition(pos);
    }

    private void fetchTracks() {
        if (mArtistId != null) {
            new ArtistTrackFetcher().execute(mArtistId, Utility.getPreferredCountry(getActivity()));
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
