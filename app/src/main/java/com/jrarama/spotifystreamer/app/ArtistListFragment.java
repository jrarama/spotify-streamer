package com.jrarama.spotifystreamer.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jrarama.spotifystreamer.app.adapter.ArtistAdapter;
import com.jrarama.spotifystreamer.app.model.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistListFragment extends Fragment {

    private ArtistAdapter artistsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

        artistsAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                getArtistList()
        );

        ListView listView = (ListView) rootView.findViewById(R.id.artist_list);
        listView.setAdapter(artistsAdapter);
        return rootView;
    }

    private List<Artist> getArtistList() {
        Uri imageUrl = Uri.parse("http://i.imgur.com/DvpvklR.png");
        ArrayList<Artist> arrayList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            arrayList.add(new Artist("Artist Name " + (i + 1), imageUrl));
        }

        return arrayList;
    }
}
