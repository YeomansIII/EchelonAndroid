package io.yeomans.echelon.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.kaaes.spotify.webapi.core.models.PlaylistSimple;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.fragments.ListSongFragment;

/**
 * Created by jason on 2/9/16.
 */
public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<PlaylistRecyclerAdapter.ViewHolder> {

  List<PlaylistSimple> mPlaylists;
  Context context;
  OnPlaylistClickListener mOnPlaylistClickListener;

  public PlaylistRecyclerAdapter() {
    mPlaylists = new ArrayList<PlaylistSimple>();
  }

  public PlaylistRecyclerAdapter(List<PlaylistSimple> playlists) {
    mPlaylists = playlists;
  }

  public void setData(List<PlaylistSimple> playlists) {
    mPlaylists = playlists;
  }

  public void addData(List<PlaylistSimple> playlists) {
    mPlaylists.addAll(playlists);
  }

  @Override
  public PlaylistRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    // Inflate the custom layout
    View contactView = inflater.inflate(R.layout.playlist_item, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(contactView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(PlaylistRecyclerAdapter.ViewHolder holder, int position) {
    PlaylistSimple curPlaylist = mPlaylists.get(position);
    Picasso.with(context).load(curPlaylist.images.get(0).url).placeholder(R.drawable.ic_music_circle_black_48dp).into(holder.image,
      PicassoPalette.with(curPlaylist.images.get(0).url, holder.image)
        .use(PicassoPalette.Profile.VIBRANT)
        .intoBackground(holder.innerLayout, PicassoPalette.Swatch.RGB)
        .intoTextColor(holder.name, PicassoPalette.Swatch.BODY_TEXT_COLOR)
        .intoCallBack(holder)
    );
    holder.name.setText(curPlaylist.name);

    holder.what = ListSongFragment.PLAYLIST;
    holder.userId = curPlaylist.owner.id;
    holder.playlistId = curPlaylist.id;
    if (mOnPlaylistClickListener != null) {
      holder.mOnPlaylistClick = mOnPlaylistClickListener;
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PicassoPalette.CallBack {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    @Bind(R.id.playlistItemInnerLayout)
    public RelativeLayout innerLayout;
    @Bind(R.id.playlistTitleText)
    public TextView name;
    @Bind(R.id.playlistArtImage)
    public ImageView image;
    @Bind(R.id.playlistItemMoreButton)
    public ImageButton moreButton;
    public char what;
    public String userId;
    public String playlistId;
    public OnPlaylistClickListener mOnPlaylistClick;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      ButterKnife.bind(this, itemView);
      itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
      Log.d("PlaylistViewHolder", "Clicked! " + getAdapterPosition());
      if (mOnPlaylistClick != null) {
        mOnPlaylistClick.onPlaylistClick(this);
      }
    }

    @Override
    public void onPaletteLoaded(Palette palette) {
      Palette.Swatch swatch = palette.getVibrantSwatch();
      int enabled, pressed;
      if (swatch == null) {
        swatch = palette.getLightVibrantSwatch();
      }
      if (swatch == null) {
        enabled = palette.getVibrantColor(Color.WHITE);
        pressed = palette.getDarkVibrantColor(Color.GRAY);
      } else {
        enabled = swatch.getBodyTextColor();
        pressed = swatch.getTitleTextColor();
      }
      int[][] states = new int[][]{
        new int[]{android.R.attr.state_enabled}, // enabled
        new int[]{android.R.attr.state_pressed}  // pressed
      };
      int[] colors = new int[]{
        enabled,
        pressed
      };
      moreButton.setImageTintList(new ColorStateList(states, colors));
    }
  }

  @Override
  public int getItemCount() {
    return mPlaylists.size();
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  public void setOnPlaylistClickListener(OnPlaylistClickListener onPlaylistClickListener) {
    mOnPlaylistClickListener = onPlaylistClickListener;
  }

  public interface OnPlaylistClickListener {
    public void onPlaylistClick(ViewHolder viewHolder);
  }
}
