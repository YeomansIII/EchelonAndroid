package io.yeomans.groupqueue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jason on 6/26/15.
 */
public class GroupFragment extends Fragment implements View.OnClickListener {

    private String playId;
    private boolean leader;
    public boolean isDestroyed;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();
        isDestroyed = false;

        playId = "";
        Bundle startingIntentBundle = this.getArguments();
        if (startingIntentBundle != null) {
            String[] extras = startingIntentBundle.getStringArray("extra_stuff");
            //playId = extras[0];
            leader = Boolean.parseBoolean(extras[1]);
            Log.wtf("Test", "leader: " + leader);
        }
        Log.wtf("Intent Extras", playId);
        if (leader) {
            ControlBarFragment controlBarFragment = (ControlBarFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG");
            if (controlBarFragment != null) {
                controlBarFragment.ready();
            }
            //((TextView) findViewById(R.id.syncRoom)).setText("Sync Room:" + playId);
//            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(MainActivity.CLIENT_ID,
//                    AuthenticationResponse.Type.TOKEN, MainActivity.REDIRECT_URI);
//            builder.setScopes(new String[]{"user-read-private", "streaming"});
//            AuthenticationRequest request = builder.build();
//            AuthenticationClient.openLoginActivity(getActivity(), MainActivity.REQUEST_CODE, request);
            //mainActivity.authenticateSpotify();
        } else {
            Log.d("Group", "Not leader");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_fragment,
                container, false);

        ///////
        SharedPreferences groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        ///////
        ((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString("group_owner_username", "error"));

        this.view = view;
//        BackendRequest be = new BackendRequest("GET", mainActivity);
//        BackendRequest.refreshGroupQueue(be);

        //view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (view != null) {
            // refreshQueueFromPref();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        BackendRequest be = new BackendRequest("GET", mainActivity);
        BackendRequest.refreshGroupQueue(be);
        //Log.d("Group","Group onResume()");
        //refreshQueueList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivity.mPlayer.pause();
        mainActivity.mPlayerCherry = true;
        isDestroyed = true;
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
                SpotifySong curSong = playqueue.get(i);

                RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
                ImageView albumArtImage = (ImageView) rt.findViewById(R.id.albumArtImage);
                TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
                TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);

                songTitleText.setText(curSong.getTitle());
                songArtistText.setText(curSong.getArtist());
                new ImageLoadTask(curSong.getAlbumArtSmall(), albumArtImage).execute();
                String uri = curSong.getUri();
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
            TextView tv = new TextView(getActivity().getApplicationContext());
            tv.setText("No songs in queue. Search for a song!");
            tv.setTextColor(Color.BLACK);
            songList.addView(tv);
        }
        Log.d("Play", "PlayQueueList End: " + playqueue);

    }

    @Override
    public void onClick(View v) {
//        if (v == view.findViewById(R.id.groupAddSongButton)) {
//            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
//            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            if (groupFragment != null && groupFragment.isVisible()) {
//                fragmentTransaction.detach(groupFragment).add(R.id.container, new SongSearchFragment(), "SEARCH_FRAG").commit();
//            }
//        }
    }
}
