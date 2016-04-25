package io.yeomans.echelon.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 4/25/16.
 */
public class PlayerService extends Service implements PlayerNotificationCallback, ConnectionStateCallback {
    private static final String TAG = "PlayerService";

    public boolean initiated = false;
    public Firebase firebaseRef;
    SharedPreferences pref, groupPref;
    private ValueEventListener trackListChangeListener;

    public Player mPlayer;
    public boolean mPlayerPlaying;
    public boolean mPlayerCherry;
    public boolean playerReady;
    public boolean loggedIn;
    public LinkedList<SpotifySong> backStack;
    public List<SpotifySong> playQueue;
    private OnPlayerControlCallback mPlayerControlCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playQueue = new LinkedList<>();
        backStack = new LinkedList<>();
        pref = getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        groupPref = getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);

        trackListChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MyFirebase", "Track data changed!");
                playQueue.clear();
                SpotifySong nowPlayingSS = null;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    SpotifySong ss = dataSnapshot1.getValue(SpotifySong.class);
                    if (!ss.isNowPlaying() && !ss.isPlayed()) {
                        playQueue.add(ss);
                    } else if (ss.isNowPlaying()) {
//                        if (nowPlaying == null || !ss.getKey().equals(nowPlaying.getKey())) {
//                            nowPlaying = ss;
//                            //Picasso.with(mainActivity).load(nowPlaying.getAlbumArtLarge().url).placeholder(R.drawable.ic_music_circle_black_48dp).into(nowPlayingAlbumCover);
//                        }
                    }
                    //Log.d("MyFirebase", "Song added: " + playqueue.getLast().getAdded());
                }
                Collections.sort(playQueue);
//                if (songListRA != null) {
//                    songListRA.notifyDataSetChanged();
//                }
//                if (songCountText != null) {
//                    songCountText.setText("" + playQueue.size());
//                }
//                if (nowPlayingSS != null) {
//                    playqueue.addFirst(nowPlayingSS);
//                }
                //refreshQueueList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        return 1;
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void configPlayer(String spotifyAuthToken, String CLIENT_ID) {
        Config playerConfig = new Config(this, spotifyAuthToken, CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer.addConnectionStateCallback(PlayerService.this);
                mPlayer.addPlayerNotificationCallback(PlayerService.this);
                playerReady = true;
                mPlayerPlaying = false;
                mPlayerCherry = true;
                initiated = true;
                Log.d("Player", "Player Ready");
                //playFirstSong();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    public void setFirebaseRef(Firebase ref) {
        firebaseRef = ref;
    }

    public void onStop() {

    }

    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onLowMemory() {

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
            Map<String, Object> oldMap = new HashMap<>();
            oldMap.put("nowPlaying", false);
            oldMap.put("played", true);
            firebaseRef.child("queuegroups")
                    .child(groupPref.getString(MainActivity.PREF_GROUP_NAME, null))
                    .child("tracks")
                    .child(old.getKey())
                    .updateChildren(oldMap);
            backStack.add(old);
            playQueue.remove(0);
            if (playQueue.size() > 0) {
                SpotifySong toPlay = playQueue.get(0);
                mPlayer.play(toPlay.getUri());
                firebaseRef.child("queuegroups")
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
            firebaseRef.child("queuegroups")
                    .child(groupPref.getString(MainActivity.PREF_GROUP_NAME, null))
                    .child("tracks")
                    .child(toPlay.getKey())
                    .child("nowPlaying")
                    .setValue(true);
        }
    }

    public interface OnPlayerControlCallback {
        void onPlayerPlay();

        void onPlayerPause();
    }

    public void setOnPlayerControlCallback(OnPlayerControlCallback mPlayerControlCallback) {
        this.mPlayerControlCallback = mPlayerControlCallback;
    }

    public class MyBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

}