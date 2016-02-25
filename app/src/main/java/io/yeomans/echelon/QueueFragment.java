package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by jason on 6/26/15.
 */
public class QueueFragment extends Fragment implements View.OnClickListener {

    public boolean isDestroyed;
    private SharedPreferences groupSettings;
    private SharedPreferences mainSettings;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    private MainActivity mainActivity;
    Firebase queuegroupRef;
    LinkedList<SpotifySong> playqueue;
    private ValueEventListener trackListChangeListener;
    private ValueEventListener participantListener;

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2, fab3;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, tip_fade_in, tip_fade_out;
    private FrameLayout queueBrowseTextFrame, queueSearchTextFrame, queueYourMusicTextFrame, queueOverlayFrame;
    private Drawer result;
    private boolean particDrawerOpen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        mainSettings = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);

        mainActivity = (MainActivity) getActivity();
        playqueue = mainActivity.playQueue;
        isDestroyed = false;

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

        participantListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //participantsArray = new ArrayList<>();
                result.removeAllItems();
                for (DataSnapshot o : dataSnapshot.getChildren()) {
                    //participantsArray.add(o.getValue(Participant.class));
                    Participant p = o.getValue(Participant.class);
                    ProfileDrawerItem dItem = new ProfileDrawerItem().withName(p.getDisplayName());
                    if (p.getImageUrl() != null) {
                        dItem.withIcon(p.getImageUrl());
                    } else {
                        dItem.withIcon(R.drawable.ic_account_grey600_24dp);
                    }
                    result.addItem(dItem);
                }
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

        fab = (FloatingActionButton) view.findViewById(R.id.groupAddSongFab);
        fab1 = (FloatingActionButton) view.findViewById(R.id.groupAddSongFab1);
        fab2 = (FloatingActionButton) view.findViewById(R.id.groupAddSongFab2);
        fab3 = (FloatingActionButton) view.findViewById(R.id.groupAddSongFab3);
        queueBrowseTextFrame = (FrameLayout) view.findViewById(R.id.queueBrowseTextFrame);
        queueSearchTextFrame = (FrameLayout) view.findViewById(R.id.queueSearchTextFrame);
        queueYourMusicTextFrame = (FrameLayout) view.findViewById(R.id.queueYourMusicTextFrame);
        queueOverlayFrame = (FrameLayout) view.findViewById(R.id.queueOverlayFrame);
        fab_open = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.rotate_backward);
        tip_fade_in = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.tip_fade_in);
        tip_fade_out = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.tip_fade_out);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
        view.findViewById(R.id.queueOverlayFrame).setOnClickListener(this);

        if (!mainSettings.getString(MainActivity.PREF_USER_AUTH_TYPE, "").equals("spotify")) {
            fab2.setVisibility(View.GONE);
            queueYourMusicTextFrame.setVisibility(View.GONE);
        }

        result = new DrawerBuilder()
                .withActivity(getActivity())
                .withRootView((ViewGroup) view.findViewById(R.id.queueGroupRootView))
                .withDisplayBelowStatusBar(false)
                .withSavedInstance(savedInstanceState)
                .withDrawerGravity(Gravity.END)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        fab.setImageResource(R.drawable.ic_account_plus_white_36dp);
                        particDrawerOpen = true;
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        fab.setImageResource(R.drawable.ic_add_white_36dp);
                        particDrawerOpen = false;
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .buildForFragment();
        result.getDrawerLayout().setFitsSystemWindows(false);
        result.getSlider().setFitsSystemWindows(false);
        particDrawerOpen = false;

        this.view = view;

        //view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);

        queuegroupRef.child("tracks").addValueEventListener(trackListChangeListener);
        queuegroupRef.child("participants").addValueEventListener(participantListener);
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
        queuegroupRef.child("participants").removeEventListener(participantListener);
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
                    ImageView albumArtImage = (ImageView) rt.findViewById(R.id.songAlbumArtImage);
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
                    Picasso.with(getContext()).load(curSong.getAlbumArtSmall()).into(albumArtImage);
                    //String uri = curSong.getUri();
                    //mainActivity.playQueue.add(uri);
//                    rt.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //trackUri = (String) v.getTag();
//                            for (RelativeLayout view : songListArr) {
//                                view.setBackgroundColor(Color.TRANSPARENT);
//                            }
//                            v.setBackgroundColor(Color.GRAY);
//                        }
//                    });
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

    public void animateFAB() {

        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            queueBrowseTextFrame.startAnimation(fab_close);
            queueSearchTextFrame.startAnimation(fab_close);
            queueYourMusicTextFrame.startAnimation(fab_close);
            queueOverlayFrame.startAnimation(tip_fade_out);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            queueOverlayFrame.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            queueBrowseTextFrame.startAnimation(fab_open);
            queueSearchTextFrame.startAnimation(fab_open);
            queueYourMusicTextFrame.startAnimation(fab_open);
            queueOverlayFrame.startAnimation(tip_fade_in);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            queueOverlayFrame.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.groupAddSongFab:
                if(particDrawerOpen) {
                    Log.i("Queue", "Can't add a friend quite yet!");
                } else {
                    animateFAB();
                }
                break;
            case R.id.groupAddSongFab1: {
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (groupFragment != null && groupFragment.isVisible()) {
                    animateFAB();
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, new SongSearchFragment(), "SONG_SEARCH_FRAG").addToBackStack(null).commit();
                }
                break;
            }
            case R.id.groupAddSongFab2: {
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (groupFragment != null && groupFragment.isVisible()) {
                    animateFAB();
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, new YourMusicFragment(), "YOUR_MUSIC_FRAG").addToBackStack(null).commit();
                }
                break;
            }
            case R.id.groupAddSongFab3: {
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (groupFragment != null && groupFragment.isVisible()) {
                    animateFAB();
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, new BrowseSongsFragment(), "BROWSE_SONG_FRAG").addToBackStack(null).commit();
                }
                break;
            }
            case R.id.queueOverlayFrame:
                animateFAB();
                break;

        }
    }
}
