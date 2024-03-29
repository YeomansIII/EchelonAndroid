package io.yeomans.echelon.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

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
        FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
        ListSongFragment lsf = new ListSongFragment();
        Bundle bundle = new Bundle();
        bundle.putChar("what", ListSongFragment.SEARCH);
        bundle.putString("searchQuery", query);
        lsf.setArguments(bundle);
        ft.replace(R.id.container, lsf, "SONG_LIST_FRAG").addToBackStack(null).commit();

    }
}