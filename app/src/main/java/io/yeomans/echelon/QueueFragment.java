package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jason on 6/26/15.
 */
public class QueueFragment extends Fragment implements View.OnClickListener {

    private String playId;
    private boolean leader;
    public boolean isDestroyed;
    private boolean shouldExecuteOnResume;
    private SharedPreferences groupSettings;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    private MainActivity mainActivity;
    private ControlBarFragment controlBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();
        isDestroyed = false;
        shouldExecuteOnResume = false;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.queue_fragment,
                container, false);

        ///////
        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        ///////
        ((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString(MainActivity.PREF_GROUP_OWNER_USERNAME, "error"));

        this.view = view;
//        BackendRequest be = new BackendRequest("GET", mainActivity);
//        BackendRequest.refreshGroupQueue(be);

        view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
        //controlBar.getView().findViewById(R.id.groupAddSongButton).setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (view != null) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        //if(shouldExecuteOnResume) {
        BackendRequest be = new BackendRequest("GET", mainActivity);
        BackendRequest.refreshGroupQueue(be);
        //} else {
        //    shouldExecuteOnResume = true;
        //}
        //Log.d("Group","Group onResume()");
        //refreshQueueList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getView().findViewById(R.id.groupAddSongButton).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshQueueList() {
        Log.d("RefreshQueue", "Refresh Queue List");

        //SharedPreferences pref = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        //String queue_json = pref.getString("group_current_queue_json", "");
        ArrayList<SpotifySong> playqueue = mainActivity.playQueue;

        Log.d("Play", "PlayQueueList: " + playqueue);

        if (playqueue.size() > 0) {
            //JSONObject json = new JSONObject(queue_json);
            //JSONArray items = json.getJSONArray("tracks");
            LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
            songList.removeAllViews();
            songListArr = new ArrayList<>();
            //boolean firstSong = false;
            //if(mainActivity.mPlayer.)
            //playqueue.clear();
            for (int i = 0; i < playqueue.size(); i++) {
                //JSONObject curObj = .getJSONObject(i);
                final SpotifySong curSong = playqueue.get(i);

                RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
                ImageView albumArtImage = (ImageView) rt.findViewById(R.id.albumArtImage);
                TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
                TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);
                RelativeLayout voteControlsLayout = (RelativeLayout) rt.findViewById(R.id.songItemVoterLayout);
                voteControlsLayout.setVisibility(View.VISIBLE);
                ImageButton voteUp = (ImageButton) rt.findViewById(R.id.voteSongUpButton);
                ImageButton voteDown = (ImageButton) rt.findViewById(R.id.voteSongDownButton);
                TextView rating = (TextView) rt.findViewById(R.id.songRatingText);

                rating.setText("" + curSong.getRating());
                voteUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            v.setOnClickListener(null);
                            JSONObject json = new JSONObject("{}");
                            json.put("pk", curSong.getPk());
                            json.put("vote", 1);
                            Log.d("SongVote", json.toString());
                            BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/update-song/", json.toString(), mainActivity);
                            BackendRequest.updateSong(be);
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                });
                voteDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            v.setOnClickListener(null);
                            JSONObject json = new JSONObject("{}");
                            json.put("pk", curSong.getPk());
                            json.put("vote", -1);
                            BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/update-song/", json.toString(), mainActivity);
                            BackendRequest.updateSong(be);
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                });

                songTitleText.setText(curSong.getTitle());
                songArtistText.setText(curSong.getArtist());
                new ImageLoadTask(curSong.getAlbumArtSmall(), albumArtImage).execute();
                //String uri = curSong.getUri();
                //mainActivity.playQueue.add(uri);
                rt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //trackUri = (String) v.getTag();
                        for (RelativeLayout view : songListArr) {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }
                        v.setBackgroundColor(Color.GRAY);
                    }
                });
                songListArr.add(rt);
                songList.addView(rt);
            }
        } else {
            LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
            songList.removeAllViews();
            TextView tv = new TextView(getActivity().getApplicationContext());
            tv.setText("No songs in queue. Search for a song!");
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            //tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            songList.addView(tv);
        }
        Log.d("Play", "PlayQueueList End: " + playqueue);

    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.groupAddSongButton)) {
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (groupFragment != null && groupFragment.isVisible()) {
                fragmentTransaction.replace(R.id.container, new AddSongFragment(), "ADD_SONG_FRAG").addToBackStack(null).commit();
            }
        }
    }
}
