package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    ValueEventListener trackListChangeListener;

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

        queuegroupRef = mainActivity.myFirebaseRef.child("queuegroups/" + groupSettings.getString(MainActivity.PREF_GROUP_NAME, ""));
        trackListChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MyFirebase", "Track data changed!");
                playqueue.clear();
                SpotifySong nowPlayingSS = null;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    SpotifySong ss = dataSnapshot1.getValue(SpotifySong.class);
                    if (!ss.isNowPlaying() && !ss.isPlayed()) {
                        playqueue.add(ss);
                    } else if (ss.isNowPlaying()) {
                        nowPlayingSS = ss;
                    }
                    //Log.d("MyFirebase", "Song added: " + playqueue.getLast().getAdded());
                }
                Collections.sort(playqueue);
                if (nowPlayingSS != null) {
                    playqueue.addFirst(nowPlayingSS);
                }
                refreshQueueList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };
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

        queuegroupRef.child("tracks").addValueEventListener(trackListChangeListener);
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
        queuegroupRef.child("tracks").removeEventListener(trackListChangeListener);
        //getView().findViewById(R.id.groupAddSongButton).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshQueueList() {
        Log.d("RefreshQueue", "Refresh Queue List");
        if (getActivity() != null) {
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
                        TextView ratingText = (TextView) rt.findViewById(R.id.songRatingText);

                        String fUid = mainSettings.getString(MainActivity.PREF_FIREBASE_UID, null);
                        Log.d("RefreshList", fUid);
                        //Check if user has voted up or down
                        Map<String, Object> votedUp = curSong.getVotedUp();
                        if (votedUp == null || !votedUp.containsKey(fUid)) {
                            voteUp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.setOnClickListener(null);
                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
                                    FirebaseCommon.rankSong(curSong.getKey(), 1, mainActivity);
                                }
                            });
                        } else {
                            voteUp.setImageResource(R.drawable.ic_chevron_up_gold_48dp);
                            voteUp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.setOnClickListener(null);
                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
                                }
                            });
                        }
                        Map<String, Object> votedDown = curSong.getVotedDown();
                        if (votedDown == null || !votedDown.containsKey(fUid)) {
                            voteDown.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.setOnClickListener(null);
                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
                                    FirebaseCommon.rankSong(curSong.getKey(), -1, mainActivity);
                                }
                            });
                        } else {
                            voteDown.setImageResource(R.drawable.ic_chevron_down_gold_48dp);
                            voteDown.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.setOnClickListener(null);
                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
                                }
                            });
                        }
                        int rating = 0;
                        if (votedUp != null) {
                            rating += votedUp.size();
                        }
                        if (votedDown != null) {
                            rating -= votedDown.size();
                        }
                        ratingText.setText("" + rating);
                    }

                    songTitleText.setText(curSong.getTitle());
                    songArtistText.setText(curSong.getArtist());
                    //new ImageLoadTask(curSong.getAlbumArtSmall(), albumArtImage).execute();
                    mainActivity.imgLoader.DisplayImage(curSong.getAlbumArtSmall(), albumArtImage);
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
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.groupAddSongButton)) {
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (groupFragment != null && groupFragment.isVisible()) {
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out).replace(R.id.container, new AddSongFragment(), "ADD_SONG_FRAG").addToBackStack(null).commit();
            }
        }
    }
}
