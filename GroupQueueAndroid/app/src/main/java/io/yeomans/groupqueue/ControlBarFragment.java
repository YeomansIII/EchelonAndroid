package io.yeomans.groupqueue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by jason on 7/13/15.
 */
public class ControlBarFragment extends Fragment implements View.OnClickListener {

    public MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        //setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_bar_fragment,
                container, false);

        view.findViewById(R.id.controlPlayButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == getView().findViewById(R.id.controlPlayButton)) {
            Log.d("Play","playlist from ControlFrag: " + mainActivity.playQueue);
            if (mainActivity.playerReady) {
                if (!mainActivity.mPlayerPlaying && mainActivity.mPlayerCherry) {
                    mainActivity.playFirstSong();
                    v.setBackground(getActivity().getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else if (!mainActivity.mPlayerPlaying) {
                    mainActivity.mPlayer.resume();
                } else {
                    mainActivity.mPlayer.pause();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(),"Please log into Spotify",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
