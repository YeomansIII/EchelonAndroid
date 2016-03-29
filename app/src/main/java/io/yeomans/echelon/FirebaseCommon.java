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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {

    static void addSong(String songId, final MainActivity mainActivity) {
        final MainActivity main = mainActivity;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String songId = params[0];
                String response = "";
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    String spotifyTracksUrl = "https://api.spotify.com/v1/tracks/?ids=" + songId;
                    url = new URL(spotifyTracksUrl);

                    urlConnection = (HttpURLConnection) url
                            .openConnection();
                    if (main.spotifyAuthToken != null) {
                        urlConnection.setRequestProperty("Authorization", "Bearer " + main.spotifyAuthToken);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace(); //If you want further info on failure...
                    }
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
                        Firebase push = mainActivity.myFirebaseRef.child("queuegroups").child(groupPrefs.getString(MainActivity.PREF_GROUP_NAME, "")).child("tracks").push();
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
                        toAdd.put("albumArtMedium", images.getJSONObject(1).getString("url"));
                        toAdd.put("albumArtLarge", images.getJSONObject(0).getString("url"));
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
        Firebase ref = mainActivity.myFirebaseRef.child("/queuegroups/"
                + groupPrefs.getString(MainActivity.PREF_GROUP_NAME, null) +
                "/tracks/" + key);

        String uid = ref.getAuth().getUid();

        if (upDown > 0) {
            ref.child("votedUp").child(uid).setValue(true);
        } else if (upDown < 0) {
            ref.child("votedDown").child(uid).setValue(true);
        } else {
            ref.child("votedUp").child(uid).removeValue();
            ref.child("votedDown").child(uid).removeValue();
        }
    }
}
