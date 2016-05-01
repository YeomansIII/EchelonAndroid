package io.yeomans.echelon.ui.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.ui.adapters.SonglistRecyclerAdapter;
import io.yeomans.echelon.ui.other.NestedRVLinearLayoutManager;

/**
 * Created by jason on 8/10/15.
 */
public class GroupFragment extends Fragment implements View.OnClickListener {
    private MainActivity mainActivity;
    private ControlBarFragment controlBar;
    private boolean leader;

    private static final String TAG = "BrowseSongsFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    RecyclerView mRecyclerView;
    SonglistRecyclerAdapter songListRA;
    RecyclerView.LayoutManager mLayoutManager;

    public boolean isDestroyed;
    private SharedPreferences groupSettings;
    private SharedPreferences mainSettings;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    Firebase queuegroupRef;
    List<SpotifySong> playqueue;
    SpotifySong nowPlaying;
    private ValueEventListener trackListChangeListener, participantListener, nowPlayingChangeListener;

    private Boolean isFabOpen = false;
    @Bind(R.id.groupAddSongFab) protected FloatingActionButton fab;
    @Bind(R.id.groupAddSongFab1) protected FloatingActionButton fab1;
    @Bind(R.id.groupAddSongFab2) protected FloatingActionButton fab2;
    @Bind(R.id.groupAddSongFab3) protected FloatingActionButton fab3;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, tip_fade_in, tip_fade_out;
    @Bind(R.id.queueBrowseTextFrame) protected FrameLayout queueBrowseTextFrame;
    @Bind(R.id.queueSearchTextFrame) protected FrameLayout queueSearchTextFrame;
    @Bind(R.id.queueYourMusicTextFrame) protected FrameLayout queueYourMusicTextFrame;
    @Bind(R.id.queueOverlayFrame) protected FrameLayout queueOverlayFrame;
    @Bind(R.id.queueGroupNowPlayingSongCount) protected TextView songCountText;
    @Bind(R.id.queueGroupNowPlayingAlbumCover) protected ImageView nowPlayingAlbumCover;
    private Drawer particDrawerResult;
    private boolean particDrawerOpen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        mainSettings = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);

        mainActivity = (MainActivity) getActivity();
        playqueue = mainActivity.playQueue;
        isDestroyed = false;

        songListRA = new SonglistRecyclerAdapter(playqueue, true);

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
                    }
                }
                Collections.sort(playqueue);
                if (songListRA != null) {
                    songListRA.notifyDataSetChanged();
                }
                if (songCountText != null) {
                    songCountText.setText("" + playqueue.size());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };

        nowPlayingChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MyFirebase", "Track data changed!");
                SpotifySong ss = dataSnapshot.getValue(SpotifySong.class);
                if (nowPlaying == null || !ss.getKey().equals(nowPlaying.getKey())) {
                    nowPlaying = ss;
                    Picasso.with(mainActivity).load(nowPlaying.getAlbumArtLarge().url).placeholder(R.drawable.ic_music_circle_black_48dp).into(nowPlayingAlbumCover);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };

        participantListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //participantsArray = new ArrayList<>();
                particDrawerResult.removeAllItems();
                for (DataSnapshot o : dataSnapshot.getChildren()) {
                    //participantsArray.add(o.getValue(Participant.class));
                    //Participant p = o.getValue(Participant.class);
                    mainActivity.myFirebaseRef.child("participants/" + o.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name;
                            if (dataSnapshot.hasChild("display_name")) {
                                name = dataSnapshot.child("display_name").getValue().toString();
                            } else {
                                name = dataSnapshot.child("id").getValue().toString();
                            }
                            ProfileDrawerItem dItem = new ProfileDrawerItem().withName(name);
                            if (dataSnapshot.hasChild("image_url")) {
                                dItem.withIcon(dataSnapshot.child("image_url").getValue().toString());
                            } else {
                                dItem.withIcon(R.drawable.ic_account_grey600_24dp);
                            }
                            particDrawerResult.addItem(dItem);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
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
        ButterKnife.bind(this, view);

        //mainActivity.actionBar.setElevation(0);
        mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT);
        mainActivity.actionBar.setTitle(groupSettings.getString(MainActivity.PREF_GROUP_NAME, "error"));

        setHasOptionsMenu(true);

        isDestroyed = false;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.queuedSongsRecyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mLayoutManager = new NestedRVLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

//        songListRA.setOnSongClickListener(new SonglistRecyclerAdapter.OnSongClickListener() {
//            @Override
//            public void onSongClick(SonglistRecyclerAdapter.ViewHolder viewHolder) {
//                viewHolder.itemView.setBackgroundColor(Color.GRAY);
//                FirebaseCommon.addSong(viewHolder.trackId, mainActivity);
//            }
//        });
        mRecyclerView.setAdapter(songListRA);

        Bundle startingIntentBundle = this.getArguments();
        if (startingIntentBundle != null) {
            String[] extras = startingIntentBundle.getStringArray("extra_stuff");
            //playId = extras[0];
            leader = Boolean.parseBoolean(extras[1]);
            Log.wtf("Test", "leader: " + leader);
        }
        controlBar = (ControlBarFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG");
        if (controlBar != null) {
            if (leader) {
                controlBar.ready(true);
            } else {
                Log.d("Group", "Not leader");
            }
        }
        //((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString(MainActivity.PREF_GROUP_OWNER_USERNAME, "error"));

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

        particDrawerResult = new DrawerBuilder()
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
        particDrawerResult.getDrawerLayout().bringToFront();
        particDrawerResult.getDrawerLayout().setFitsSystemWindows(false);
        particDrawerResult.getSlider().setFitsSystemWindows(false);
        particDrawerOpen = false;

        this.view = view;

        //view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);

        queuegroupRef.child("tracks").addValueEventListener(trackListChangeListener);
        queuegroupRef.child("participants").addValueEventListener(participantListener);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group_participants: {
                if (particDrawerOpen) {
                    particDrawerResult.closeDrawer();
                } else {
                    particDrawerResult.openDrawer();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // queueFragment.onResume();
        //participantsFragment.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        queuegroupRef.child("tracks").removeEventListener(trackListChangeListener);
        queuegroupRef.child("participants").removeEventListener(participantListener);
        //getView().findViewById(R.id.groupAddSongButton).setVisibility(View.GONE);
    }

    public void leaveGroup() {
        if (mainActivity.playerService != null) {
            mainActivity.playerService.stop();
        }
        groupSettings.edit().clear().commit();
        controlBar.getView().setVisibility(View.GONE);
        Log.d("Group", "Leaving Group");
        isDestroyed = true;
    }

//    public void refreshQueueList() {
//        Log.d("RefreshQueue", "Refresh Queue List");
//        if (getActivity() != null) {
//            Log.d("Play", "PlayQueueList: " + playqueue);
//
//            if (playqueue.size() > 0) {
//                LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
//                songList.removeAllViews();
//                songListArr = new ArrayList<>();
//                for (int i = 0; i < playqueue.size(); i++) {
//                    final SpotifySong curSong = playqueue.get(i);
//                    RelativeLayout rt;
//                    if (curSong.isNowPlaying()) {
//                        rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.now_playing_item, null);
//                    } else {
//                        rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
//                    }
//                    ImageView albumArtImage = (ImageView) rt.findViewById(R.id.songAlbumArtImage);
//                    TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
//                    TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);
//                    RelativeLayout voteControlsLayout = (RelativeLayout) rt.findViewById(R.id.songItemVoterLayout);
//
//                    if (!curSong.isNowPlaying()) {
//                        voteControlsLayout.setVisibility(View.VISIBLE);
//                        ImageButton voteUp = (ImageButton) rt.findViewById(R.id.voteSongUpButton);
//                        ImageButton voteDown = (ImageButton) rt.findViewById(R.id.voteSongDownButton);
//                        TextView ratingText = (TextView) rt.findViewById(R.id.songRatingText);
//
//                        String fUid = mainSettings.getString(MainActivity.PREF_FIREBASE_UID, null);
//                        Log.d("RefreshList", fUid);
//                        //Check if user has voted up or down
//                        Map<String, Object> votedUp = curSong.getVotedUp();
//                        if (votedUp == null || !votedUp.containsKey(fUid)) {
//                            voteUp.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    v.setOnClickListener(null);
//                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
//                                    FirebaseCommon.rankSong(curSong.getKey(), 1, mainActivity);
//                                }
//                            });
//                        } else {
//                            voteUp.setImageResource(R.drawable.ic_chevron_up_gold_48dp);
//                            voteUp.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    v.setOnClickListener(null);
//                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
//                                }
//                            });
//                        }
//                        Map<String, Object> votedDown = curSong.getVotedDown();
//                        if (votedDown == null || !votedDown.containsKey(fUid)) {
//                            voteDown.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    v.setOnClickListener(null);
//                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
//                                    FirebaseCommon.rankSong(curSong.getKey(), -1, mainActivity);
//                                }
//                            });
//                        } else {
//                            voteDown.setImageResource(R.drawable.ic_chevron_down_gold_48dp);
//                            voteDown.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    v.setOnClickListener(null);
//                                    FirebaseCommon.rankSong(curSong.getKey(), 0, mainActivity);
//                                }
//                            });
//                        }
//                        int rating = 0;
//                        if (votedUp != null) {
//                            rating += votedUp.size();
//                        }
//                        if (votedDown != null) {
//                            rating -= votedDown.size();
//                        }
//                        ratingText.setText("" + rating);
//                    }
//
//                    songTitleText.setText(curSong.getTitle());
//                    songArtistText.setText(curSong.getArtist());
//                    Picasso.with(getContext()).load(curSong.getAlbumArtSmall()).into(albumArtImage);
//                    songListArr.add(rt);
//                    songList.addView(rt);
//                }
//            } else {
//                LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
//                songList.removeAllViews();
//                TextView tv = new TextView(getActivity().getApplicationContext());
//                tv.setText("No songs in queue. Search for a song!");
//                tv.setTextColor(Color.BLACK);
//                tv.setGravity(Gravity.CENTER);
//                //tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                songList.addView(tv);
//            }
//            Log.d("Play", "PlayQueueList End: " + playqueue);
//        }
//    }

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
                if (particDrawerOpen) {
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

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }
}