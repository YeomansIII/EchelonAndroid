package io.yeomans.groupqueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity
        implements View.OnClickListener, NavigationDrawerFragment.NavigationDrawerCallbacks, PlayerNotificationCallback, ConnectionStateCallback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static final String MAIN_PREFS_NAME = "basic_pref";
    public static final String GROUP_PREFS_NAME = "group_pref";


    //POSITIONS
    public static final int HOME_POS = 0;
    public static final int GROUP_POS = 1;
    public static final int SETTINGS_POS = 2;

    //SPOTIFY
    public static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    public static final String REDIRECT_URI = "groupqueue://callback";
    public static final int REQUEST_CODE = 9001;

    public Player mPlayer;
    public boolean mPlayerPlaying;
    public boolean mPlayerCherry;
    public boolean playerReady;
    public boolean loggedIn;
    public ArrayList<SpotifySong> backStack;
    public ArrayList<SpotifySong> playQueue;
    private OnPlayerControlCallback mPlayerControlCallback;

    //GCM
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "listener_gcm_id";
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


    //COMMON
    SharedPreferences pref;
    Context context;
    MainActivity mainActivity;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    //public Fragment groupFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        mainActivity = this;

        pref = getSharedPreferences(MAIN_PREFS_NAME, 0);
        String token = pref.getString("token", null);

        loggedIn = pref.getBoolean("logged_in", false);

        playQueue = new ArrayList<>();
        backStack = new ArrayList<>();

        //setContentViewControl();
        setContentViewNav();

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
        }

        setOnPlayerControlCallback(((OnPlayerControlCallback) getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG")));
    }

    private void setContentViewLogin() {
        Fragment newFragment = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, newFragment).commit();
    }

//    private void setContentViewControl() {
//        ControlBarFragment controlFragment = new ControlBarFragment();
//        controlFragment.mainActivity = this;
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.add(R.id.control_bar_container, controlFragment).commit();
//    }

    private void setContentViewNav() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the group content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (groupFragment != null && groupFragment.isVisible()) {
            fragmentTransaction.detach(groupFragment);
        }
        switch (position) {
            case HOME_POS:
                fragmentTransaction
                        .replace(R.id.container, new HomeFragment(), "HOME_FRAG")
                        .commit();
                break;
            case GROUP_POS:
                if (groupFragment != null && !groupFragment.isVisible()) {
                    List<Fragment> listFrag = fragmentManager.getFragments();
                    Fragment currentFrag = null;
                    for (Fragment in : listFrag) {
                        if (in.isVisible()) {
                            currentFrag = in;
                        }
                    }
                    if (currentFrag != null) {
                        fragmentTransaction.remove(currentFrag);
                    }
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.attach(groupFragment).commit();
                } else if (groupFragment == null) {
                    Toast.makeText(getApplicationContext(), "Please create or join a group first", Toast.LENGTH_SHORT).show();
//                    fragmentManager.beginTransaction()
//                            .add(R.id.container, new GroupFragment(), "GROUP_FRAG")
//                            .commit();
                }
                break;
            case SETTINGS_POS:
                break;
        }
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.group, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (mNavigationDrawerFragment.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
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
        }
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

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
            try {
                JSONObject json = new JSONObject("{}");
                json.put("pk", old.getPk());
                json.put("played", true);
                BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/update-song/", json.toString(), mainActivity);
                be.updateSong(be);
            } catch (JSONException je) {
                je.printStackTrace();
            }
            backStack.add(old);
            playQueue.remove(0);
            if(playQueue.size()>0) {
                mPlayer.play(playQueue.get(0).getUri());
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
            mPlayer.play(playQueue.get(0).getUri());
            mPlayerCherry=false;
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
        builder.setScopes(new String[]{"user-read-private", "streaming"});
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
        if(mPlayerPlaying) {
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
