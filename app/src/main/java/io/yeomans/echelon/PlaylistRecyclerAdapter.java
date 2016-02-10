package io.yeomans.echelon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

    public PlaylistRecyclerAdapter(List<PlaylistSimple> playlists) {
        mPlaylists = playlists;
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
        Picasso.with(context).load(mPlaylists.get(position).images.get(0).url).into(holder.image);
        holder.name.setText(mPlaylists.get(position).name);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView name;
        public ImageView image;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.playlistTitleText);
            image = (ImageView) itemView.findViewById(R.id.playlistArtImage);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
