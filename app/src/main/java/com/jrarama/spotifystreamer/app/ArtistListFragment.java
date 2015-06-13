package com.jrarama.spotifystreamer.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jrarama.spotifystreamer.app.adapter.ArtistAdapter;
import com.jrarama.spotifystreamer.app.model.ArtistModel;
import com.jrarama.spotifystreamer.app.task.ArtistFetcherTask;

import java.util.ArrayList;

public class ArtistListFragment extends Fragment {

    private static final String TAG = ArtistListFragment.class.getSimpleName();
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
        return rootView;
    }

    private void attachArtistSearchEvent(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                new ArtistFetcher().execute(s.toString());
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
