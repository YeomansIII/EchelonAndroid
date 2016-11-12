package io.yeomans.echelon.models;

/**
 * Created by jason on 10/3/16.
 */

import android.os.Parcel;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.kaaes.spotify.webapi.core.models.PlaylistTrack;
import io.yeomans.echelon.util.Dependencies;

public class Playlist {

  @SerializedName("description")
  @Expose
  private String description;
  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("image")
  @Expose
  private String image;
  @SerializedName("followers")
  @Expose
  private Integer followers;
  @SerializedName("name")
  @Expose
  private String name;
  @SerializedName("uri")
  @Expose
  private String uri;
  @SerializedName("type")
  @Expose
  private String type;
  @SerializedName("external_url")
  @Expose
  private String externalUrl;
  @SerializedName("tracks")
  @Expose
  private Map<String, SpotifySong> tracks;

  public Playlist() {

  }

  public Playlist(io.github.kaaes.spotify.webapi.core.models.Playlist playlist) {
    description = playlist.description;
    id = playlist.id;
    image = playlist.images.get(0).url;
    followers = playlist.followers.total;
    name = playlist.name;
    uri = playlist.uri;
    type = playlist.type;
    externalUrl = playlist.external_urls.get("spotify");
    tracks = new HashMap<>();
    DatabaseReference ref = Dependencies.INSTANCE.getDatabase().getReference();
    for (PlaylistTrack track : playlist.tracks.items) {
      String pushKey = ref.push().getKey();
      tracks.put(pushKey, new SpotifySong(pushKey, track.track));
    }
  }


  /**
   * @return The description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description The description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return The image
   */
  public String getImage() {
    return image;
  }

  /**
   * @param image The image
   */
  public void setImage(String image) {
    this.image = image;
  }

  /**
   * @return The followers
   */
  public Integer getFollowers() {
    return followers;
  }

  /**
   * @param followers The followers
   */
  public void setFollowers(Integer followers) {
    this.followers = followers;
  }

  /**
   * @return The name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return The uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * @param uri The uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type The type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return The externalUrl
   */
  public String getExternalUrl() {
    return externalUrl;
  }

  /**
   * @param externalUrl The external_url
   */
  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  /**
   * @return The track list
   */
  public Map<String, SpotifySong> getTracks() {
    return tracks;
  }

  /**
   * @param tracks The tracks list
   */
  public void setTracks(Map<String, SpotifySong> tracks) {
    this.tracks = tracks;
  }

  public Map<String, Object> getTracksMap() {
    Map<String, Object> tempMap = new HashMap<>();
    Iterator it = tracks.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      tempMap.put((String) pair.getKey(), ((SpotifySong) pair.getValue()).getMap());
      it.remove(); // avoids a ConcurrentModificationException
    }
    return tempMap;
  }
}
