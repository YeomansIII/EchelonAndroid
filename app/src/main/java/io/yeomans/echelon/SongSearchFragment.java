package io.yeomans.echelon;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by jason on 7/10/15.
 */
public class SongSearchFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_search_fragment,
                container, false);

        view.findViewById(R.id.searchSongButton).setOnClickListener(this);

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == getActivity().findViewById(R.id.searchSongButton)) {
            Log.d("Button", "Song Search Button");
            searchSongs(((TextInputLayout) view.findViewById(R.id.searchSongEditWrapper)).getEditText().getText().toString());
        }
    }

    public void searchSongs(String query) {
        try {
            String spotifyTracksUrl = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query, "UTF-8") + "&type=track";
            FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
            ListSongFragment lsf = new ListSongFragment();
            Bundle bundle = new Bundle();
            bundle.putString("get_url", spotifyTracksUrl);
            lsf.setArguments(bundle);
            ft.replace(R.id.container, lsf, "SONG_LIST_FRAG").addToBackStack(null).commit();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}