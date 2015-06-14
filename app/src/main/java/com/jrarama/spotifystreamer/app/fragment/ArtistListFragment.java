package com.jrarama.spotifystreamer.app.fragment;

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
import com.jrarama.spotifystreamer.app.activity.ArtistTracksActivity;
import com.jrarama.spotifystreamer.app.adapter.ArtistAdapter;
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.jrarama.spotifystreamer.app.task.ArtistFetcherTask;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ArtistListFragment extends Fragment {

    private static final String LOG_TAG = ArtistListFragment.class.getSimpleName();
    private ArtistAdapter artistsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

        artistsAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                new ArrayList<ArtistModel>()
        );

        ListView listView = (ListView) rootView.findViewById(R.id.artist_list);
        EditText editText = (EditText) rootView.findViewById(R.id.artist_search_text);

        artistsAdapter.setNotifyOnChange(false);
        listView.setAdapter(artistsAdapter);

        attachArtistSearchEvent(editText);
        attachOnItemClickEvent(listView);
        return rootView;
    }

    private void attachOnItemClickEvent(ListView listView) {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistModel artist = artistsAdapter.getItem(position);

                if (artist != null) {
                    String artistId = artist.getId();
                    Log.d(LOG_TAG, "Selected artist: " + artistId + ", " + artist.getName());
                    Intent intent = new Intent(getActivity(), ArtistTracksActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, artistId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No artist selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attachArtistSearchEvent(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private Timer timer=new Timer();
            private final long TEXT_CHANGE_DELAY = 500; // in ms

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new ArtistFetcher().execute(s.toString());
                    }
                }, TEXT_CHANGE_DELAY);
            }
        });
    }

    private void populateArtists(ArrayList<ArtistModel> artistModels) {
        artistsAdapter.clear();

        if (artistModels != null) {
            for (ArtistModel model: artistModels) {
                artistsAdapter.add(model);
            }
        }

        artistsAdapter.notifyDataSetChanged();
    }

    class ArtistFetcher extends ArtistFetcherTask {

        @Override
        protected void onPostExecute(ArrayList<ArtistModel> artistModels) {
            super.onPostExecute(artistModels);

            if (artistModels == null || artistModels.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.message_no_artist), Toast.LENGTH_SHORT).show();
            }

            populateArtists(artistModels);
        }
    }
}
