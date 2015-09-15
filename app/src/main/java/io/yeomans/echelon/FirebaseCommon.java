package io.yeomans.echelon;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;

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
import java.util.ArrayList;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {

    static void addSong(String songId, MainActivity mainActivity) {
        SharedPreferences groupPrefs = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        Firebase ref = mainActivity.myFirebaseRef;
        ref.child("queuegroups").child(groupPrefs.getString(MainActivity.PREF_GROUP_NAME, "")).child("tracks").push().setValue(songId);
    }

    static public void getListOfSpotifySongs(ArrayList<String> idList, MainActivity main) {
        final MainActivity mainActivity = main;
        new AsyncTask<ArrayList, Void, String>() {
            @Override
            protected String doInBackground(ArrayList... params) {
                ArrayList<String> songIdList = params[0];
                try {
                    HttpClient client = new DefaultHttpClient();
                    String spotifyTracksUrl = "https://api.spotify.com/v1/tracks/?ids=";
                    for (String id : songIdList) {
                        spotifyTracksUrl += id + ",";
                    }
                    spotifyTracksUrl = spotifyTracksUrl.replaceAll(",$", "");

                    HttpGet get2 = new HttpGet(spotifyTracksUrl);
                    Log.d("SearchSongs", get2.getURI().toString());
                    get2.addHeader("Authorization", "Bearer " + mainActivity.spotifyAuthToken);
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
                        SpotifySong ss = new SpotifySong(
                                spotifyTrackJson.getString("id"),
                                spotifyTrackJson.getString("uri"),
                                spotifyTrackJson.getString("name"),
                                spotifyTrackJson.getJSONArray("artists").getJSONObject(0).getString("name"),
                                album.getString("name"),
                                spotifyTrackJson.getInt("duration_ms"),
                                images.getJSONObject(2).getString("url"),
                                images.getJSONObject(1).getString("url"),
                                images.getJSONObject(0).getString("url")
                        );
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(idList, null, null);
    }
}
