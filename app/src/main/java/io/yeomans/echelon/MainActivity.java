package io.yeomans.echelon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, PlayerNotificationCallback, ConnectionStateCallback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public Toolbar toolbar;
    private NavigationView navigationView;
    public DrawerLayout drawerLayout;
    public static final String MAIN_PREFS_NAME = "basic_pref";
    public static final String GROUP_PREFS_NAME = "group_pref";

    //USER PREF
    public static final String PREF_ECHELON_API_TOKEN = "echelon_api_token";
    public static final String PREF_LISTENER_LOGGED_IN = "listener_logged_in";
    public static final String PREF_LISTENER_PK = "listener_pk";
    public static final String PREF_LISTENER_OWNER_OF = "listener_owner_of";
    public static final String PREF_LISTENER_USER_PK = "listener_user_pk";
    public static final String PREF_LISTENER_USERNAME = "listener_username";
    public static final String PREF_LISTENER_EMAIL = "listener_email";
    public static final String PREF_LISTENER_GCM_ID = "listener_gcm_id";

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
    public static final String REDIRECT_URI = "groupqueue://callback";
    public static final int REQUEST_CODE = 9001;

    public boolean spotifyAuthenticated;
    public AuthenticationResponse authResponse;
    public String spotifyAuthToken;

    public Player mPlayer;
    public boolean mPlayerPlaying;
    public boolean mPlayerCherry;
    public boolean playerReady;
    public boolean loggedIn;
    public LinkedList<SpotifySong> backStack;
    public LinkedList<SpotifySong> playQueue;
    private OnPlayerControlCallback mPlayerControlCallback;

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

    GoogleCloudMessaging gcm;
    String regid;
    AtomicInteger msgId = new AtomicInteger();

    //FIREBASE
    Firebase myFirebaseRef;
    public static final String PREF_FIREBASE_UID = "firebase_uid";
    public static final String FIREBASE_URL = "https://flickering-heat-6442.firebaseio.com/";
    public static final String ECHELONADO_URL = "https://api.echelonapp.io:8081/";

    //COMMON
    SharedPreferences pref, groupPref;
    Context context;
    MainActivity mainActivity;
    MainActivity mainActivityClass;
    ImageLoader imgLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        Firebase.setAndroidContext(this);

        setContentView(R.layout.activity_main);

        myFirebaseRef = new Firebase(FIREBASE_URL);
        //myFirebaseRef.child("message").setValue("Do you have data? You'll love Firebase.");

        context = getApplicationContext();
        mainActivity = this;
        mainActivityClass = MainActivity.this;
        imgLoader = new ImageLoader(this);

        pref = getSharedPreferences(MAIN_PREFS_NAME, 0);
        groupPref = getSharedPreferences(GROUP_PREFS_NAME, 0);

        //String token = pref.getString(PREF_ECHELON_API_TOKEN, null);

        loggedIn = pref.getBoolean(PREF_LISTENER_LOGGED_IN, false);

        playQueue = new LinkedList<>();
        backStack = new LinkedList<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        if (!loggedIn) {
            setContentViewLogin();
        } else {
            if (pref.getBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, false)) {
                authenticateSpotify();
            }
            setUpNavDrawerAndActionBar();
            String usernameJoin = groupPref.getString(MainActivity.PREF_GROUP_OWNER_USERNAME, null);
            Log.d("Main", "Part of group: " + usernameJoin);
            if (usernameJoin != null && usernameJoin.equals(pref.getString(PREF_LISTENER_USERNAME, ""))) {
                Log.d("Main", "In group. Leader");
                BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/activate-my-group/", this);
                BackendRequest.activateJoinGroup(be);
            } else if (usernameJoin != null) {
                try {
                    Log.d("Main", "In group.");
                    JSONObject json = new JSONObject("{}");
                    json.put("username_join", usernameJoin);
                    BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/join-group/", json.toString(), this);
                    BackendRequest.activateJoinGroup(be);
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                setContentViewHome();
            }
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
        //setContentViewControl();
        //setContentViewNav();
        navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Initializing Drawer Layout and ActionBarToggle
        //drawerLayout.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        ((TextView) findViewById(R.id.navHeaderUsernameText)).setText(pref.getString(PREF_SPOTIFY_DISPLAY_NAME, "error"));
        ImageView profileImage = (ImageView) findViewById(R.id.navHeaderProfileImage);
        profileImage.setImageResource(R.drawable.ic_account_black_48dp);
        //new ImageLoadTask(pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, ""), profileImage).execute();
        imgLoader.DisplayImage(pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, ""), profileImage);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    public boolean onLeaveGroupClick(MenuItem item) {
        BackendRequest be = new BackendRequest("GET", "apiv1/queuegroups/reset-group/", mainActivity);
        BackendRequest.resetGroup(be);
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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        boolean returner = false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.drawer_home:
                fragmentTransaction
                        .replace(R.id.container, new HomeFragment(), "HOME_FRAG")
                        .commit();
                returner = true;
                break;
            case R.id.drawer_group:
                GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                if (groupFragment != null && !groupFragment.isVisible()) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    fragmentTransaction.replace(R.id.container, groupFragment).commit();
                } else if (groupFragment == null) {
                    Toast.makeText(getApplicationContext(), "Please create or join a group first", Toast.LENGTH_SHORT).show();
//                    fragmentManager.beginTransaction()
//                            .add(R.id.container, new queueFragment(), "GROUP_FRAG")
//                            .commit();
                }
                returner = true;
                break;
            case R.id.drawer_settings:
                fragmentTransaction.replace(R.id.container, new SettingsFragment(), "SETTINGS_FRAG").addToBackStack(null).commit();
                returner = true;
                break;
            case R.id.drawer_bug:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/1xta8IsctqjHZ-o5-NNOgUUIuX9WFjsqvWFFaWnLauLw/viewform?usp=send_form"));
                startActivity(browserIntent);
                break;
            case R.id.drawer_about:
                fragmentTransaction.replace(R.id.container, new AboutFragment(), "ABPOUT_FRAG").addToBackStack(null).commit();
                returner = true;
                break;
        }
        if (returner) {
            drawerLayout.closeDrawer(navigationView);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            authResponse = AuthenticationClient.getResponse(resultCode, intent);
            if (authResponse.getType() == AuthenticationResponse.Type.TOKEN) {
                spotifyAuthenticated = true;
                spotifyAuthToken = authResponse.getAccessToken();
                pref.edit()
                        .putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, true)
                        .putString(MainActivity.PREF_SPOTIFY_AUTH_TOKEN, spotifyAuthToken)
                        .apply();

                BackendRequest be = new BackendRequest("GET", this);
                BackendRequest.getSpotifyMeAuth(be);
            }
        }
    }

    public void configPlayer() {
        Config playerConfig = new Config(this, spotifyAuthToken, CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer.addConnectionStateCallback(MainActivity.this);
                mPlayer.addPlayerNotificationCallback(MainActivity.this);
                playerReady = true;
                mPlayerPlaying = false;
                mPlayerCherry = true;
                Log.d("Player", "Player Ready");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
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

    public void spotifyLogout() {
        AuthenticationClient.logout(mainActivity.getApplicationContext());
        spotifyAuthenticated = false;
        pref.edit().putBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, false).apply();
        Log.d("Authentication", "Logged Out of Spotify");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        if (eventType == EventType.TRACK_END) {
            SpotifySong old = playQueue.get(0);
            old.setBackStack(true);
            myFirebaseRef.child("queuegroups")
                    .child(groupPref.getString(MainActivity.PREF_GROUP_NAME, null))
                    .child("tracks")
                    .child(old.getKey())
                    .child("nowPlaying")
                    .setValue(false);
            backStack.add(old);
            playQueue.remove(0);
            if (playQueue.size() > 0) {
                SpotifySong toPlay = playQueue.get(0);
                mPlayer.play(toPlay.getUri());
                myFirebaseRef.child("queuegroups")
                        .child(groupPref.getString(MainActivity.PREF_GROUP_NAME, null))
                        .child("tracks")
                        .child(toPlay.getKey())
                        .child("nowPlaying")
                        .setValue(true);
            } else {
                mPlayerCherry = true;
                mPlayerPlaying = false;
            }
        } else if (eventType == EventType.PLAY) {
            mPlayerPlaying = true;
            mPlayerControlCallback.onPlayerPlay();
        } else if (eventType == EventType.PAUSE) {
            mPlayerPlaying = false;
            mPlayerControlCallback.onPlayerPause();
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    public void playFirstSong() {
        Log.d("Play", "PlayQueue: " + playQueue);
        if (playQueue.size() > 0) {
            SpotifySong toPlay = playQueue.get(0);
            mPlayer.play(toPlay.getUri());
            mPlayerCherry = false;
            myFirebaseRef.child("queuegroups")
                    .child(groupPref.getString(MainActivity.PREF_GROUP_NAME, null))
                    .child("tracks")
                    .child(toPlay.getKey())
                    .child("nowPlaying")
                    .setValue(true);
        }
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
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(MainActivity.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, MainActivity.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, MainActivity.REQUEST_CODE, request);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
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

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        //int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + regId);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        //editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = pref.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        //int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        //int currentVersion = getAppVersion(context);
        //if (registeredVersion != currentVersion) {
        //    Log.i(TAG, "App version changed.");
        //    return "";
        // }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        Log.d("GCM", "Send to backend");
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver actionGcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "gcm_intent received in group activity");
            // Extract data included in the Intent
            String action = intent.getStringExtra("action");

            BackendRequest be = new BackendRequest("GET", mainActivity);
            BackendRequest.refreshGroupQueue(be);
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

    public boolean onPlayControlSelected() {
        if (!mPlayerPlaying && mPlayerCherry) {
            playFirstSong();
            return true;
        } else if (!mPlayerPlaying) {
            mPlayer.resume();
            return true;
        }
        return false;
    }

    public boolean onPauseControlSelected() {
        if (mPlayerPlaying) {
            mPlayer.pause();
            return true;
        }
        return false;
    }

    public interface OnPlayerControlCallback {
        void onPlayerPlay();

        void onPlayerPause();
    }

    public void setOnPlayerControlCallback(OnPlayerControlCallback mPlayerControlCallback) {
        this.mPlayerControlCallback = mPlayerControlCallback;
    }
}
