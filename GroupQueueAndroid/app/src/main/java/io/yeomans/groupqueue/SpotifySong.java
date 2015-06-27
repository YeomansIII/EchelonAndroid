package io.yeomans.groupqueue;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jason on 6/26/15.
 */
public class SpotifySong {

    private String songId;
    private String uri;
    private String title;
    private String artist;
    private String album;
    private int lengthMs;
    private String albumArt;

    public SpotifySong(String songId) {
        this.songId = songId;
    }

    private void fillSongData() {
            AsyncTask<String, Void, String> get = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... params) {
                    String a = "";
                    HttpURLConnection urlConnection;
                    try {
                        Log.d("GET", "URL: " + params[0]);
                        URL urlget = new URL(params[0]);
                        urlConnection = (HttpURLConnection) urlget.openConnection();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");

                        //return "works";
                        String returner = s.hasNext() ? s.next() : "";
                        urlConnection.disconnect();
                        return returner;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "{\"success\":false, \"error\":\"could not connect to server\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    try {
                        Log.d("GET", "PostExecute Song Search");
                        JSONObject json = new JSONObject(msg);
                        uri = json.getString("uri");
                        title = json.getString("name");
                        artist = json.getJSONArray("artists").getJSONObject(0).getString("name");
                        JSONObject jsonAlbum = json.getJSONObject("album");
                        album = jsonAlbum.getString("name");
                        albumArt = jsonAlbum.getJSONArray("images").getJSONObject(2).getString("url");
                        lengthMs = json.getInt("duration_ms");
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }.execute("https://api.spotify.com/v1/tracks/" + songId, null, null);
    }
}
