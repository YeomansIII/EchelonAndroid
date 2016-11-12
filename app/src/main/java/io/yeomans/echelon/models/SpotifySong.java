package io.yeomans.echelon.models;

import android.os.Parcel;

import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.kaaes.spotify.webapi.core.models.AlbumSimple;
import io.github.kaaes.spotify.webapi.core.models.ArtistSimple;
import io.github.kaaes.spotify.webapi.core.models.Image;
import io.github.kaaes.spotify.webapi.core.models.LinkedTrack;
import io.github.kaaes.spotify.webapi.core.models.Track;
import io.yeomans.echelon.util.Dependencies;

/**
 * Created by jason on 6/26/15.
 */
public class SpotifySong extends Track implements Comparable<SpotifySong> {

  public String key;
  public Long added;
  public boolean played;
  public boolean backStack;
  public String uri;
  public Map<String, Object> votedUp;
  public Map<String, Object> votedDown;
  public boolean fromDefault;

  public SpotifySong() {
    album = new AlbumSimple();
    List<Image> temp = new ArrayList<>();
    temp.add(new Image());
    temp.add(new Image());
    temp.add(new Image());
    album.images = temp;
    artists = new ArrayList<>();
  }

  public SpotifySong(String key, Track track) {
    this.key = key;
    artists = track.artists;
    available_markets = track.available_markets;
    is_playable = track.is_playable;
    linked_from = track.linked_from;
    disc_number = track.disc_number;
    duration_ms = track.duration_ms;
    explicit = track.explicit;
    external_urls = track.external_urls;
    href = track.href;
    id = track.id;
    name = track.name;
    preview_url = track.preview_url;
    track_number = track.track_number;
    type = track.type;
    uri = track.uri;
    album = track.album;
    external_ids = track.external_ids;
    popularity = track.popularity;
  }

  protected SpotifySong(Parcel in) {
    super(in);
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
    return id;
  }

  public void setSongId(String songId) {
    this.id = songId;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getTitle() {
    return name;
  }

  public void setTitle(String title) {
    this.name = title;
  }

  public String getArtist() {
    String artistas = "";
    for (ArtistSimple artista : artists) {
      artistas += artista.name + ", ";
    }
    return artistas.replaceAll(", $", "");
  }

  public void setArtist(String artist) {
    ArtistSimple temp = new ArtistSimple();
    temp.name = artist;
    this.artists.add(temp);
  }

  public void addArtist(String artist) {
    ArtistSimple temp = new ArtistSimple();
    temp.name = artist;
    this.artists.add(temp);
  }

  public String getAlbum() {
    return album.name;
  }

  public void setAlbum(String album) {
    this.album.name = album;
  }

  public long getLengthMs() {
    return duration_ms;
  }

  public void setLengthMs(int lengthMs) {
    this.duration_ms = lengthMs;
  }

  public Image getAlbumArtSmall() {
    return album.images.get(2);
  }

  public void setAlbumArtSmall(String albumArtSmall) {
    Image temp = new Image();
    temp.url = albumArtSmall;
    this.album.images.set(2, temp);
  }

  public Image getAlbumArtMedium() {
    return album.images.get(1);
  }

  public void setAlbumArtMedium(String albumArtMedium) {
    Image temp = new Image();
    temp.url = albumArtMedium;
    this.album.images.set(1, temp);
  }

  public Image getAlbumArtLarge() {
    return album.images.get(0);
  }

  public void setAlbumArtLarge(String albumArtLarge) {
    Image temp = new Image();
    temp.url = albumArtLarge;
    this.album.images.set(0, temp);
  }

  public boolean isBackStack() {
    return backStack;
  }

  public void setBackStack(boolean backStack) {
    this.backStack = backStack;
  }

  public int getRating() {
    return popularity != null ? popularity : 0;
  }

  public void setRating(int rating) {
    this.popularity = rating;
  }

  public boolean isUpVoted() {
    if (votedUp != null) {
      return votedUp.containsKey(Dependencies.INSTANCE.getAuth().getCurrentUser().getUid());
    }
    return false;
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

  public List<ArtistSimple> getArtists() {
    return artists;
  }

  public void setArtists(List<ArtistSimple> artists) {
    this.artists = artists;
  }

  public List<String> getAvailable_markets() {
    return available_markets;
  }

  public void setAvailable_markets(List<String> available_markets) {
    this.available_markets = available_markets;
  }

  public Boolean getIs_playable() {
    return is_playable;
  }

  public void setIs_playable(Boolean is_playable) {
    this.is_playable = is_playable;
  }

  public LinkedTrack getLinked_from() {
    return linked_from;
  }

  public void setLinked_from(LinkedTrack linked_from) {
    this.linked_from = linked_from;
  }

  public int getDisc_number() {
    return disc_number;
  }

  public void setDisc_number(int disc_number) {
    this.disc_number = disc_number;
  }

  public long getDuration_ms() {
    return duration_ms;
  }

  public void setDuration_ms(long duration_ms) {
    this.duration_ms = duration_ms;
  }

  public Boolean getExplicit() {
    return explicit;
  }

  public void setExplicit(Boolean explicit) {
    this.explicit = explicit;
  }

  public Map<String, String> getExternal_urls() {
    return external_urls;
  }

  public void setExternal_urls(Map<String, String> external_urls) {
    this.external_urls = external_urls;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPreview_url() {
    return preview_url;
  }

  public void setPreview_url(String preview_url) {
    this.preview_url = preview_url;
  }

  public int getTrack_number() {
    return track_number;
  }

  public void setTrack_number(int track_number) {
    this.track_number = track_number;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, Object> getMap() {
    Map<String, Object> toAdd = new HashMap<>();
    toAdd.put("key", getKey());
    toAdd.put("added", ServerValue.TIMESTAMP);
    toAdd.put("songId", getId());
    toAdd.put("uri", getUri());
    toAdd.put("title", getTitle());
    toAdd.put("artist", getArtist());
    toAdd.put("album", getAlbum());
    toAdd.put("lengthMs", getLengthMs());
    toAdd.put("albumArtSmall", getAlbumArtSmall().url);
    toAdd.put("albumArtMedium", getAlbumArtMedium().url);
    toAdd.put("albumArtLarge", getAlbumArtLarge().url);
    return toAdd;
  }

//    private void fillSongData() {
//        AsyncTask<String, Void, String> get = new AsyncTask<String, Void, String>() {
//            @Override
//            protected String doInBackground(String... params) {
//                String a = "";
//                HttpURLConnection urlConnection;
//                try {
//                    Log.d("GET", "URL: " + params[0]);
//                    URL urlget = new URL(params[0]);
//                    urlConnection = (HttpURLConnection) urlget.openConnection();
//                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                    Scanner s = new Scanner(in).useDelimiter("\\A");
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
//                    Log.d("GET", "PostExecute Song Search");
//                    JSONObject json = new JSONObject(msg);
//                    uri = json.getString("uri");
//                    title = json.getString("name");
//                    artist = json.getJSONArray("artists").getJSONObject(0).getString("name");
//                    JSONObject jsonAlbum = json.getJSONObject("album");
//                    album.name = jsonAlbum.getString("name");
//                    Image temp = new Image();
//                    temp.url = jsonAlbum.getJSONArray("images").getJSONObject(2).getString("url");
//                    album.images.set(2, temp);
//                    duration_ms = json.getInt("duration_ms");
//                } catch (JSONException je) {
//                    je.printStackTrace();
//                }
//            }
//        }.execute("https://api.spotify.com/v1/tracks/" + songId, null, null);
//    }

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

  public static final Creator<SpotifySong> CREATOR = new Creator<SpotifySong>() {
    public SpotifySong createFromParcel(Parcel source) {
      return new SpotifySong(source);
    }

    public SpotifySong[] newArray(int size) {
      return new SpotifySong[size];
    }
  };
}
