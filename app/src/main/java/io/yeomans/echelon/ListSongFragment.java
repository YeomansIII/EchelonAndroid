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
public class ListSongFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    String getUrl;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
        getUrl = getArguments().getString("get_url");
        //ownerId = getArguments().getString("owner_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_song_fragment,
                container, false);

        listSongs();

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
    }

    public void listSongs() {
        final ListSongFragment songSearchFrag = this;
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    String spotifyTracksUrl = getUrl;

                    HttpGet get2 = new HttpGet(spotifyTracksUrl);
                    Log.d("SearchSongs", get2.getURI().toString());
                    get2.addHeader("Authorization", "Bearer " + mainActivity.spotifyAuthToken);
                    HttpResponse responseGet2 = client.execute(get2);
                    HttpEntity resEntityGet2 = responseGet2.getEntity();
                    if (resEntityGet2 != null) {
                        return EntityUtils.toString(resEntityGet2);
                    }
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
                return "{\"error\":\"error\"}";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("ListSongs", "" + msg);

                try {
                    JSONObject json = new JSONObject(msg).getJSONObject("tracks");
                    JSONArray items = json.getJSONArray("items");
                    Log.d("Search Song", items.toString());
                    LinearLayout songList = (LinearLayout) view.findViewById(R.id.listSongListLayout);
                    songList.removeAllViews();
                    songListArr = new ArrayList<>();
                    for (int i = 0; i < items.length() - 1; i++) {
                        JSONObject curObj;
                        try {
                            curObj = items.getJSONObject(i).getJSONObject("track");
                        } catch (JSONException je) {
                            curObj = items.getJSONObject(i);
                        }

                        RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
                        ImageView albumArtImage = (ImageView) rt.findViewById(R.id.albumArtImage);
                        TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
                        TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);

                        songTitleText.setText(curObj.getString("name"));
                        songArtistText.setText(curObj.getJSONArray("artists").getJSONObject(0).getString("name"));
                        new ImageLoadTask(curObj.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url"), albumArtImage).execute();
                        rt.setTag(curObj.getString("id"));
                        rt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (RelativeLayout view : songListArr) {
                                    view.setOnClickListener(null);
                                }
                                view.setBackgroundColor(Color.DKGRAY);
                                FirebaseCommon.addSong((String) v.getTag(), mainActivity);
                            }
                        });
                        songListArr.add(rt);
                        songList.addView(rt);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }
}