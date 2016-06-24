package io.yeomans.echelon.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;
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
import io.yeomans.echelon.util.BackendRequest;
import io.yeomans.echelon.util.EchelonUtils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, ConnectionStateCallback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public Toolbar toolbar;
    public ActionBar actionBar;
    private NavigationView navigationView;
    public CoordinatorLayout coordinatorLayout;
    public static final String MAIN_PREFS_NAME = "basic_pref";
    public static final String GROUP_PREFS_NAME = "group_pref";

    //NAV DRAWER
    private IProfile profile;

    //USER PREF
    public static final String PREF_USER_AUTH_TYPE = "user_auth_type";
    public static final String PREF_USER_DISPLAY_NAME = "user_display_name";
    public static final String PREF_USER_EXT_URL = "user_ext_url";
    public static final String PREF_USER_IMAGE_URL = "user_image_url";

    //SPOTIFY USER PREF
    public static final String PREF_SPOTIFY_AUTHENTICATED = "spotify_authenticated";
    public static final String PREF_SPOTIFY_AUTH_TOKEN = "spotify_auth_token";
    public static final String PREF_SPOTIFY_UID = "spotify_uid";
    public static final String PREF_SPOTIFY_DISPLAY_NAME = "spotify_display_name";
    public static final String PREF_SPOTIFY_EMAIL = "spotify_email";
    public static final String PREF_SPOTIFY_COUNTRY = "spotify_country";
    public static final String PREF_SPOTIFY_EXT_URL = "spotify_ext_url";
    public static final String PREF_SPOTIFY_PRODUCT = "spotify_product";
    public static final String PREF_SPOTIFY_TYPE = "spotify_type";
    public static final String PREF_SPOTIFY_URI = "spotify_uri";
    public static final String PREF_SPOTIFY_IMAGE_URL = "spotify_image_url";

    //GROUP PREF
    public static final String PREF_GROUP_PK = "group_pk";
    public static final String PREF_GROUP_NAME = "group_name";
    public static final String PREF_GROUP_OWNER_PK = "group_owner_pk";
    public static final String PREF_GROUP_LEADER_UID = "group_leader_uid";
    public static final String PREF_GROUP_OWNER_USERNAME = "group_owner_username";
    public static final String PREF_GROUP_PARTICIPANTS_JSON = "group_participants_json";

    //SPOTIFY
    public static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    public static final String REDIRECT_URI = "echelonapp://callback";
    public static final int REQUEST_CODE = 9001;

    public boolean spotifyAuthenticated;
    public AuthenticationResponse authResponse;
    public String spotifyAuthToken;
    public SpotifyApi spotifyApi;
    public SpotifyService spotify;
    public PlayerService playerService;
    public boolean playerConnBound;

    public boolean loggedIn;
    public LinkedList<SpotifySong> backStack;
    public List<SpotifySong> playQueue;
    private OnPlayerControlCallback mPlayerControlCallback;

    private ServiceConnection playerConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((PlayerService.PlayerBinder) service).getService();
            if (!playerService.isInitiated()) {
                playerService.setFirebaseRef(myFirebaseRef);
                playerService.configPlayer(spotifyAuthToken, CLIENT_ID);
            }
            Log.i("PlayerService", "Connected to MainActivity");
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
    static final String TAG = "GCM Demo";

    String regid;
    AtomicInteger msgId = new AtomicInteger();

    //FIREBASE
    public DatabaseReference myFirebaseRef;
    public FirebaseAuth firebaseAuth;
    public FirebaseApp firebaseApp;
    public static final String PREF_FIREBASE_UID = "firebase_uid";
    public static final String PROD_FIREBASE_URL = "https://flickering-heat-6442.firebaseio.com/";
    public static final String DEV_FIREBASE_URL = "https://echelon-dev.firebaseio.com/";
    public static final String ECHELON_PROD_WORKER_URL = "https://api.echelonapp.io/";
    public static final String ECHELON_DEV_WORKER_URL = "https://worker-dev-dot-echelon-1000.appspot.com/";
    //public static final String ECHELON_DEV_WORKER_URL = "http://192.168.0.204:8080/";

    //COMMON
    public SharedPreferences pref, groupPref;
    Context context;
    MainActivity mainActivity;
    MainActivity mainActivityClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG_MODE) {
            Log.i("MainActivity", "Debug Mode");
            FirebaseOptions fbOptions = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyA1AB-xEi2bRPKW3iazpUwMfObq1mSdo4Q")
                    .setApplicationId("1:1000399740031:android:380bd9907ea4b0e6")
                    .setDatabaseUrl("https://echelon-dev.firebaseio.com")
                    .setStorageBucket("echelon-dev.appspot.com")
                    .build();
            firebaseApp = FirebaseApp.initializeApp(this, fbOptions, "Testing");
            myFirebaseRef = FirebaseDatabase.getInstance(firebaseApp).getReference();
            firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
        } else {
            myFirebaseRef = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
        }
        spotifyApi = new SpotifyApi();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + spotifyAuthToken);
                    }
                })
                .build();

        Picasso.with(getApplicationContext()).setIndicatorsEnabled(BuildConfig.DEBUG);

        spotify = restAdapter.create(SpotifyService.class);

        context = getApplicationContext();
        mainActivity = this;
        mainActivityClass = MainActivity.this;

        pref = getSharedPreferences(MAIN_PREFS_NAME, 0);
        groupPref = getSharedPreferences(GROUP_PREFS_NAME, 0);

        //String token = pref.getString(PREF_ECHELON_API_TOKEN, null);


        if (firebaseAuth.getCurrentUser() == null) {
            loggedIn = false;
        } else {
            loggedIn = true;
            DatabaseReference userRef = myFirebaseRef.child("users/" + firebaseAuth.getCurrentUser().getUid());
            userRef.child("online").onDisconnect().setValue(false);
            userRef.child("online").setValue(true);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SharedPreferences.Editor edit = pref.edit();
                    if (dataSnapshot.hasChild("display_name")) {
                        edit.putString(MainActivity.PREF_USER_DISPLAY_NAME, (String) dataSnapshot.child("display_name").getValue());
                    }
                    if (dataSnapshot.hasChild("ext_url")) {
                        edit.putString(MainActivity.PREF_USER_EXT_URL, (String) dataSnapshot.child("ext_url").getValue());
                    }
                    if (dataSnapshot.hasChild("image_url")) {
                        edit.putString(MainActivity.PREF_USER_IMAGE_URL, (String) dataSnapshot.child("image_url").getValue());
                    }
                    edit.apply();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Toast.makeText(getApplicationContext(), "Error accessing the database", Toast.LENGTH_SHORT).show();
                }
            });
            if (pref.getBoolean(PREF_SPOTIFY_AUTHENTICATED, false)) {
                authenticateSpotify();
            }
        }

        playQueue = new LinkedList<>();
        backStack = new LinkedList<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        colorizeToolbar(toolbar, Color.WHITE, this);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);


        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
//            gcm = GoogleCloudMessaging.getInstance(this);
//            registerInBackground();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        if (!loggedIn) {
            setContentViewLogin();
        } else {
            setContentViewHome();
            setUpNavDrawerAndActionBar();
        }

        setOnPlayerControlCallback(((OnPlayerControlCallback) getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")));
    }

    private void setContentViewLogin() {
        Fragment newFragment = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, newFragment).commit();
    }

    private void setContentViewHome() {
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
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName("Submit Feature/Bug");
        SecondaryDrawerItem item6 = new SecondaryDrawerItem().withName("About");
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
                                String gName = groupPref.getString(MainActivity.PREF_GROUP_NAME, null);
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
                                    if (groupPref.getString(MainActivity.PREF_GROUP_LEADER_UID, "").equals(pref.getString(MainActivity.PREF_FIREBASE_UID, "."))) {
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
        if (firebaseAuth.getCurrentUser() != null) {
            myFirebaseRef.child("users/" + firebaseAuth.getCurrentUser().getUid())
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
            myFirebaseRef.child("participants/" + firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String displayName = (String) dataSnapshot.child("display_name").getValue();
                                    if (displayName != null && !displayName.equals("null")) {
                                        profile.withName(displayName);
                                        pref.edit().putString(MainActivity.PREF_USER_DISPLAY_NAME, displayName).apply();
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

    public void checkGroup() {
        myFirebaseRef.child("users/" + pref.getString(MainActivity.PREF_FIREBASE_UID, null) + "/cur_group")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            myFirebaseRef.child("queuegroups/" + dataSnapshot.getValue()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        groupPref.edit()
                                                .putString(MainActivity.PREF_GROUP_NAME,
                                                        (String) dataSnapshot.child("name").getValue())
                                                .putString(MainActivity.PREF_GROUP_LEADER_UID,
                                                        (String) dataSnapshot.child("leader").getValue()).apply();
                                        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                                        DialogFragment dialogFragment = null;
                                        if (pref.getString(MainActivity.PREF_FIREBASE_UID, "").equals(dataSnapshot.child("leader").getValue())) {
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
                                        myFirebaseRef.child("users/" + pref.getString(MainActivity.PREF_FIREBASE_UID, null) + "/cur_group").removeValue();
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
        SharedPreferences pref = getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        SharedPreferences pref2 = getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);

        //pref.edit().remove("token").putBoolean("logged_in", false).commit();
        DatabaseReference thisUserRef = myFirebaseRef.child("users/" + pref.getString(MainActivity.PREF_FIREBASE_UID, null));
        DatabaseReference thisParticipantRef = myFirebaseRef.child("participants/" + pref.getString(MainActivity.PREF_FIREBASE_UID, null));
        if (pref.getString(MainActivity.PREF_USER_AUTH_TYPE, "").equals("anonymous")) {
            thisUserRef.removeValue();
            thisParticipantRef.removeValue();
        } else {
            thisParticipantRef.child("online").setValue(false);
        }
        firebaseAuth.signOut();
        pref.edit().clear().apply();
        pref2.edit().clear().apply();
        AuthenticationClient.clearCookies(getApplicationContext());
        //spotifyLogout();
        FragmentManager fragmentManager = getSupportFragmentManager();
        ((ControlBarFragment) fragmentManager.findFragmentByTag("CONTROL_FRAG")).unReady();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().replace(R.id.container, new LoginFragment()).commit();
    }

    public boolean onLeaveGroupClick(MenuItem item) {
        DatabaseReference thisGroupRef = myFirebaseRef.child("queuegroups/" + groupPref.getString(MainActivity.PREF_GROUP_NAME, null));
        String uid = pref.getString(MainActivity.PREF_FIREBASE_UID, null);
        if (uid != null) {
            if (groupPref.getString(MainActivity.PREF_GROUP_LEADER_UID, "").equals(uid)) {
                thisGroupRef.removeValue();
            } else {
                thisGroupRef.child("participants/" + uid).removeValue();
            }
            myFirebaseRef.child("users/" + uid + "/cur_group").removeValue();
            groupPref.edit().clear().apply();
            ((ControlBarFragment) getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")).unReady();
            setContentViewHome();
        } else {
            Toast.makeText(getApplicationContext(), "Error, not identified, please log in", Toast.LENGTH_SHORT).show();
            logout();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        Log.d("Nav", "onOptionsItemSelected");

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            authResponse = AuthenticationClient.getResponse(resultCode, intent);
            switch (authResponse.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    spotifyAuthenticated = true;
                    spotifyAuthToken = authResponse.getAccessToken();
                    pref.edit()
                            .putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, true)
                            .putString(MainActivity.PREF_SPOTIFY_AUTH_TOKEN, spotifyAuthToken)
                            .apply();

                    //if (myFirebaseRef.getAuth() == null) {
                    BackendRequest be = new BackendRequest("GET", this);
                    BackendRequest.getSpotifyMeAuth(be);
                    //}
                    // Handle successful response
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.e("SpotifyAuth", authResponse.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Log.e("SpotifyAuth", authResponse.getError());
            }
        }
    }

    public void completeLogin() {
        Intent playerIntent = new Intent(this, PlayerService.class);
        if (EchelonUtils.isServiceRunning(this, PlayerService.class)) {
            playerConnBound = getApplicationContext().bindService(playerIntent, playerConn, 0);
        } else {
            getApplicationContext().startService(playerIntent);
            playerConnBound = getApplicationContext().bindService(playerIntent, playerConn, 0);
        }
        checkGroup();
    }

    @Override
    public void onLoggedIn() {
        spotifyAuthenticated = true;
        pref.edit().putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, true).apply();
        Log.d("Authentication", "Logged In to Spotify");
    }

    @Override
    public void onLoggedOut() {
        spotifyAuthenticated = false;
        pref.edit().putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, false).apply();
        Log.d("Authentication", "Logged Out of Spotify");
    }

//    public void spotifyLogout() {
//        AuthenticationClient.logout(mainActivity.getApplicationContext());
//        spotifyAuthenticated = false;
//        pref.edit().putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, false).apply();
//        Log.d("Authentication", "Logged Out of Spotify");
//    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    //register your activity onResume()
    @Override
    public void onResume() {
        super.onResume();
        Log.d("Main", "Activity onResume()");
        context.registerReceiver(actionGcmReceiver, new IntentFilter("gcm_intent"));
    }

    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();
        context.unregisterReceiver(actionGcmReceiver);
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

    public void authenticateSpotify() {
        Log.d("Authentication", "Authenticating Anonymously with Spotify");
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(MainActivity.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, MainActivity.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, MainActivity.REQUEST_CODE, request);
    }

    public void authenticateAnonymously() {
        Log.d("Authentication", "Authenticating Anonymously");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                DatabaseReference thisParticipantRef = myFirebaseRef.child("participants/" + authResult.getUser().getUid());
                thisParticipantRef.child("online").onDisconnect().setValue(false);
                thisParticipantRef.child("online").setValue(true);
                thisParticipantRef.child("display_name").setValue("Anonymous");
                pref.edit().putString(MainActivity.PREF_FIREBASE_UID, authResult.getUser().getUid())
                        .putString(MainActivity.PREF_USER_AUTH_TYPE, "anonymous")
                        .putString(MainActivity.PREF_USER_DISPLAY_NAME, "Anonymous")
                        .apply();
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment groupFragment = fragmentManager.findFragmentByTag("GROUP_FRAGMENT");
                if (groupFragment == null || !groupFragment.isVisible()) {
                    fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
                }
                setUpNavDrawerAndActionBar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
//            @Override
//            public void onAuthenticated(AuthData authData) {
//                DatabaseReference thisParticipantRef = myFirebaseRef.child("participants/" + authData.getUid());
//                thisParticipantRef.child("online").onDisconnect().setValue(false);
//                thisParticipantRef.child("online").setValue(true);
//                thisParticipantRef.child("display_name").setValue("Anonymous");
//                pref.edit().putString(MainActivity.PREF_FIREBASE_UID, authData.getUid())
//                        .putString(MainActivity.PREF_USER_AUTH_TYPE, "anonymous")
//                        .putString(MainActivity.PREF_USER_DISPLAY_NAME, "Anonymous")
//                        .apply();
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                Fragment groupFragment = fragmentManager.findFragmentByTag("GROUP_FRAGMENT");
//                if (groupFragment == null || !groupFragment.isVisible()) {
//                    fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
//                }
//                setUpNavDrawerAndActionBar();
//            }
//
//            @Override
//            public void onAuthenticationError(DatabaseError firebaseError) {
//                Toast.makeText(getApplicationContext(), "Error authenticating", Toast.LENGTH_SHORT).show();
//            }
//        });
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

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver actionGcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "gcm_intent received in group activity");
            // Extract data included in the Intent
            String action = intent.getStringExtra("action");
            //do other stuff here
        }
    };

    @Override
    public void onClick(View v) {
//        if (v == findViewById(R.id.controlPlayButton)) {
//            Log.d("Play","playlist from ControlFrag: " + playQueue);
//            if (playerReady) {
//                if (!mPlayerPlaying && mPlayerCherry) {
//                    playFirstSong();
//                    v.setBackground(getResources().getDrawable(android.R.drawable.ic_media_pause));
//                } else if (!mPlayerPlaying) {
//                    mPlayer.resume();
//                } else {
//                    mPlayer.pause();
//                }
//            } else {
//                Toast.makeText(getApplicationContext(),"Please log into Spotify",Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    /**
     * Use this method to colorize toolbar icons to the desired target color
     *
     * @param toolbarView       toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity          reference to activity needed to register observers
     */
    public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor, Activity activity) {
        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY);

        for (int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            //Step 1 : Changing the color of back button (or open drawer button).
            if (v instanceof ImageButton) {
                //Action Bar back button
                ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
            }

            if (v instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {

                    //Step 2: Changing the color of any ActionMenuViews - icons that
                    //are not back button, nor text, nor overflow menu icon.
                    final View innerView = ((ActionMenuView) v).getChildAt(j);

                    if (innerView instanceof ActionMenuItemView) {
                        int drawablesCount = ((ActionMenuItemView) innerView).getCompoundDrawables().length;
                        for (int k = 0; k < drawablesCount; k++) {
                            if (((ActionMenuItemView) innerView).getCompoundDrawables()[k] != null) {
                                final int finalK = k;

                                //Important to set the color filter in seperate thread,
                                //by adding it to the message queue
                                //Won't work otherwise.
                                innerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            //Step 3: Changing the color of title and subtitle.
            toolbarView.setTitleTextColor(toolbarIconsColor);
            toolbarView.setSubtitleTextColor(toolbarIconsColor);

            //Step 4: Changing the color of the Overflow Menu icon.
            setOverflowButtonColor(activity, colorFilter);
        }
    }

    private static void setOverflowButtonColor(final Activity activity, final PorterDuffColorFilter colorFilter) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                ImageView overflow = (ImageView) outViews.get(0);
                overflow.setColorFilter(colorFilter);
                removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }

    private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
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
