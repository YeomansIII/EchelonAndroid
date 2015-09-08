package io.yeomans.echelon;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

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

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
    }

    public void select() {
        if (!selected) {
            selected = true;
            getFeaturedPlaylists();
        }
    }

    public void getFeaturedPlaylists() {
        final BrowseSongsFragment songSearchFrag = this;
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    String spotifyTracksUrl = "https://api.spotify.com/v1/browse/featured-playlists?limit=6";

                    HttpGet get2 = new HttpGet(spotifyTracksUrl);
                    Log.d("GettingPlaylists", get2.getURI().toString());
                    get2.addHeader("Authorization", "Bearer " + mainActivity.spotifyAuthToken);
                    HttpResponse responseGet2 = client.execute(get2);
                    HttpEntity resEntityGet2 = responseGet2.getEntity();
                    if (resEntityGet2 != null) {
                        String spotifyResponse = EntityUtils.toString(resEntityGet2);
                        return spotifyResponse;
                    }
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
                return "{\"error\":\"error\"}";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("GettingPlaylists", msg);

                try {
                    JSONObject json = new JSONObject(msg).getJSONObject("playlists");
                    JSONArray items = json.getJSONArray("items");
                    Log.d("GettingPlaylists", items.toString());
                    LinearLayout playlistList = (LinearLayout) view.findViewById(R.id.featuredPlaylistsListLayout);
                    playlistList.removeAllViews();
                    playlistListArr = new ArrayList<>();
                    for (int i = 0; i < items.length() - 1; i++) {
                        JSONObject curObj = items.getJSONObject(i);

                        RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.playlist_item, null);
                        ImageView albumArtImage = (ImageView) rt.findViewById(R.id.playlistArtImage);
                        TextView songTitleText = (TextView) rt.findViewById(R.id.playlistTitleText);

                        songTitleText.setText(curObj.getString("name"));
                        new ImageLoadTask(curObj.getJSONArray("images").getJSONObject(0).getString("url"), albumArtImage).execute();
                        rt.setTag(curObj.getString("id"));
//                        rt.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                try {
//                                    for (RelativeLayout view : songListArr) {
//                                        view.setOnClickListener(null);
//                                    }
//                                    view.setBackgroundColor(Color.DKGRAY);
//                                    JSONObject queueJson = new JSONObject("{}");
//                                    queueJson.put("spotify_id", (String) v.getTag());
//                                    BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/queue-song/", queueJson.toString(), (MainActivity) songSearchFrag.getActivity());
//                                    BackendRequest.queueNewSong(be);
//                                } catch (JSONException je) {
//                                    je.printStackTrace();
//                                }
//                            }
//                        });
                        playlistListArr.add(rt);
                        playlistList.addView(rt);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }
}