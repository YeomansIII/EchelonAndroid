package io.yeomans.echelon;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.yeomans.groupqueue.R;

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

       // view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
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

    public void ready(boolean isLeader) {
        if (!isLeader) {
            getView().findViewById(R.id.controlPlayButton).setVisibility(View.GONE);
        }
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
        }
//        else if (v == getView().findViewById(R.id.groupAddSongButton)) {
//            Log.d("Control", "add song 1");
//            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
//            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            if (groupFragment != null && groupFragment.isVisible()) {
//                Log.d("Control", "add song 2");
//                fragmentTransaction.replace(R.id.container, new SongSearchFragment(), "SEARCH_FRAG").addToBackStack(null).commit();
//            }
//        }
    }

    @Override
    public void onPlayerPlay() {
        ImageButton v = (ImageButton) getView().findViewById(R.id.controlPlayButton);
        v.setImageDrawable(getView().getResources().getDrawable(R.drawable.ic_pause_white_48dp));
        if (mCallback != null) {
            mCallback.onPlayControlSelected();
        }
    }

    @Override
    public void onPlayerPause() {
        ImageButton v = (ImageButton) getView().findViewById(R.id.controlPlayButton);
        v.setImageDrawable(getView().getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp));
        if (mCallback != null) {
            mCallback.onPauseControlSelected();
        }
    }

    public interface OnMediaControlSelectedListener {
        boolean onPlayControlSelected();

        boolean onPauseControlSelected();
    }
}
