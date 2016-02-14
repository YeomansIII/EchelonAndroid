package io.yeomans.echelon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jason on 2/9/16.
 */
public class SonglistRecyclerAdapter extends RecyclerView.Adapter<SonglistRecyclerAdapter.ViewHolder> {

    List<Track> mTracks;
    Context context;
    OnSongClickListener mOnSongClickListener;
    char what;

    public SonglistRecyclerAdapter() {
        mTracks = new ArrayList<>();
    }

    public SonglistRecyclerAdapter(List<Track> tracks, char what) {
        mTracks = tracks;
        this.what = what;
    }

    @Override
    public SonglistRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.song_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SonglistRecyclerAdapter.ViewHolder holder, int position) {
        Track curTrack = mTracks.get(position);
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

        Picasso.with(context).load(imgUrl).into(holder.image);
        holder.title.setText(curTrack.name);
        String artistText = "";
        List<ArtistSimple> artists = curTrack.artists;
        for (ArtistSimple a : artists) {
            artistText += a.name + ", ";
        }
        holder.artist.setText(artistText.replaceAll(", $", ""));
        holder.trackId = curTrack.id;

        if (mOnSongClickListener != null) {
            holder.mOnSongClick = mOnSongClickListener;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView title, artist;
        public ImageView image;
        public String trackId;
        public OnSongClickListener mOnSongClick;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.songTitleText);
            artist = (TextView) itemView.findViewById(R.id.songArtistText);
            image = (ImageView) itemView.findViewById(R.id.songAlbumArtImage);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (mOnSongClick != null) {
                mOnSongClick.onSongClick(this);
            }
        }
    }

    @Override
    public int getItemCount() {
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
