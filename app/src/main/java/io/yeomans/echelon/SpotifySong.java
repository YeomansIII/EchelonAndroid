package io.yeomans.echelon;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by jason on 6/26/15.
 */
public class SpotifySong implements Comparable<SpotifySong> {

    private String key;
    private Long added;
    private boolean played;
    private boolean backStack;
    private String songId;
    private String uri;
    private String title;
    private String artist;
    private String album;
    private int lengthMs;
    private String albumArtSmall;
    private String albumArtMedium;
    private String albumArtLarge;
    private int rating;
    private boolean nowPlaying;
    private Map<String, Object> votedUp;
    private Map<String, Object> votedDown;

    public SpotifySong() {

    }

    public SpotifySong(String key, Long added, String songId, String uri, String title, String artist, String album, int lengthMs, String albumArtSmall, String albumArtMedium, String albumArtLarge) {
        this.key = key;
        this.added = added;
        this.backStack = false;
        this.songId = songId;
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lengthMs = lengthMs;
        this.albumArtSmall = albumArtSmall;
        this.albumArtMedium = albumArtMedium;
        this.albumArtLarge = albumArtLarge;
        this.rating = 0;
        this.nowPlaying = false;
    }

    public SpotifySong(boolean backStack, String songId, String uri, String title, String artist, String album, int lengthMs, String albumArtSmall, String albumArtMedium, String albumArtLarge, int rating, boolean nowPlaying) {
        this.backStack = backStack;
        this.songId = songId;
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lengthMs = lengthMs;
        this.albumArtSmall = albumArtSmall;
        this.albumArtMedium = albumArtMedium;
        this.albumArtLarge = albumArtLarge;
        this.rating = rating;
        this.nowPlaying = nowPlaying;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getAdded() {
        return added;
    }

    public void setAdded(Long added) {
        this.added = added;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getLengthMs() {
        return lengthMs;
    }

    public void setLengthMs(int lengthMs) {
        this.lengthMs = lengthMs;
    }

    public String getAlbumArtSmall() {
        return albumArtSmall;
    }

    public void setAlbumArtSmall(String albumArtSmall) {
        this.albumArtSmall = albumArtSmall;
    }

    public String getAlbumArtMedium() {
        return albumArtMedium;
    }

    public void setAlbumArtMedium(String albumArtMedium) {
        this.albumArtMedium = albumArtMedium;
    }

    public String getAlbumArtLarge() {
        return albumArtLarge;
    }

    public void setAlbumArtLarge(String albumArtLarge) {
        this.albumArtLarge = albumArtLarge;
    }

    public boolean isBackStack() {
        return backStack;
    }

    public void setBackStack(boolean backStack) {
        this.backStack = backStack;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(boolean nowPlaying) {
        this.nowPlaying = nowPlaying;
    }

    public Map<String, Object> getVotedUp() {
        return votedUp;
    }

    public void setVotedUp(Map<String, Object> votedUp) {
        this.votedUp = votedUp;
    }

    public Map<String, Object> getVotedDown() {
        return votedDown;
    }

    public void setVotedDown(Map<String, Object> votedDown) {
        this.votedDown = votedDown;
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
                    albumArtSmall = jsonAlbum.getJSONArray("images").getJSONObject(2).getString("url");
                    lengthMs = json.getInt("duration_ms");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute("https://api.spotify.com/v1/tracks/" + songId, null, null);
    }

    @Override
    public int compareTo(SpotifySong ssong) {
        int srating = 0;
        if (ssong.getVotedUp() != null) {
            srating += ssong.getVotedUp().size();
        }
        if (ssong.getVotedDown() != null) {
            srating -= ssong.getVotedDown().size();
        }
        int arating = 0;
        if (this.getVotedUp() != null) {
            arating += this.getVotedUp().size();
        }
        if (this.getVotedDown() != null) {
            arating -= this.getVotedDown().size();
        }
        int result = ((Integer) srating).compareTo(arating);
        if (result == 0) {
            result = this.getAdded().compareTo(ssong.getAdded());
        }
        return result;
    }
}
