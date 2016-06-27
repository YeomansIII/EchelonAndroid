package io.yeomans.echelon.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 7/13/15.
 */
public class ControlBarFragment extends Fragment implements View.OnClickListener, MainActivity.OnPlayerControlCallback {

    public MainActivity mainActivity;
    private OnMediaControlSelectedListener mCallback;
    private Intent playIntent = new Intent("io.yeomans.echelon.PLAY"),
            pauseIntent = new Intent("io.yeomans.echelon.PAUSE"),
            skipIntent = new Intent("io.yeomans.echelon.SKIP"),
            stopServiceIntent = new Intent("io.yeomans.echelon.STOP_SERVICE");
    private IntentFilter filter;

    @Bind(R.id.controlPlayButton)
    public ImageButton controlPlayButton;
    @Bind(R.id.controlSkipButton)
    public ImageButton controlSkipButton;

    private boolean playing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        filter = new IntentFilter();
        filter.addAction("io.yeomans.echelon.PLAYING");
        filter.addAction("io.yeomans.echelon.PAUSING");
        //playing = mainActivity.playerService.mPlayerPlaying;
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
        ButterKnife.bind(this, view);
        getContext().registerReceiver(receiver, filter);

        // view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
        controlPlayButton.setOnClickListener(this);
        controlSkipButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(receiver);
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

    public void unReady() {
        if (getView() != null) {
            getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("Control", "click");
        if (v.getId() == R.id.controlPlayButton) {
            Log.d("Control", "Play button clicked");
            if (!playing) {
                getContext().sendBroadcast(playIntent);
            } else {
                getContext().sendBroadcast(pauseIntent);
            }
        } else if (v.getId() == R.id.controlSkipButton) {
            getContext().sendBroadcast(skipIntent);
        }
    }

    @Override
    public void onPlayerPlay() {

    }

    @Override
    public void onPlayerPause() {

    }

    public interface OnMediaControlSelectedListener {
        boolean onPlayControlSelected();

        boolean onPauseControlSelected();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("io.yeomans.echelon.PLAYING")) {
                controlPlayButton.setImageDrawable(getView().getResources().getDrawable(R.drawable.ic_pause_white_48dp));
                playing = true;
            } else if (action.equals("io.yeomans.echelon.PAUSING")) {
                controlPlayButton.setImageDrawable(getView().getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp));
                playing = false;
            }
        }
    };
}
