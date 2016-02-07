package io.yeomans.echelon;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by jason on 12/8/15.
 */
public class SongItemView extends RelativeLayout {
    String title, artist, albumArtUrl, id;
    TextView titleText, artistText;
    ImageView albumImage;
    View.OnClickListener onClickListener;

    public SongItemView(Context context) {
        super(context);
    }

    public SongItemView(Context context, String title, String artist, String albumArtUrl) {
        super(context);
        this.title = title;
        this.artist = artist;
        this.albumArtUrl = albumArtUrl;
        initView();
    }

    public SongItemView(Context context, String title, String artist, String albumArtUrl, String id, View.OnClickListener onClickListener) {
        super(context);
        this.title = title;
        this.artist = artist;
        this.albumArtUrl = albumArtUrl;
        this.id = id;
        this.onClickListener = onClickListener;
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.song_item, null);
        titleText = (TextView) view.findViewById(R.id.songTitleText);
        artistText = (TextView) view.findViewById(R.id.songArtistText);
        albumImage = (ImageView) view.findViewById(R.id.albumArtImage);
        if (title != null) {
            titleText.setText(title);
        }
        if (artist != null) {
            artistText.setText(artist);
        }
        if (albumArtUrl != null) {
            Picasso.with(getContext()).load(albumArtUrl).into(albumImage);
        }
        if (id != null) {
            view.setTag(id);
        }
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        addView(view);
    }
}
