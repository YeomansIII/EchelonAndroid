package io.yeomans.echelon.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyService;
import io.yeomans.echelon.BuildConfig;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.services.PlayerService;
import io.yeomans.echelon.ui.fragments.AboutFragment;
import io.yeomans.echelon.ui.fragments.AccountFragment;
import io.yeomans.echelon.ui.fragments.ControlBarFragment;
import io.yeomans.echelon.ui.fragments.CurrentGroupDialogFragment;
import io.yeomans.echelon.ui.fragments.CurrentGroupLeaderDialogFragment;
import io.yeomans.echelon.ui.fragments.GroupFragment;
import io.yeomans.echelon.ui.fragments.HomeFragment;
import io.yeomans.echelon.ui.fragments.LoginFragment;
import io.yeomans.echelon.ui.fragments.SettingsFragment;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.EchelonUtils;
import io.yeomans.echelon.util.PreferenceNames;

public class MainActivity extends AppCompatActivity {

  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */
  public Toolbar toolbar;
  public ActionBar actionBar;
  private NavigationView navigationView;
  public CoordinatorLayout coordinatorLayout;

  //NAV DRAWER
  private IProfile profile;

  //SPOTIFY
  public static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
  public static final String REDIRECT_URI = "echelonapp://callback";
  public static final int REQUEST_CODE = 9001;

  public boolean spotifyAuthenticated;
  public AuthenticationResponse authResponse;
  public String spotifyAuthToken;
  // public SpotifyApi spotifyApi;
  public SpotifyService spotify;
  public PlayerService playerService;
  public boolean playerConnBound;

  public boolean loggedIn;
  public LinkedList<SpotifySong> backStack;
  public List<SpotifySong> playQueue;
  private OnPlayerControlCallback mPlayerControlCallback;
  public boolean shouldPlayAfterServiceInit;

  private ServiceConnection playerConn = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      playerService = ((PlayerService.PlayerBinder) service).getService();
      if (shouldPlayAfterServiceInit) {
        playerService.shouldPlayAfterServiceInit = shouldPlayAfterServiceInit;
        shouldPlayAfterServiceInit = false;
      }
      if (getGroup() != null && !playerService.isInitiated()) {
        playerService.configPlayer();
      }
      if (playerService.mPlayerPlaying) {
        sendBroadcast(playerService.playingIntent);
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
  };


  //GCM
  public static final String EXTRA_MESSAGE = "message";
  public static final String PROPERTY_REG_ID = "device_gcm_id";
  private static final String PROPERTY_APP_VERSION = "appVersion";
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

  /**
   * Substitute you own sender ID here. This is the project number you got
   * from the API Console, as described in "Getting Started."
   */
  String SENDER_ID = "45203521863";

  /**
   * Tag used on log messages.
   */
  static final String TAG = "MainActivity";

  String regid;
  AtomicInteger msgId = new AtomicInteger();

  //FIREBASE
  private Dependencies dependencies;
  public static final String PROD_FIREBASE_URL = "https://flickering-heat-6442.firebaseio.com/";
  public static final String DEV_FIREBASE_URL = "https://echelon-dev.firebaseio.com/";
  public static final String ECHELON_PROD_WORKER_URL = "https://echelon-1000.appspot.com/";
  //public static final String ECHELON_DEV_WORKER_URL = "http://192.168.0.204:8080/";

  //COMMON
  public SharedPreferences pref, groupPref;
  Context context;
  MainActivity mainActivity;
  MainActivity mainActivityClass;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    setContentView(R.layout.activity_main);
    Dependencies.INSTANCE.init(getApplicationContext());
    dependencies = Dependencies.INSTANCE;

    //String token = pref.getString(PREF_ECHELON_API_TOKEN, null);


    if (dependencies.getAuth().getCurrentUser() == null) {
      loggedIn = false;
      Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
      startActivity(intent);
      MainActivity.this.finish();
    } else {
      loggedIn = true;
      DatabaseReference userRef = dependencies.getDatabase().getReference("users/" + dependencies.getAuth().getCurrentUser().getUid());
      userRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          SharedPreferences.Editor edit = pref.edit();
          if (dataSnapshot.hasChild("display_name")) {
            edit.putString(PreferenceNames.PREF_USER_DISPLAY_NAME, (String) dataSnapshot.child("display_name").getValue());
          }
          if (dataSnapshot.hasChild("ext_url")) {
            edit.putString(PreferenceNames.PREF_USER_EXT_URL, (String) dataSnapshot.child("ext_url").getValue());
          }
          if (dataSnapshot.hasChild("image_url")) {
            edit.putString(PreferenceNames.PREF_USER_IMAGE_URL, (String) dataSnapshot.child("image_url").getValue());
          }
          edit.apply();
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
          Toast.makeText(getApplicationContext(), "Error accessing the database", Toast.LENGTH_SHORT).show();
        }
      });
    }

    spotify = io.github.kaaes.spotify.webapi.retrofit.v2.Spotify.createAuthenticatedService(spotifyAuthToken);
    Picasso.with(getApplicationContext()).setIndicatorsEnabled(BuildConfig.DEBUG);
    context = getApplicationContext();
    mainActivity = this;
    mainActivityClass = MainActivity.this;

    pref = dependencies.getPreferences();
    groupPref = dependencies.getGroupPreferences();

    playQueue = new LinkedList<>();
    backStack = new LinkedList<>();

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    actionBar = getSupportActionBar();
    //coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);


    // Check device for Play Services APK. If check succeeds, proceed with
    //  GCM registration.
    if (checkPlayServices()) {
//            gcm = GoogleCloudMessaging.getInstance(this);
//            registerInBackground();
    } else {
      Log.i(TAG, "No valid Google Play Services APK found.");
    }
    setContentViewHome();
    checkGroup();
    setUpNavDrawerAndActionBar();

    setOnPlayerControlCallback(((OnPlayerControlCallback) getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")));
  }

  public void setContentViewLogin() {
    Fragment newFragment = new LoginFragment();
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.container, newFragment).commit();
  }

  public void setContentViewHome() {
    Fragment newFragment = new HomeFragment();
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.container, newFragment).commit();
  }

  public void setUpNavDrawerAndActionBar() {
    DrawerImageLoader.init(new AbstractDrawerImageLoader() {
      @Override
      public void set(ImageView imageView, Uri uri, Drawable placeholder) {
        Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
      }

      @Override
      public void cancel(ImageView imageView) {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
      }
    });
    profile = new ProfileDrawerItem();
    final AccountHeader headerResult = new AccountHeaderBuilder()
      .withActivity(this)
      .withHeaderBackground(R.drawable.nav_header_bg)
      .addProfiles(profile)
      .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
        @Override
        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
          return false;
        }
      })
      .withSelectionListEnabledForSingleProfile(false)
      .build();
    PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_grey600_36dp);
    PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Group").withIcon(R.drawable.ic_queue_music_grey_36dp);
    PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Account").withIcon(R.drawable.ic_account_grey600_36dp);
    PrimaryDrawerItem item4 = new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_settings_grey600_36dp);
    PrimaryDrawerItem item5 = new SecondaryDrawerItem().withName("Submit Feature/Bug");
    PrimaryDrawerItem item6 = new SecondaryDrawerItem().withName("About");
    final Drawer result = new DrawerBuilder()
      .withActivity(this)
      .withToolbar(toolbar)
      .withAccountHeader(headerResult)
      .addDrawerItems(
        item1,
        item2,
        item3,
        item4,
        new DividerDrawerItem(),
        item5,
        item6
      )
      .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
          boolean returner = false;
          FragmentManager fragmentManager = getSupportFragmentManager();
          FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

          switch (position) {
            case 1:
              fragmentTransaction
                .replace(R.id.container, new HomeFragment(), "HOME_FRAG")
                .commit();
              returner = true;
              break;
            case 2:
              GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
              String gName = groupPref.getString(PreferenceNames.PREF_GROUP_NAME, null);
              if (groupFragment != null && groupFragment.isVisible()) {
                Log.d("Nav", "You are already at the group!");
              } else if (gName != null) {
                View cfocus = getCurrentFocus();
                if (cfocus != null) {
                  InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
                  imm.hideSoftInputFromWindow(cfocus.getWindowToken(), 0);
                }
                Fragment fragment = new GroupFragment();
                Bundle bundle = new Bundle();
                if (groupPref.getString(PreferenceNames.PREF_GROUP_LEADER_UID, "").equals(pref.getString(PreferenceNames.PREF_FIREBASE_UID, "."))) {
                  bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
                } else {
                  bundle.putStringArray("extra_stuff", new String[]{"" + false, "" + false});
                }
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                  .replace(R.id.container, fragment, "GROUP_FRAG")
                  .commit();
              } else {
                Toast.makeText(getApplicationContext(), "Please create or join a group first", Toast.LENGTH_SHORT).show();
              }
              returner = true;
              break;
            case 3:
              fragmentTransaction.replace(R.id.container, new AccountFragment(), "ACCOUNT_FRAG").addToBackStack(null).commit();
              returner = true;
              break;
            case 4:
              fragmentTransaction.replace(R.id.container, new SettingsFragment(), "SETTINGS_FRAG").addToBackStack(null).commit();
              returner = true;
              break;
            case 6:
              Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/1xta8IsctqjHZ-o5-NNOgUUIuX9WFjsqvWFFaWnLauLw/viewform?usp=send_form"));
              startActivity(browserIntent);
              break;
            case 7:
              fragmentTransaction.replace(R.id.container, new AboutFragment(), "ABOUT_FRAG").addToBackStack(null).commit();
              returner = true;
              break;
          }
          return false;
        }
      }).build();
    if (dependencies.getAuth().getCurrentUser() != null) {
      dependencies.getCurrentUserReference()
        .addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              String email = (String) dataSnapshot.child("email").getValue();
              if (email != null && !email.equals("null")) {
                profile.withEmail(email);
              }
              headerResult.updateProfile(profile);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
          }
        );
      dependencies.getCurrentParticipantReference()
        .addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              String displayName = (String) dataSnapshot.child("display_name").getValue();
              if (displayName != null && !displayName.equals("null")) {
                profile.withName(displayName);
                pref.edit().putString(PreferenceNames.PREF_USER_DISPLAY_NAME, displayName).apply();
              } else {
                profile.withName((String) dataSnapshot.child("id").getValue());
              }
              String imgUrl = (String) dataSnapshot.child("image_url").getValue();
              if (imgUrl != null) {
                profile.withIcon(imgUrl);
              }
              headerResult.updateProfile(profile);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
          }
        );
    }
  }

  public String getGroup() {
    return dependencies.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, null);
  }

  public void checkGroup() {
    dependencies.getCurrentUserReference().child("cur_group")
      .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() != null) {
            dependencies.getDatabase().getReference("queuegroups/" + dataSnapshot.getValue()).addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                  groupPref.edit()
                    .putString(PreferenceNames.PREF_GROUP_NAME,
                      (String) dataSnapshot.child("name").getValue())
                    .putString(PreferenceNames.PREF_GROUP_LEADER_UID,
                      (String) dataSnapshot.child("leader").getValue()).apply();
                  FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                  DialogFragment dialogFragment = null;
                  if (pref.getString(PreferenceNames.PREF_FIREBASE_UID, "").equals(dataSnapshot.child("leader").getValue())) {
                    dialogFragment = new CurrentGroupLeaderDialogFragment();
                    //dialogFragment.show(getSupportFragmentManager(), "CURRENT_GROUP_LEADER_DIALOG");
                  } else {
                    dialogFragment = new CurrentGroupDialogFragment();
                    //dialogFragment.show(getSupportFragmentManager(), "CURRENT_GROUP_DIALOG");
                  }
                  //dialogFragment.show(fragmentTransaction, "CURRENT_GROUP_DIALOG");
                  fragmentTransaction.add(dialogFragment, null).commitAllowingStateLoss();
                  Log.d("Dialog", "Show group dialog");
                  //fragmentTransaction.commitAllowingStateLoss();
                } else {
                  dependencies.getCurrentUserReference().child("cur_group").removeValue();
                }
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
      });
  }

  public void logout() {
    if (pref.getString(PreferenceNames.PREF_USER_AUTH_TYPE, "").equals("anonymous")) {
      dependencies.getCurrentUserReference().removeValue();
      dependencies.getCurrentParticipantReference().removeValue();
    } else {
      dependencies.getCurrentParticipantReference().child("online").setValue(false);
    }
    dependencies.getAuth().signOut();
    pref.edit().clear().apply();
    groupPref.edit().clear().apply();
    AuthenticationClient.clearCookies(getApplicationContext());
    FragmentManager fragmentManager = getSupportFragmentManager();
    ((ControlBarFragment) fragmentManager.findFragmentByTag("CONTROL_FRAG")).unReady();
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    fragmentManager.beginTransaction().replace(R.id.container, new LoginFragment()).commit();
  }

//    public boolean onLeaveGroupClick(MenuItem item) {
//        if (dependencies.getAuth().getCurrentUser() != null) {
//            String uid = dependencies.getAuth().getCurrentUser().getUid();
//            if (groupPref.getString(PreferenceNames.PREF_GROUP_LEADER_UID, "").equals(uid)) {
//                dependencies.getCurrentGroupReference().removeValue();
//            } else {
//                dependencies.getCurrentGroupReference().child("participants/" + uid).removeValue();
//            }
//            dependencies.getDatabase().getReference("users/" + uid + "/cur_group").removeValue();
//            groupPref.edit().clear().apply();
//            ((ControlBarFragment) getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")).unReady();
//            setContentViewHome();
//        } else {
//            Snackbar.make(findViewById(R.id.coordinator_layout), "Not identified, please log in", Snackbar.LENGTH_SHORT).show();
//            logout();
//        }
//        return true;
//    }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    //int id = item.getItemId();
    Log.d("Nav", "onOptionsItemSelected");

    return super.onOptionsItemSelected(item);
  }

  public void startupPlayerService() {
    Intent playerIntent = new Intent(this, PlayerService.class);
    if (EchelonUtils.isServiceRunning(this, PlayerService.class)) {
      playerConnBound = getApplicationContext().bindService(playerIntent, playerConn, 0);
    } else {
      getApplicationContext().startService(playerIntent);
      playerConnBound = getApplicationContext().bindService(playerIntent, playerConn, 0);
    }
  }

  //register your activity onResume()
  @Override
  public void onResume() {
    super.onResume();
    Log.d("Main", "Activity onResume()");
    startupPlayerService();
    //context.registerReceiver(actionGcmReceiver, new IntentFilter("gcm_intent"));
  }

  //Must unregister onPause()
  @Override
  protected void onPause() {
    super.onPause();
    //context.unregisterReceiver(actionGcmReceiver);
  }

  @Override
  protected void onDestroy() {
    if (playerConnBound) {
      getApplicationContext().unbindService(playerConn);
    }
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment groupFragment = fragmentManager.findFragmentByTag("GROUP_FRAG");
    if (groupFragment != null && groupFragment.isVisible()) {
      fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
    } else {
      super.onBackPressed();
    }
  }

  /**
   * Check the device to make sure it has the Google Play Services APK. If
   * it doesn't, display a dialog that allows users to download the APK from
   * the Google Play Store or enable it in the device's system settings.
   */
  public boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        GooglePlayServicesUtil.getErrorDialog(resultCode, this,
          PLAY_SERVICES_RESOLUTION_REQUEST).show();
      } else {
        Log.i(TAG, "This device is not supported.");
        finish();
      }
      return false;
    }
    return true;
  }

  public boolean onPlayControlSelected() {
    return playerService.play();
  }

  public boolean onPauseControlSelected() {
    return playerService.pause();
  }

  public interface OnPlayerControlCallback {
    void onPlayerPlay();

    void onPlayerPause();
  }

  public void setOnPlayerControlCallback(OnPlayerControlCallback mPlayerControlCallback) {
    this.mPlayerControlCallback = mPlayerControlCallback;
  }
}
