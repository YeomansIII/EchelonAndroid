package io.yeomans.echelon.ui.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.github.kaaes.spotify.webapi.core.models.ArtistSimple;
import io.github.kaaes.spotify.webapi.core.models.Track;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.fragments.SongDetailBottomSheetFragment;
import io.yeomans.echelon.util.FirebaseCommon;

/**
 * Created by jason on 2/9/16.
 */
public class SonglistRecyclerAdapter extends RecyclerView.Adapter<SonglistRecyclerAdapter.ViewHolder> {

  List<Track> mTracks;
  List<SpotifySong> sTracks;
  Context context;
  OnSongClickListener mOnSongClickListener;
  AppCompatActivity activity;
  char what;
  boolean isSpotifySong = false, vote = false;

  public SonglistRecyclerAdapter() {
    mTracks = new ArrayList<>();
  }

  public SonglistRecyclerAdapter(List<Track> tracks, char what) {
    mTracks = tracks;
    this.what = what;
  }

  public SonglistRecyclerAdapter(List<SpotifySong> tracks, boolean vote) {
    sTracks = tracks;
    isSpotifySong = true;
    this.vote = vote;
  }

  @Override
  public SonglistRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    // Inflate the custom layout
    View contactView = inflater.inflate(R.layout.song_item, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(contactView, context);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(SonglistRecyclerAdapter.ViewHolder holder, int position) {
    Track curTrack;
    if (isSpotifySong) {
      holder.isSpotifySong = true;
      curTrack = sTracks.get(position);
      SpotifySong curSS = (SpotifySong) curTrack;
      holder.key = curSS.getKey();
      holder.upVoted = curSS.isUpVoted();
      if (holder.upVoted) {
        holder.voteUp.setSelected(true);
      } else {
        holder.voteUp.setSelected(false);
      }
    } else {
      holder.isSpotifySong = false;
      curTrack = mTracks.get(position);
    }
    String imgUrl;
    try {
      imgUrl = curTrack.album.images.get(2).url;
    } catch (IndexOutOfBoundsException e) {
      try {
        imgUrl = curTrack.album.images.get(1).url;
      } catch (IndexOutOfBoundsException e2) {
        imgUrl = curTrack.album.images.get(0).url;
      }
    }

    Picasso.with(context).load(imgUrl).placeholder(R.drawable.ic_music_circle_black_36dp).into(holder.image);
    holder.title.setText(curTrack.name);
    String artistText = "";
    List<ArtistSimple> artists = curTrack.artists;
    for (ArtistSimple a : artists) {
      artistText += a.name + ", ";
    }
    holder.artist.setText(artistText.replaceAll(", $", ""));
    holder.trackId = curTrack.id;

    if (vote) {
      holder.voteUp.setVisibility(View.VISIBLE);
      holder.moreButton.setVisibility(View.GONE);
    } else {
      holder.voteUp.setVisibility(View.GONE);
      holder.moreButton.setVisibility(View.VISIBLE);
    }

    if (mOnSongClickListener != null) {
      holder.mOnSongClick = mOnSongClickListener;
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    public TextView title, artist;
    public ImageView image;
    public String key;
    public String trackId;
    public boolean upVoted, isSpotifySong;
    public ImageButton voteUp, moreButton;
    public OnSongClickListener mOnSongClick;
    Context context;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView, Context context) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      this.context = context;
      title = (TextView) itemView.findViewById(R.id.songTitleText);
      artist = (TextView) itemView.findViewById(R.id.songArtistText);
      image = (ImageView) itemView.findViewById(R.id.songAlbumArtImage);
      image.setOnClickListener(this);
      voteUp = (ImageButton) itemView.findViewById(R.id.songItemVoteUpButton);
      voteUp.setOnClickListener(this);
      itemView.setOnClickListener(this);
      moreButton = (ImageButton) itemView.findViewById(R.id.songItemMoreButton);
    }


    @Override
    public void onClick(View view) {
      if (view == voteUp && !key.equals("") && isSpotifySong) {
        if (upVoted) {
          FirebaseCommon.rankSong(key, 0);
          voteUp.setSelected(false);
          upVoted = false;
        } else {
          FirebaseCommon.rankSong(key, 1);
          voteUp.setSelected(true);
          upVoted = true;
        }
      } else if (view == image) {
        SongDetailBottomSheetFragment songBottom = SongDetailBottomSheetFragment.newInstance(trackId);
        songBottom.show(((AppCompatActivity) context).getSupportFragmentManager(), "SONG_DETAIL");
      } else if (mOnSongClick != null) {
        mOnSongClick.onSongClick(this);
      }
    }
  }

  @Override
  public int getItemCount() {
    if (isSpotifySong) {
      return sTracks.size();
    }
    return mTracks.size();
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  public void setOnSongClickListener(OnSongClickListener onSongClickListener) {
    mOnSongClickListener = onSongClickListener;
  }

  public interface OnSongClickListener {
    void onSongClick(ViewHolder viewHolder);
  }
}
