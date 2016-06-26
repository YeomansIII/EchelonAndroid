package io.yeomans.echelon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jason on 6/25/16.
 */
public class BackendSpotifyAuth {

    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("spotify-token")
    @Expose
    private String spotifyToken;

    public BackendSpotifyAuth(String uid, String spotifyToken) {
        this.uid = uid;
        this.spotifyToken = spotifyToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSpotifyToken() {
        return spotifyToken;
    }

    public void setSpotifyToken(String spotifyToken) {
        this.spotifyToken = spotifyToken;
    }
}
