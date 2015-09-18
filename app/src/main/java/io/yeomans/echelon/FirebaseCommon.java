package io.yeomans.echelon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {

    static void addSong(String songId, MainActivity mainActivity) {
        final MainActivity main = mainActivity;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String songId = params[0];
                try {
                    HttpClient client = new DefaultHttpClient();
                    String spotifyTracksUrl = "https://api.spotify.com/v1/tracks/?ids=" + songId;
                    HttpGet get2 = new HttpGet(spotifyTracksUrl);
                    Log.d("AddSong", get2.getURI().toString());
                    get2.addHeader("Authorization", "Bearer " + main.spotifyAuthToken);
                    HttpResponse responseGet2 = client.execute(get2);
                    HttpEntity resEntityGet2 = responseGet2.getEntity();
                    if (resEntityGet2 != null) {
                        return EntityUtils.toString(resEntityGet2);
                    }
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
                return "{\"error\":\"error\"}";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("SearchSongs", "" + msg);
                try {
                    JSONObject json = new JSONObject(msg);
                    JSONArray items = json.getJSONArray("tracks");
                    Log.d("RefreshQueue", "tracks: " + items.length());
                    for (int p = 0; p < items.length(); p++) {
                        JSONObject spotifyTrackJson = items.getJSONObject(p);
                        JSONObject album = spotifyTrackJson.getJSONObject("album");
                        JSONArray images = album.getJSONArray("images");
                        SharedPreferences groupPrefs = main.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
                        Firebase ref = new Firebase(MainActivity.FIREBASE_URL);
                        Firebase push = ref.child("queuegroups").child(groupPrefs.getString(MainActivity.PREF_GROUP_NAME, "")).child("tracks").push();
                        Map<String, Object> toAdd = new HashMap<>();
                        toAdd.put("key", push.getKey());
                        toAdd.put("added", ServerValue.TIMESTAMP);
                        toAdd.put("songId", spotifyTrackJson.getString("id"));
                        toAdd.put("uri", spotifyTrackJson.getString("uri"));
                        toAdd.put("title", spotifyTrackJson.getString("name"));
                        toAdd.put("artist", spotifyTrackJson.getJSONArray("artists").getJSONObject(0).getString("name"));
                        toAdd.put("album", album.getString("name"));
                        toAdd.put("lengthMs", spotifyTrackJson.getInt("duration_ms"));
                        toAdd.put("albumArtSmall", images.getJSONObject(2).getString("url"));
                        toAdd.put("albumArtSmall", images.getJSONObject(1).getString("url"));
                        toAdd.put("albumArtSmall", images.getJSONObject(0).getString("url"));
                        push.setValue(toAdd);
                        FragmentManager fragmentManager = main.getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                        if (groupFragment != null && !groupFragment.isVisible()) {
                            View view = main.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) main.getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            fragmentTransaction.replace(R.id.container, groupFragment).commit();
                        }
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(songId, null, null);
    }

    static public void rankSong(String key, final int upDown, MainActivity mainActivity) {
        SharedPreferences prefs = mainActivity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        SharedPreferences groupPrefs = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        final String firePath = MainActivity.FIREBASE_URL
                + "/queuegroups/"
                + groupPrefs.getString(MainActivity.PREF_GROUP_NAME, null) +
                "/tracks/" + key;
        Firebase ref = new Firebase(firePath);

        if (upDown > 0) {
            ref.child("votedUp").child(prefs.getString(MainActivity.PREF_FIREBASE_UID, null)).setValue(true);
        } else if (upDown < 0) {
            ref.child("votedDown").child(prefs.getString(MainActivity.PREF_FIREBASE_UID, null)).setValue(true);
        } else {
            ref.child("votedUp").child(prefs.getString(MainActivity.PREF_FIREBASE_UID, null)).removeValue();
            ref.child("votedDown").child(prefs.getString(MainActivity.PREF_FIREBASE_UID, null)).removeValue();
        }
    }
}
