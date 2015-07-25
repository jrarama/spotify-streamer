package com.jrarama.spotifystreamer.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.R;
import com.jrarama.spotifystreamer.app.Utility;
import com.jrarama.spotifystreamer.app.activity.MainActivity;
import com.jrarama.spotifystreamer.app.adapter.ArtistAdapter;
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.jrarama.spotifystreamer.app.task.ArtistFetcherTask;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ArtistListFragment extends MusicServiceFragment {

    private static final String LOG_TAG = Utility.getLogTag(ArtistListFragment.class);

    private static final String ARTISTS_KEY = "artists";
    private static final String SEARCH_KEY = "q";
    private ArtistAdapter artistsAdapter;
    private ArrayList<ArtistModel> mArtistModels = null;
    private String mSearchArtist = null;
    private EditText searchBox;
    private boolean fromNotification = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARTISTS_KEY, mArtistModels);
        outState.putString(SEARCH_KEY, mSearchArtist);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (savedInstanceState != null) {
            mArtistModels = savedInstanceState.getParcelableArrayList(ARTISTS_KEY);
            mSearchArtist = savedInstanceState.getString(SEARCH_KEY);

            Log.d(LOG_TAG, "mSearchArtist: " + mSearchArtist);
        }

        artistsAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                mArtistModels != null ? mArtistModels : new ArrayList<ArtistModel>()
        );

        artistsAdapter.setNotifyOnChange(false);
        artistsAdapter.notifyDataSetChanged();

        populateFromIntent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.artist_list);
        searchBox = (EditText) rootView.findViewById(R.id.artist_search_text);

        searchBox.setText(mSearchArtist);

        listView.setAdapter(artistsAdapter);
        attachArtistSearchEvent(searchBox);
        attachOnItemClickEvent(listView);
        return rootView;
    }

    private void attachOnItemClickEvent(ListView listView) {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistModel artist = artistsAdapter.getItem(position);

                if (artist != null) {
                    Log.d(LOG_TAG, "Selected artist: " + artist.getId() + ", " + artist.getName());
                    Callback callback = (Callback) getActivity();
                    if (callback != null) {
                        callback.onArtistSelected(artist.getId(), artist.getName());
                    } else {
                        Log.e(LOG_TAG, "Parent activity does not implement " + Callback.class.getName());
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_no_artist_selected), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attachArtistSearchEvent(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long TEXT_CHANGE_DELAY = 500; // in ms

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (fromNotification && mArtistModels != null) return;
                mSearchArtist = s.toString();
                timer.cancel();

                if (mSearchArtist.length() == 0) {
                    mArtistModels = null;
                    populateArtists();
                    return;
                }

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new ArtistFetcher().execute(mSearchArtist);
                    }
                }, TEXT_CHANGE_DELAY);
            }
        });
    }

    private void populateArtists() {
        artistsAdapter.clear();

        if (mArtistModels != null) {
            for (ArtistModel model: mArtistModels) {
                artistsAdapter.add(model);
            }
        }

        artistsAdapter.notifyDataSetChanged();

        Callback callback = (Callback) getActivity();
        if (callback != null) {
            callback.onArtistSearched(mSearchArtist, mArtistModels);
        } else {
            Log.e(LOG_TAG, "Parent activity does not implement " + Callback.class.getName());
        }
    }

    private void populateFromIntent() {
        Intent intent = getActivity().getIntent();
        String action = intent != null ? intent.getAction() : null;

        if (MainActivity.ACTION_FROM_NOTIFICATION.equals(action) && musicPlayerService != null) {
            mSearchArtist = musicPlayerService.getQueryString();
            fromNotification = true;

            if (searchBox != null) {
                searchBox.setText(mSearchArtist);
            }
            mArtistModels = musicPlayerService.getArtistModels();
            populateArtists();
        } else {
            fromNotification = false;
        }
    }

    @Override
    void afterServiceConnected() {
        populateFromIntent();
    }

    @Override
    void afterServiceDisconnected() {

    }

    @Override
    void getBroadcastStatus(Intent intent) {

    }

    class ArtistFetcher extends ArtistFetcherTask {

        @Override
        protected void onPostExecute(ArrayList<ArtistModel> artistModels) {
            super.onPostExecute(artistModels);

            if (artistModels == null || artistModels.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.message_no_artist), Toast.LENGTH_SHORT).show();
            }

            mArtistModels = artistModels;
            populateArtists();
        }
    }

    public interface Callback {
        void onArtistSelected(String id, String name);

        void onArtistSearched (String queryString, ArrayList<ArtistModel> artistModels);
    }
}
