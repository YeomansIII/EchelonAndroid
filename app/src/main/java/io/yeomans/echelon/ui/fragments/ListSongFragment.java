package io.yeomans.echelon.ui.fragments;

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
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.kaaes.spotify.webapi.core.models.Playlist;
import io.github.kaaes.spotify.webapi.core.models.PlaylistTrack;
import io.github.kaaes.spotify.webapi.core.models.Track;
import io.github.kaaes.spotify.webapi.core.models.TracksPager;
import io.yeomans.echelon.R;
import io.yeomans.echelon.callbacks.AddSongCallback;
import io.yeomans.echelon.interfaces.Picker;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.ui.adapters.SonglistRecyclerAdapter;
import io.yeomans.echelon.util.Dependencies;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

  public LayoutManagerType mCurrentLayoutManagerType;

  public final static char SEARCH = '0';
  public final static char PLAYLIST = '1';
  private char what;
  private String searchQuery;
  private String userId;
  private String playlistId;

  private View view;
  private ArrayList<RelativeLayout> songListArr;
  String getUrl;
  MainActivity mainActivity;
  View loadOverlay;
  @Bind(R.id.songListPlaylistPickerButton)
  public ImageButton songListPlaylistPickerButton;

  RecyclerView mRecyclerView;
  SonglistRecyclerAdapter songListRA;
  RecyclerView.LayoutManager mLayoutManager;
  List<Track> tracks;
  Bundle arguments;
  boolean isPlayListPicker;

  Dependencies dependencies;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dependencies = Dependencies.INSTANCE;
    mainActivity = (MainActivity) getActivity();
    arguments = getArguments();
    what = arguments.getChar("what");
    isPlayListPicker = arguments.getBoolean("isPlayListPicker");
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
    ButterKnife.bind(this, view);
    if (isPlayListPicker) {
      songListPlaylistPickerButton.setVisibility(View.VISIBLE);
      songListPlaylistPickerButton.setOnClickListener(this);
    }
    mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));

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
        Call<Track> call = dependencies.getSpotify().getTrack(viewHolder.trackId);
        call.enqueue(new AddSongCallback(mainActivity));
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
    if (v == songListPlaylistPickerButton) {
      Log.d(TAG, "will send picked: " + (getParentFragment() != null && getParentFragment() instanceof Picker));
      if (getParentFragment() != null && getParentFragment() instanceof Picker) {
        Fragment parentFragment = getParentFragment();
        ((Picker) parentFragment).pickedPlaylist(userId, playlistId);
      }
    }
  }

  public void getSongs() {
    if (what == SEARCH) {
      Log.d("WhatList", "Search");
      Call<TracksPager> call = dependencies.getSpotify().searchTracks(searchQuery);
      call.enqueue(new Callback<TracksPager>() {
        @Override
        public void onResponse(Call<TracksPager> call, Response<TracksPager> response) {
          mainActivity.toolbar.setTitle(searchQuery);
          tracks.addAll(response.body().tracks.items);
          songListRA.notifyDataSetChanged();
          loadOverlay.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call<TracksPager> call, Throwable t) {
          Log.wtf("WhatList", t.toString() + "   " + t.getMessage());
        }
      });
    } else if (what == PLAYLIST) {
      Log.d("WhatList", "Playlist");
      Call<Playlist> call = dependencies.getSpotify().getPlaylist(userId, playlistId);
      call.enqueue(new Callback<Playlist>() {
        @Override
        public void onResponse(Call<Playlist> call, Response<Playlist> response) {
          Playlist playlist = response.body();
          mainActivity.toolbar.setTitle(playlist.name);
          for (PlaylistTrack t : playlist.tracks.items) {
            tracks.add(t.track);
          }
          songListRA.notifyDataSetChanged();
          loadOverlay.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call<Playlist> call, Throwable t) {
          Log.wtf("WhatList", t.toString() + "   " + t.getMessage());
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
