package io.yeomans.groupqueue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jason on 7/13/15.
 */
public class ControlBarFragment extends Fragment implements View.OnClickListener, MainActivity.OnPlayerControlCallback {

    public MainActivity mainActivity;
    private OnMediaControlSelectedListener mCallback;

    private boolean playing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        playing = false;
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

        view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
        view.findViewById(R.id.controlPlayButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void setOnMediaControlSelectedListener(OnMediaControlSelectedListener mCallback) {
        this.mCallback = mCallback;
        Log.d("Control", "set callback");
    }

    public void ready() {
        getView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        Log.d("Control", "click");
        if (v == getView().findViewById(R.id.controlPlayButton)) {
            if (mainActivity.playerReady) {

                //Log.d("Control", "playlist from ControlFrag: " + mCallback.playQueue);
                if (mainActivity.mPlayerPlaying) {
                    mainActivity.onPauseControlSelected();
                    //playing = false;
                } else {
                    mainActivity.onPlayControlSelected();
                    //playing = true;
                }
            }
        } else if (v == getView().findViewById(R.id.groupAddSongButton)) {
            Log.d("Control", "add song 1");
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (groupFragment != null && groupFragment.isVisible()) {
                Log.d("Control", "add song 2");
                fragmentTransaction.replace(R.id.container, new SongSearchFragment(), "SEARCH_FRAG").addToBackStack(null).commit();
            }
        }
    }

    @Override
    public void onPlayerPlay() {
//        CircleButton v = (CircleButton) getView().findViewById(R.id.controlPlayButton);
//        v.setImageResource(android.R.drawable.ic_media_pause);
        //v.setBackground(getResources().getDrawable(android.R.drawable.ic_media_pause));
    }

    @Override
    public void onPlayerPause() {
//        CircleButton v = (CircleButton) getView().findViewById(R.id.controlPlayButton);
//        v.setImageResource(android.R.drawable.ic_media_play);
        //v.setBackground(getResources().getDrawable(android.R.drawable.ic_media_play));
    }

    public interface OnMediaControlSelectedListener {
        boolean onPlayControlSelected();

        boolean onPauseControlSelected();
    }
}
