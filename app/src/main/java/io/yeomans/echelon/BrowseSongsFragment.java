package io.yeomans.echelon;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.FeaturedPlaylists;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jason on 7/10/15.
 */
public class BrowseSongsFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ArrayList<RelativeLayout> playlistListArr;
    MainActivity mainActivity;
    boolean selected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
        selected = false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_songs_fragment,
                container, false);

        getFeaturedPlaylists();

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

    public void getFeaturedPlaylists() {
        mainActivity.spotify.getFeaturedPlaylists(new Callback<FeaturedPlaylists>() {
            @Override
            public void success(FeaturedPlaylists featuredPlaylists, Response response) {
                RecyclerView rvPlaylists = (RecyclerView) view.findViewById(R.id.browsePlaylistRecyclerView);
                PlaylistRecyclerAdapter playlistRA = new PlaylistRecyclerAdapter(featuredPlaylists.playlists.items);
                rvPlaylists.setAdapter(playlistRA);
                rvPlaylists.setLayoutManager(new GridLayoutManager(getContext(), 2));

//                ((TextView) view.findViewById(R.id.featuredPlaylistsMessage)).setText(featuredPlaylists.message);
//                List<PlaylistSimple> items = featuredPlaylists.playlists.items;
//                Log.d("GettingPlaylists", items.toString());
//                LinearLayout playlistListLeft = (LinearLayout) view.findViewById(R.id.featuredPlaylistsListLayoutLeft);
//                LinearLayout playlistListRight = (LinearLayout) view.findViewById(R.id.featuredPlaylistsListLayoutRight);
//                playlistListLeft.removeAllViews();
//                playlistListRight.removeAllViews();
//                playlistListArr = new ArrayList<>();
//                boolean colLeft = true;
//                for (int i = 0; i < items.size(); i++) {
//                    PlaylistSimple curObj = items.get(i);
//
//                    RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.playlist_item, null);
//                    ImageView albumArtImage = (ImageView) rt.findViewById(R.id.playlistArtImage);
//                    TextView songTitleText = (TextView) rt.findViewById(R.id.playlistTitleText);
//
//                    songTitleText.setText(curObj.name);
//                    Picasso.with(getContext()).load(curObj.images.get(0).url).into(albumArtImage);
//                    rt.setTag(R.string.userId, curObj.owner.id);
//                    rt.setTag(R.string.playlistId, curObj.id);
//                    rt.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
//                            ListSongFragment lsf = new ListSongFragment();
//                            Bundle bundle = new Bundle();
//                            bundle.putChar("what", ListSongFragment.PLAYLIST);
//                            bundle.putString("userId", v.getTag(R.string.userId).toString());
//                            bundle.putString("playlistId", v.getTag(R.string.playlistId).toString());
//                            lsf.setArguments(bundle);
//                            ft.replace(R.id.container, lsf, "SONG_LIST_FRAG").addToBackStack(null).commit();
//                        }
//                    });
//                    playlistListArr.add(rt);
//                    if (colLeft) {
//                        playlistListLeft.addView(rt);
//                        colLeft = false;
//                    } else {
//                        playlistListRight.addView(rt);
//                        colLeft = true;
//                    }
//                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}