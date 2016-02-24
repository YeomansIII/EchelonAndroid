package io.yeomans.echelon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;

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
        Picasso.with(context).load(curPlaylist.images.get(0).url).placeholder(R.drawable.ic_music_circle_black_48dp).into(holder.image);
        holder.name.setText(curPlaylist.name);

        holder.what = ListSongFragment.PLAYLIST;
        holder.userId = curPlaylist.owner.id;
        holder.playlistId = curPlaylist.id;
        if (mOnPlaylistClickListener != null) {
            holder.mOnPlaylistClick = mOnPlaylistClickListener;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView name;
        public ImageView image;
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

            name = (TextView) itemView.findViewById(R.id.playlistTitleText);
            image = (ImageView) itemView.findViewById(R.id.playlistArtImage);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Log.d("PlaylistViewHolder", "Clicked! " + getAdapterPosition());
            if (mOnPlaylistClick != null) {
                mOnPlaylistClick.onPlaylistClick(this);
            }
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
