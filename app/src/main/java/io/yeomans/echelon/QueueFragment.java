package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jason on 6/26/15.
 */
public class QueueFragment extends Fragment implements View.OnClickListener {

    private String playId;
    private boolean leader;
    public boolean isDestroyed;
    private boolean shouldExecuteOnResume;
    private SharedPreferences groupSettings;
    private SharedPreferences mainSettings;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    private MainActivity mainActivity;
    private ControlBarFragment controlBar;
    Firebase queuegroupRef;
    LinkedList<SpotifySong> playqueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        mainSettings = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);

        mainActivity = (MainActivity) getActivity();
        playqueue = mainActivity.playQueue;
        isDestroyed = false;
        shouldExecuteOnResume = false;

        queuegroupRef = mainActivity.myFirebaseRef.child("queuegroups").child(groupSettings.getString(MainActivity.PREF_GROUP_NAME, ""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.queue_fragment,
                container, false);

        //////////////
        ((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString(MainActivity.PREF_GROUP_OWNER_USERNAME, "error"));

        this.view = view;

        view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);

        queuegroupRef.child("tracks").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MyFirebase", "Track data changed!");
                playqueue.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Log.d("MyFirebase", "Creating SpotifySong with Track Data");
                    playqueue.add(dataSnapshot1.getValue(SpotifySong.class));
                }
                refreshQueueList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        });
        queuegroupRef.child("tracks").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("Tracks", "Child Changed!!");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
                RelativeLayout rt;
                if (curSong.isNowPlaying()) {
                    rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.now_playing_item, null);
                } else {
                    rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
                }
                ImageView albumArtImage = (ImageView) rt.findViewById(R.id.albumArtImage);
                TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
                TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);
                RelativeLayout voteControlsLayout = (RelativeLayout) rt.findViewById(R.id.songItemVoterLayout);

                if (!curSong.isNowPlaying()) {
                    voteControlsLayout.setVisibility(View.VISIBLE);
                    ImageButton voteUp = (ImageButton) rt.findViewById(R.id.voteSongUpButton);
                    ImageButton voteDown = (ImageButton) rt.findViewById(R.id.voteSongDownButton);
                    TextView rating = (TextView) rt.findViewById(R.id.songRatingText);

                    //Check if user has voted up or down
                    if (curSong.getVotedUp().containsKey(mainSettings.getString(MainActivity.PREF_FIREBASE_UID, null))) {
                        rating.setText("" + curSong.getRating());
                        voteUp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.setOnClickListener(null);
                                FirebaseCommon.rankSong(curSong.getKey(), 1, mainActivity);
                            }
                        });
                    }
                    voteDown.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setOnClickListener(null);
                            FirebaseCommon.rankSong(curSong.getKey(), -1, mainActivity);
                        }
                    });
                }

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
