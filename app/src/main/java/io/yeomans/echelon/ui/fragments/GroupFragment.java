package io.yeomans.echelon.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.ui.adapters.SonglistRecyclerAdapter;
import io.yeomans.echelon.ui.other.NestedRVLinearLayoutManager;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

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

  private View view;
  private ArrayList<RelativeLayout> songListArr;
  DatabaseReference queuegroupRef;
  List<SpotifySong> playqueue;
  SpotifySong nowPlaying;
  private ValueEventListener trackListChangeListener, participantListener, nowPlayingChangeListener;

  private Boolean isFabOpen = false;
  @Bind(R.id.groupAddSongFab)
  protected FloatingActionButton fab;
  @Bind(R.id.groupAddSongFab1)
  protected FloatingActionButton fab1;
  @Bind(R.id.groupAddSongFab2)
  protected FloatingActionButton fab2;
  @Bind(R.id.groupAddSongFab3)
  protected FloatingActionButton fab3;
  private Animation fab_open, fab_close, rotate_forward, rotate_backward, tip_fade_in, tip_fade_out;
  @Bind(R.id.queueBrowseTextFrame)
  protected FrameLayout queueBrowseTextFrame;
  @Bind(R.id.queueSearchTextFrame)
  protected FrameLayout queueSearchTextFrame;
  @Bind(R.id.queueYourMusicTextFrame)
  protected FrameLayout queueYourMusicTextFrame;
  @Bind(R.id.queueOverlayFrame)
  protected FrameLayout queueOverlayFrame;
  @Bind(R.id.queueNowPlayingLayout)
  protected RelativeLayout queueNowPlayingLayout;
  @Bind(R.id.nowPlayingAlbumArtImage)
  protected ImageView nowPlayingAlbumArtImage;
  @Bind(R.id.nowPlayingTitleText)
  protected TextView nowPlayingTitleText;
  @Bind(R.id.nowPlayingArtistText)
  protected TextView nowPlayingArtistText;
  private Drawer particDrawerResult;
  private boolean particDrawerOpen;
  private String groupName;

  Dependencies dependencies;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dependencies = Dependencies.INSTANCE;

    mainActivity = (MainActivity) getActivity();

    playqueue = mainActivity.playQueue;
    isDestroyed = false;

    songListRA = new SonglistRecyclerAdapter(playqueue, true);

    queuegroupRef = dependencies.getCurrentGroupReference();
    trackListChangeListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d("MyFirebase", "Track data changed!");
        playqueue.clear();
        SpotifySong nowPlayingSS = null;
        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
          SpotifySong ss = dataSnapshot1.getValue(SpotifySong.class);
          playqueue.add(ss);
        }
        Collections.sort(playqueue);
        if (songListRA != null) {
          songListRA.notifyDataSetChanged();
        }
      }

      @Override
      public void onCancelled(DatabaseError firebaseError) {

      }

    };

    nowPlayingChangeListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d("MyFirebase", "Track data changed!");
        if (dataSnapshot.getValue() != null) {
          SpotifySong ss = dataSnapshot.getValue(SpotifySong.class);
          nowPlaying = ss;
          nowPlayingTitleText.setText(nowPlaying.getTitle());
          nowPlayingArtistText.setText(nowPlaying.getArtist());
          Picasso.with(mainActivity).load(nowPlaying.getAlbumArtLarge().url).placeholder(R.drawable.ic_music_circle_black_48dp).into(nowPlayingAlbumArtImage);
        }
      }

      @Override
      public void onCancelled(DatabaseError firebaseError) {

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
          dependencies.getDatabase().getReference("participants/" + o.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              String name;
              if (dataSnapshot.hasChild("display_name")) {
                name = dataSnapshot.child("display_name").getValue().toString();
              } else {
                name = "Anonymous";
              }
              ProfileDrawerItem dItem = new ProfileDrawerItem().withName(name);
              if (dataSnapshot.hasChild("image_url")) {
                dItem.withIcon(dataSnapshot.child("image_url").getValue().toString());
              } else {
                dItem.withIcon(R.drawable.ic_account_grey600_24dp);
              }
              dItem.withTag(dataSnapshot.getKey());
              dItem.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                  String userId = (String) drawerItem.getTag();
                  UserDetailBottomSheetFragment userBottom = UserDetailBottomSheetFragment.newInstance(userId);
                  userBottom.show(mainActivity.getSupportFragmentManager(), "USER_DETAIL");
                  return true;
                }
              });
              particDrawerResult.addItem(dItem);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
          });
        }
      }

      @Override
      public void onCancelled(DatabaseError firebaseError) {

      }
    };
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.queue_fragment,
      container, false);
    ButterKnife.bind(this, view);

    groupName = dependencies.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, "error");
    //mainActivity.actionBar.setElevation(0);
    mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
    mainActivity.actionBar.setTitle(groupName);

    setHasOptionsMenu(true);

    isDestroyed = false;

    mRecyclerView = (RecyclerView) view.findViewById(R.id.queuedSongsRecyclerView);
//        mRecyclerView.setNestedScrollingEnabled(false);
    mLayoutManager = new NestedRVLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

    mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

    if (savedInstanceState != null) {
      // Restore saved layout manager type.
      mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
        .getSerializable(KEY_LAYOUT_MANAGER);
    }
    setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

    songListRA.setOnSongClickListener(new SonglistRecyclerAdapter.OnSongClickListener() {
      @Override
      public void onSongClick(SonglistRecyclerAdapter.ViewHolder viewHolder) {
        SongDetailBottomSheetFragment songBottom = SongDetailBottomSheetFragment.newInstance(viewHolder.trackId);
        songBottom.show(mainActivity.getSupportFragmentManager(), "SONG_DETAIL");
      }
    });
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
    //((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString(PreferenceNames.PREF_GROUP_OWNER_USERNAME, "error"));

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

    if (!dependencies.getPreferences().getString(PreferenceNames.PREF_USER_AUTH_TYPE, "").equals("spotify")) {
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
    queuegroupRef.child("nowPlaying").addValueEventListener(nowPlayingChangeListener);
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
      case R.id.action_leave_group: {
        leaveGroup(item);
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
    queuegroupRef.child("nowPlaying").removeEventListener(nowPlayingChangeListener);
    //getView().findViewById(R.id.groupAddSongButton).setVisibility(View.GONE);
  }

  public void leaveGroup(MenuItem item) {
    Log.d("Group", "Leaving Group");
    queuegroupRef.child("tracks").removeEventListener(trackListChangeListener);
    queuegroupRef.child("participants").removeEventListener(participantListener);
    queuegroupRef.child("nowPlaying").removeEventListener(nowPlayingChangeListener);
    if (mainActivity.playerService != null) {
      mainActivity.playerService.kill();
    }
    if (dependencies.getAuth().getCurrentUser() != null) {
      String uid = dependencies.getAuth().getCurrentUser().getUid();
      if (dependencies.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_LEADER_UID, "").equals(uid)) {
        dependencies.getCurrentGroupReference().removeValue();
      } else {
        dependencies.getCurrentGroupReference().child("participants/" + uid).removeValue();
      }
      dependencies.getDatabase().getReference("users/" + uid + "/cur_group").removeValue();
      dependencies.getGroupPreferences().edit().clear().apply();
      ((ControlBarFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")).unReady();
    } else {
      Snackbar.make(view, "Not identified, please log in", Snackbar.LENGTH_SHORT).show();
      mainActivity.logout();
    }
    isDestroyed = true;
    mainActivity.setContentViewHome();
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
      case R.id.groupAddSongFab: // or add user (invite)
        Log.i("Queue", "FAB Touch");
        if (particDrawerOpen) { // this occurs when the participant drawer is open and the button is showing an "add user" icon
          Log.i("Queue", "Can't add a friend quite yet!");
          createInviteDialog();
        } else { // this occurs when the participant drawer is closed and the button is showing an "add" icon, meaning add music
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

  public void createInviteDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    builder.setTitle("Invite User");
    builder.setMessage("Include the user's friendcode in the following format: 'Display Name#0000'");

    final EditText input = new EditText(mainActivity);
    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        inviteUser(input.getText().toString());
      }
    });
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  public void inviteUser(String user) {
    if (dependencies.getAuth().getCurrentUser() != null) {
      Pattern p = Pattern.compile("([^#]*)#([0-9]{4})");
      Matcher m = p.matcher(user);
      if (m.find()) {
        String displayName = m.group(1);
        String friendCode = m.group(2);
        Log.d(TAG, "Getting username for display_name: " + displayName + " and friend_code: " + friendCode);
        dependencies.getDatabase().getReference("display_names/" + displayName + "/" + friendCode).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            String inviteeUid = (String) dataSnapshot.getValue();
            if (inviteeUid != null) {
              Map<String, Object> invite = new HashMap<>();
              invite.put("groupName", groupName);
              invite.put("inviter", dependencies.getAuth().getCurrentUser().getUid());
              invite.put("invitee", inviteeUid);
              DatabaseReference invitePush = dependencies.getDatabase().getReference("queue/invites").push();
              invitePush.setValue(invite);
              Snackbar.make(view, "Invite sent", Snackbar.LENGTH_SHORT).show();
            } else {
              Snackbar.make(view, "That user doesn't exist!", Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                public void onClick(View v) {
                  createInviteDialog();
                }
              }).setActionTextColor(getResources().getColor(R.color.ech_con_seafrost)).show();
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(view, "Error sending invite", Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
              public void onClick(View v) {
                createInviteDialog();
              }
            }).setActionTextColor(getResources().getColor(R.color.ech_con_seafrost)).show();
          }
        });
      } else {
        Snackbar.make(view, "Display Name not valid", Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
          public void onClick(View v) {
            createInviteDialog();
          }
        }).setActionTextColor(getResources().getColor(R.color.ech_con_seafrost)).show();
      }
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
