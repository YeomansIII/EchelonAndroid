package io.yeomans.echelon;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
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

    private static final String TAG = "BrowseSongsFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

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
    View loadOverlay;

    RecyclerView mRecyclerView;
    SonglistRecyclerAdapter songListRA;
    RecyclerView.LayoutManager mLayoutManager;
    List<Track> tracks;

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
        tracks = new ArrayList<>();
        getSongs();
        //ownerId = getArguments().getString("owner_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.list_song_fragment,
                container, false);

        loadOverlay = view.findViewById(R.id.songListLoadOverlay);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.songListRecyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        songListRA = new SonglistRecyclerAdapter(tracks, what);
        songListRA.setOnSongClickListener(new SonglistRecyclerAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(SonglistRecyclerAdapter.ViewHolder viewHolder) {
                viewHolder.itemView.setBackgroundColor(Color.GRAY);
                FirebaseCommon.addSong(viewHolder.trackId, mainActivity);
            }
        });
        mRecyclerView.setAdapter(songListRA);

        if (tracks.size() != 0) {
            loadOverlay.setVisibility(View.GONE);
        }

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
    }

    public void getSongs() {
        if (what == SEARCH) {
            Log.d("WhatList", "Search");
            mainActivity.spotify.searchTracks(searchQuery, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    mainActivity.toolbar.setTitle(searchQuery);
                    tracks.addAll(tracksPager.tracks.items);
                    songListRA.notifyDataSetChanged();
                    loadOverlay.setVisibility(View.GONE);
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
                    mainActivity.toolbar.setTitle(playlist.name);
                    for (PlaylistTrack t : playlist.tracks.items) {
                        tracks.add(t.track);
                    }
                    songListRA.notifyDataSetChanged();
                    loadOverlay.setVisibility(View.GONE);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.wtf("WhatList", error.toString());
                }
            });
        }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }
}