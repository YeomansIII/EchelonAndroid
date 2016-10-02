package io.yeomans.echelon.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.kaaes.spotify.webapi.core.models.FeaturedPlaylists;
import io.github.kaaes.spotify.webapi.core.models.PlaylistSimple;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.ui.adapters.PlaylistRecyclerAdapter;
import io.yeomans.echelon.ui.other.GridSpacingItemDecoration;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.ViewUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 7/10/15.
 */
public class BrowseSongsFragment extends Fragment implements View.OnClickListener {


  private static final String TAG = "BrowseSongsFragment";
  private static final String KEY_LAYOUT_MANAGER = "layoutManager";
  private static final int SPAN_COUNT = 2;

  private enum LayoutManagerType {
    GRID_LAYOUT_MANAGER,
    LINEAR_LAYOUT_MANAGER
  }

  protected LayoutManagerType mCurrentLayoutManagerType;

  private View view;
  MainActivity mainActivity;
  boolean selected;
  View loadOverlay;
  RecyclerView rvPlaylists;
  PlaylistRecyclerAdapter playlistRA;
  RecyclerView.LayoutManager mLayoutManager;
  List<PlaylistSimple> playlists;
  String message;
  Dependencies dependencies;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setHasOptionsMenu(true);
    dependencies = Dependencies.INSTANCE;
    mainActivity = (MainActivity) getActivity();
    selected = false;
    playlists = new ArrayList<>();
    getFeaturedPlaylists();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.browse_songs_fragment,
      container, false);

    //mainActivity.actionBar.setElevation(0);
    mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));

    //mLayoutManager = new GridLayoutManager(getActivity(), 2);
    if (message != null) {
      mainActivity.toolbar.setTitle(message);
    }
    loadOverlay = view.findViewById(R.id.browsePlaylistLoadOverlay);

    rvPlaylists = (RecyclerView) view.findViewById(R.id.browsePlaylistRecyclerView);
    mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);

    mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;

    if (savedInstanceState != null) {
      // Restore saved layout manager type.
      mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
        .getSerializable(KEY_LAYOUT_MANAGER);
    }
    setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

    rvPlaylists.addItemDecoration(new GridSpacingItemDecoration(2, ViewUtils.dpToPx(4, getResources()), false));

    playlistRA = new PlaylistRecyclerAdapter(playlists);
    playlistRA.setOnPlaylistClickListener(new PlaylistRecyclerAdapter.OnPlaylistClickListener() {
      @Override
      public void onPlaylistClick(PlaylistRecyclerAdapter.ViewHolder viewHolder) {
        FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
        ListSongFragment lsf = new ListSongFragment();
        Bundle bundle = new Bundle();
        bundle.putChar("what", viewHolder.what);
        bundle.putString("userId", viewHolder.userId);
        bundle.putString("playlistId", viewHolder.playlistId);
        lsf.setArguments(bundle);
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, lsf, "SONG_LIST_FRAG").addToBackStack(null).commit();
      }
    });
    rvPlaylists.setAdapter(playlistRA);

    if (playlists.size() != 0) {
      loadOverlay.setVisibility(View.GONE);
    }

    this.view = view;
    return view;
  }

  @Override
  public void onClick(View v) {
  }

  public void select() {
//        if (!selected) {
//            selected = true;
//            getFeaturedPlaylists();
//        }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Save currently selected layout manager.
    savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
    super.onSaveInstanceState(savedInstanceState);
  }

  /**
   * Set RecyclerView's LayoutManager to the one given.
   *
   * @param layoutManagerType Type of layout manager to switch to.
   */
  public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
    int scrollPosition = 0;

    // If a layout manager has already been set, get current scroll position.
    if (rvPlaylists.getLayoutManager() != null) {
      scrollPosition = ((LinearLayoutManager) rvPlaylists.getLayoutManager())
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

    rvPlaylists.setLayoutManager(mLayoutManager);
    rvPlaylists.scrollToPosition(scrollPosition);
  }

  public void getFeaturedPlaylists() {
    Locale uLocale = Locale.getDefault();
    String format = "yyyy-MM-dd'T'HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(format, uLocale);
    String dateTime = sdf.format(new Date());
    Log.d("Playlists", uLocale.toString() + "    " + dateTime);
    Map<String, Object> options = new HashMap<>();
    options.put("locale", uLocale.toString());
    options.put("timestamp", dateTime);
    Call<FeaturedPlaylists> call = dependencies.getSpotify().getFeaturedPlaylists();
    call.enqueue(new Callback<FeaturedPlaylists>() {
      @Override
      public void onResponse(Call<FeaturedPlaylists> call, Response<FeaturedPlaylists> response) {
        Log.i("Playlists", "Get playlist results");
        FeaturedPlaylists featuredPlaylists = response.body();
        mainActivity.toolbar.setTitle(featuredPlaylists.message);
        message = featuredPlaylists.message;
        playlists.addAll(featuredPlaylists.playlists.items);
        playlistRA.notifyDataSetChanged();
        loadOverlay.setVisibility(View.GONE);
      }

      @Override
      public void onFailure(Call<FeaturedPlaylists> call, Throwable t) {
        Log.wtf("WhatList", t.toString() + "   " + t.getMessage());
      }
    });
  }
}
