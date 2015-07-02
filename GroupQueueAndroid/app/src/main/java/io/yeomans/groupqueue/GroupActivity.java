package io.yeomans.groupqueue;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by jason on 6/26/15.
 */
public class GroupActivity extends Activity implements PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "groupqueue://callback";

    private String groupId;
    private String playId;
    private boolean leader;

    private static final int REQUEST_CODE = 9001;
    public static final String PREFS_NAME = "group_prefs";

    private Player mPlayer;
    private boolean playerReady;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        playId = "";
        Bundle startingIntentBundle = getIntent().getExtras();
        if (startingIntentBundle != null) {
            String[] extras = startingIntentBundle.getStringArray("extra_stuff");
            //playId = extras[0];
            leader = Boolean.parseBoolean(extras[1]);
            Log.wtf("Test", "leader: " + leader);
        }
        Log.wtf("Intent Extras", playId);
        if (leader) {
            //((TextView) findViewById(R.id.syncRoom)).setText("Sync Room:" + playId);
            BackendRequest be = new BackendRequest("get","apiv1/queuegroups/create-group/",this);
            BackendRequest.createGroup(be);

            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }
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
                        mPlayer.addConnectionStateCallback(GroupActivity.this);
                        mPlayer.addPlayerNotificationCallback(GroupActivity.this);
                        playerReady = true;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

//    public void showListenerId() {
//
//        new AsyncTask<String, Void, String>() {
//            @Override
//            protected String doInBackground(String... params) {
//                String a = "";
//                HttpURLConnection urlConnection;
//                try {
//                    URL urlget = new URL(params[0]);
//                    urlConnection = (HttpURLConnection) urlget.openConnection();
//                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                    java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
//
//                    //return "works";
//                    String returner = s.hasNext() ? s.next() : "";
//                    urlConnection.disconnect();
//                    return returner;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return "{\"success\":false, \"error\":\"could not connect to server\"}";
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//                try {
//                    JSONObject json = new JSONObject(msg);
//                    if (json.getBoolean("success")) {
//                        idText.setTextSize(40f);
//                        myListenerId = json.getString("id");
//                        idText.setText("Your ID: " + myListenerId);
//                    } else {
//                        idText.setText(json.getString("error"));
//                    }
//                } catch (JSONException je) {
//                    je.printStackTrace();
//                }
//            }
//        }.execute(MYIDBASEURL + getRegistrationId(context), null, null);
//
//    }

//    public String searchSongs(String searchTerms) {
//
//        try {
//            AsyncTask<String, Void, String> get = new AsyncTask<String, Void, String>() {
//                @Override
//                protected String doInBackground(String... params) {
//                    String a = "";
//                    HttpURLConnection urlConnection;
//                    try {
//                        Log.d("GET", "URL: " + params[0]);
//                        URL urlget = new URL(params[0]);
//                        urlConnection = (HttpURLConnection) urlget.openConnection();
//                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                        java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
//
//                        //return "works";
//                        String returner = s.hasNext() ? s.next() : "";
//                        urlConnection.disconnect();
//                        return returner;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return "{\"success\":false, \"error\":\"could not connect to server\"}";
//                }
//
//                @Override
//                protected void onPostExecute(String msg) {
//                    try {
//                        Log.d("GET", "PostExecute Song Search");
//                        JSONObject json = new JSONObject(msg);
//                        JSONArray items = json.getJSONObject("tracks").getJSONArray("items");
//                        LinearLayout songList = (LinearLayout) findViewById(R.id.songList);
//                        songListArr = new ArrayList<TextView>();
//                        for (int i = 0; i < items.length(); i++) {
//                            JSONObject curObj = items.getJSONObject(i);
//                            TextView tv = new TextView(getApplicationContext());
//                            tv.setText(curObj.getString("name") + " by " + curObj.getJSONArray("artists").getJSONObject(0).getString("name"));
//                            tv.setTextSize(30f);
//                            tv.setTextColor(Color.WHITE);
//                            tv.setTag(curObj.getString("uri"));
//                            tv.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    trackUri = (String) v.getTag();
//                                    for (TextView view : songListArr) {
//                                        view.setBackgroundColor(Color.TRANSPARENT);
//                                    }
//                                    v.setBackgroundColor(Color.GRAY);
//                                }
//                            });
//                            songListArr.add(tv);
//                            songList.addView(tv);
//                        }
//                    } catch (JSONException je) {
//                        je.printStackTrace();
//                    }
//                }
//            }.execute("https://api.spotify.com/v1/search?q=" + URLEncoder.encode(searchTerms, "UTF-8") + "&type=track", null, null);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

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

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }
}
