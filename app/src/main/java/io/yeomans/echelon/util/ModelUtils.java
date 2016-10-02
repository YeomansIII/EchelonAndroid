package io.yeomans.echelon.util;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.github.kaaes.spotify.webapi.core.models.Track;

/**
 * Created by jason on 10/2/16.
 */

public class ModelUtils {
  public static Map<String, Object> createTrackMap(Track track, String key) {
    Map<String, Object> toAdd = new HashMap<>();
    toAdd.put("key", key);
    toAdd.put("added", ServerValue.TIMESTAMP);
    toAdd.put("songId", track.id);
    toAdd.put("uri", track.uri);
    toAdd.put("title", track.name);
    toAdd.put("artist", track.artists.get(0).name);
    toAdd.put("album", track.album.name);
    toAdd.put("lengthMs", track.duration_ms);
    toAdd.put("albumArtSmall", track.album.images.get(2).url);
    toAdd.put("albumArtMedium", track.album.images.get(1).url);
    toAdd.put("albumArtLarge", track.album.images.get(0).url);
    return toAdd;
  }
}
