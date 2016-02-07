package io.yeomans.echelon;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jason on 7/10/15.
 */
public class ListSongFragment extends Fragment implements View.OnClickListener {

    final static char SEARCH = '0';
    final static char PLAYLIST = '1';
    private char what;
    private String searchQuery;
    private String userId;
    private String playlistId;

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    String getUrl;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
        what = getArguments().getChar("what");
        Log.d("WhatList", "" + what);
        if (what == SEARCH) {
            try {
                searchQuery = URLEncoder.encode(getArguments().getString("searchQuery"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (what == PLAYLIST) {
            userId = getArguments().getString("userId");
            playlistId = getArguments().getString("playlistId");
        }
        //ownerId = getArguments().getString("owner_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_song_fragment,
                container, false);

        listSongs();

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
    }

    public void listSongs() {
        if (what == SEARCH) {
            Log.d("WhatList", "Search");
            mainActivity.spotify.searchTracks(searchQuery, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    generateLayout(tracksPager.tracks.items);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.wtf("WhatList", error.toString());
                }
            });
        } else if (what == PLAYLIST) {
            Log.d("WhatList", "Playlist");
            mainActivity.spotify.getPlaylist(userId, playlistId, new Callback<Playlist>() {
                @Override
                public void success(Playlist playlist, Response response) {
                    generateLayout(playlist.tracks.items);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.wtf("WhatList", error.toString());
                }
            });
        }
    }

    public void generateLayout(List items) {
        Log.d("Search Song", items.toString());
        LinearLayout songList = (LinearLayout) view.findViewById(R.id.listSongListLayout);
        songList.removeAllViews();
        songListArr = new ArrayList<>();
        for (int i = 0; i < items.size() - 1; i++) {
            Track curObj;
            Object obj = items.get(i);
            if (obj instanceof Track) {
                Log.d("WhatList", "Track");
                curObj = (Track) obj;
            } else if (obj instanceof PlaylistTrack) {
                Log.d("WhatList", "Playlist");
                curObj = ((PlaylistTrack) obj).track;
            } else {
                Log.wtf("Echelon", "This shouldn't be happening...");
                break;
            }

            SongItemView siv = new SongItemView(getContext(),
                    curObj.name,
                    curObj.artists.get(0).name,
                    curObj.album.images.get(2).url,
                    curObj.id,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (RelativeLayout view : songListArr) {
                                view.setOnClickListener(null);
                            }
                            view.setBackgroundColor(Color.DKGRAY);
                            FirebaseCommon.addSong((String) v.getTag(), mainActivity);
                        }
                    });

            songListArr.add(siv);
            songList.addView(siv);
        }
    }
}