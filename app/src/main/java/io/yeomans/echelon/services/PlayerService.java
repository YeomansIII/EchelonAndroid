package io.yeomans.echelon.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 4/25/16.
 */
public class PlayerService extends Service implements PlayerNotificationCallback, ConnectionStateCallback {
  private static final String TAG = "PlayerService";

  public boolean playerSetup = false, firebaseSetup = false;
  SharedPreferences pref, groupPref;
  private ValueEventListener trackListChangeListener, nowPlayingChangeListener;
  private PlayerBinder playerBinder;

  public Intent playingIntent = new Intent("io.yeomans.echelon.PLAYING"),
    pausingIntent = new Intent("io.yeomans.echelon.PAUSING"),
    playIntent = new Intent("io.yeomans.echelon.PLAY"),
    pauseIntent = new Intent("io.yeomans.echelon.PAUSE"),
    playPauseIntent = new Intent("io.yeomans.echelon.PLAY_PAUSE"),
    skipIntent = new Intent("io.yeomans.echelon.SKIP"),
    stopIntent = new Intent("io.yeomans.echelon.STOP"),
    stopServiceIntent = new Intent("io.yeomans.echelon.STOP_SERVICE");

  public Player mPlayer;
  public boolean mPlayerPlaying;
  public boolean mPlayerShouldPlaying;
  public boolean mPlayerCherry;
  public boolean playerReady;
  public boolean loggedIn;
  public LinkedList<SpotifySong> backStack;
  public List<SpotifySong> playQueue;
  public SpotifySong nowPlaying;

  Dependencies dependencies;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return playerBinder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Dependencies.INSTANCE.init(getApplicationContext());
    dependencies = Dependencies.INSTANCE;
    Log.i(TAG, "Creating PlayerService");
    playerBinder = new PlayerBinder();
    playQueue = new LinkedList<>();
    backStack = new LinkedList<>();
    pref = dependencies.getPreferences();
    groupPref = dependencies.getPreferences();

    IntentFilter filter = new IntentFilter();
    filter.addAction("io.yeomans.echelon.STOP_SERVICE");
    filter.addAction("io.yeomans.echelon.STOP");
    filter.addAction("io.yeomans.echelon.PLAY");
    filter.addAction("io.yeomans.echelon.PAUSE");
    filter.addAction("io.yeomans.echelon.PLAY_PAUSE");
    filter.addAction("io.yeomans.echelon.SKIP");

    registerReceiver(receiver, filter);

    trackListChangeListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d("MyFirebase", "Track data changed!");
        playQueue.clear();
        SpotifySong nowPlayingSS = null;
        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
          SpotifySong ss = dataSnapshot1.getValue(SpotifySong.class);
          playQueue.add(ss);
        }
        Collections.sort(playQueue);
        if (mPlayerShouldPlaying) {
          mPlayer.clearQueue();
          for (SpotifySong ss : playQueue) {
            mPlayer.queue(ss.getUri());
          }
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
      }

    };
    nowPlayingChangeListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
          nowPlaying = null;
        } else {
          nowPlaying = dataSnapshot.getValue(SpotifySong.class);
          startForegroundNotification(nowPlaying);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

  private void setNowPlaying(SpotifySong toPlay) {
    if (nowPlaying != null) {
      DatabaseReference push = dependencies.getCurrentGroupReference().child("pastTracks").push();
      push.setValue(nowPlaying.getMap());
    }
    if (toPlay == null) {
      dependencies.getCurrentGroupReference().child("nowPlaying").removeValue();
    } else {
      dependencies.getCurrentGroupReference().child("nowPlaying").setValue(toPlay.getMap());
      dependencies.getCurrentGroupReference().child("tracks/" + toPlay.getKey()).removeValue();
    }
  }

  public void configPlayer() {
    String authToken = pref.getString(PreferenceNames.PREF_SPOTIFY_AUTH_TOKEN, null);
    if (authToken != null) {
      Config playerConfig = new Config(this, authToken, MainActivity.CLIENT_ID);
      mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
        @Override
        public void onInitialized(Player player) {
          mPlayer.addConnectionStateCallback(PlayerService.this);
          mPlayer.addPlayerNotificationCallback(PlayerService.this);
          playerReady = true;
          mPlayerPlaying = false;
          mPlayerShouldPlaying = false;
          mPlayerCherry = true;
          playerSetup = true;
          Log.d("Player", "Player Ready");
          dependencies.getCurrentGroupReference().child("tracks").addValueEventListener(trackListChangeListener);
          dependencies.getCurrentGroupReference().child("nowPlaying").addValueEventListener(nowPlayingChangeListener);
          //playFirstSong();
        }

        @Override
        public void onError(Throwable throwable) {
          Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
        }
      });
    } else {
      Toast.makeText(getApplicationContext(), "Could not create player", Toast.LENGTH_LONG).show();
    }
  }

  public boolean isInitiated() {
    return playerSetup;
  }

  public boolean play() {
    if (!mPlayerPlaying && mPlayerCherry) {
      mPlayerShouldPlaying = true;
      playFirstSong();
      return true;
    } else if (!mPlayerPlaying) {
      mPlayerShouldPlaying = true;
      mPlayer.resume();
      return true;
    }
    return false;
  }

  public boolean pause() {
    if (mPlayerPlaying) {
      mPlayer.pause();
      mPlayerShouldPlaying = false;
      return true;
    }
    return false;
  }

  public void playPause() {
    if (!play()) {
      pause();
    }
  }

  public boolean stop() {
    mPlayer.pause();
    mPlayer.clearQueue();
    mPlayerCherry = true;
    mPlayerShouldPlaying = false;
    return true;
  }

  public void kill() {
    stop();
    dependencies.getCurrentGroupReference().child("tracks").removeEventListener(trackListChangeListener);
    dependencies.getCurrentGroupReference().child("nowPlaying").removeEventListener(nowPlayingChangeListener);
    Spotify.destroyPlayer(mPlayer);
    unregisterReceiver(receiver);
    stopSelf();
  }

  public void onStop() {

  }

  public void onPause() {

  }

  @Override
  public void onDestroy() {
    dependencies.getCurrentGroupReference().child("tracks").removeEventListener(trackListChangeListener);
    dependencies.getCurrentGroupReference().child("nowPlaying").removeEventListener(nowPlayingChangeListener);
    Spotify.destroyPlayer(mPlayer);
    try {
      unregisterReceiver(receiver);
    } catch (IllegalArgumentException e) {

    }
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
    if (eventType == EventType.TRACK_CHANGED) {
      setNowPlaying(playQueue.get(0));
    } else if (eventType == EventType.PLAY) {
      mPlayerPlaying = true;
      sendBroadcast(playingIntent);
    } else if (eventType == EventType.PAUSE) {
      mPlayerPlaying = false;
      sendBroadcast(pausingIntent);
      if (mPlayerShouldPlaying) {
        setNowPlaying(null);
        mPlayerShouldPlaying = false;
      }
    } else if (eventType == EventType.TRACK_END) {
      if (mPlayerPlaying) {
        mPlayerPlaying = false;
        sendBroadcast(pausingIntent);
      }
    } else if (eventType == EventType.TRACK_START) {
      if (!mPlayerPlaying) {
        mPlayerPlaying = true;
        sendBroadcast(playingIntent);
      }
    }
  }

  @Override
  public void onPlaybackError(ErrorType errorType, String s) {

  }

  public void playFirstSong() {
    Log.d("Play", "PlayQueue: " + playQueue);
    if (playQueue.size() > 0) {
      if (mPlayerShouldPlaying) {
        mPlayer.clearQueue();
        for (SpotifySong ss : playQueue) {
          mPlayer.queue(ss.getUri());
        }
      }
      mPlayerCherry = false;
    }
  }

  public void startForegroundNotification(SpotifySong spotifySong) {
    PendingIntent notifClickPendingIntent =
      PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    PendingIntent stopPIntent = PendingIntent.getBroadcast(this, 0, stopServiceIntent, 0);
    PendingIntent playPausePIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, 0);
    android.support.v4.app.NotificationCompat.Builder mBuilder =
      new android.support.v4.app.NotificationCompat.Builder(this)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(spotifySong.getTitle())
        .setContentText(spotifySong.getArtist())
        .setContentIntent(notifClickPendingIntent)
        .addAction(R.drawable.ic_stop_black_18dp, "Close", stopPIntent)
        .addAction(R.drawable.ic_stop_black_18dp, "Play/Pause", playPausePIntent);
    startForeground(1, mBuilder.build());
  }

  public class PlayerBinder extends Binder {
    public PlayerService getService() {
      return PlayerService.this;
    }
  }

  private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals("io.yeomans.echelon.SKIP")) {
        mPlayer.skipToNext();
      } else if (action.equals("io.yeomans.echelon.PLAY")) {
        Log.d(TAG, "mPlayerPlaying: " + mPlayerPlaying + "; mPlayerShouldPlaying: " + mPlayerShouldPlaying);
        play();
      } else if (action.equals("io.yeomans.echelon.PAUSE")) {
        Log.d(TAG, "mPlayerPlaying: " + mPlayerPlaying + "; mPlayerShouldPlaying: " + mPlayerShouldPlaying);
        pause();
      } else if (action.equals("io.yeomans.echelon.PLAY_PAUSE")) {
        Log.d(TAG, "mPlayerPlaying: " + mPlayerPlaying + "; mPlayerShouldPlaying: " + mPlayerShouldPlaying);
        playPause();
      } else if (action.equals("io.yeomans.echelon.STOP")) {
        Log.d(TAG, "Stop Service Intent");
        pause();
        stop();
      } else if (action.equals("io.yeomans.echelon.STOP_SERVICE")) {
        Log.d(TAG, "Stop Service Intent");
        pause();
        stop();
        stopSelf();
      }
    }
  };

}
