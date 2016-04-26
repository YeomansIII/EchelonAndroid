package io.yeomans.echelon.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 4/25/16.
 */
public class PlayerService extends Service implements PlayerNotificationCallback, ConnectionStateCallback {
    private static final String TAG = "PlayerService";

    public boolean playerSetup = false, firebaseSetup = false;
    public Firebase firebaseRef, queuegroupRef;
    SharedPreferences pref, groupPref;
    private ValueEventListener trackListChangeListener;
    private PlayerBinder playerBinder;

    public Player mPlayer;
    public boolean mPlayerPlaying;
    public boolean mPlayerCherry;
    public boolean playerReady;
    public boolean loggedIn;
    public LinkedList<SpotifySong> backStack;
    public List<SpotifySong> playQueue;
    public SpotifySong nowPlaying;
    private OnPlayerControlCallback mPlayerControlCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Creating PlayerService");
        playerBinder = new PlayerBinder();
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

    private void setNowPlaying(SpotifySong nowPlaying) {
        Map<String, Object> toAdd = new HashMap<>();
        toAdd.put("key", nowPlaying.getKey());
        toAdd.put("added", ServerValue.TIMESTAMP);
        toAdd.put("songId", nowPlaying.getId());
        toAdd.put("uri", nowPlaying.getUri());
        toAdd.put("title", nowPlaying.getTitle());
        toAdd.put("artist", nowPlaying.getArtist());
        toAdd.put("album", nowPlaying.getAlbum());
        toAdd.put("lengthMs", nowPlaying.getLengthMs());
        toAdd.put("albumArtSmall", nowPlaying.getAlbumArtSmall());
        toAdd.put("albumArtMedium", nowPlaying.getAlbumArtMedium());
        toAdd.put("albumArtLarge", nowPlaying.getAlbumArtLarge());
        Log.d(TAG, (new JSONObject(toAdd).toString()));
        queuegroupRef.child("nowPlaying").setValue(toAdd, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("Data could not be saved. " + firebaseError.getCode());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });
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
                playerSetup = true;
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
        queuegroupRef = firebaseRef.child("queuegroups/" + groupPref.getString(MainActivity.PREF_GROUP_NAME, ""));
        queuegroupRef.child("tracks").addValueEventListener(trackListChangeListener);
        firebaseSetup = true;
        Toast.makeText(PlayerService.this, firebaseRef.getAuth().getUid(), Toast.LENGTH_SHORT).show();
    }

    public boolean isInitiated() {
        return playerSetup && firebaseSetup;
    }

    public boolean play() {
        if (!mPlayerPlaying && mPlayerCherry) {
            playFirstSong();
            return true;
        } else if (!mPlayerPlaying) {
            mPlayer.resume();
            return true;
        }
        return false;
    }

    public boolean pause() {
        if (mPlayerPlaying) {
            mPlayer.pause();
            return true;
        }
        return false;
    }

    public boolean stop() {
        mPlayer.pause();
        mPlayer.clearQueue();
        mPlayerCherry = true;
        return true;
    }

    public void onStop() {

    }

    public void onPause() {

    }

    @Override
    public void onDestroy() {
        queuegroupRef.child("tracks").removeEventListener(trackListChangeListener);
        Spotify.destroyPlayer(mPlayer);
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
            if (mPlayerControlCallback != null) {
                mPlayerControlCallback.onPlayerPlay();
            }
        } else if (eventType == EventType.PAUSE) {
            mPlayerPlaying = false;
            if (mPlayerControlCallback != null) {
                mPlayerControlCallback.onPlayerPause();
            }
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    public void playFirstSong() {
        Log.d("Play", "PlayQueue: " + playQueue);
        if (playQueue.size() > 0) {
            SpotifySong toPlay = playQueue.get(0);
            setNowPlaying(toPlay);
            mPlayer.play(toPlay.getUri());
            mPlayerCherry = false;
            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle("Echelon Player")
                            .setContentText("Playing music");
            startForeground(1, mBuilder.build());
        }
    }

    public interface OnPlayerControlCallback {
        void onPlayerPlay();

        void onPlayerPause();
    }

    public void setOnPlayerControlCallback(OnPlayerControlCallback mPlayerControlCallback) {
        this.mPlayerControlCallback = mPlayerControlCallback;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

}