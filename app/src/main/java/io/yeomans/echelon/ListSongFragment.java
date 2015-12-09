package io.yeomans.echelon;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        //final ListSongFragment songSearchFrag = this;
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String response = "";
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    Log.d("ListSong", "Getting Songs");
                    String spotifyTracksUrl = getUrl;
                    url = new URL(spotifyTracksUrl);

                    urlConnection = (HttpURLConnection) url
                            .openConnection();
                    if (mainActivity.spotifyAuthToken != null) {
                        urlConnection.setRequestProperty("Authorization", "Bearer " + mainActivity.spotifyAuthToken);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace(); //If you want further info on failure...
                    }
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
                    LinearLayout songList = (LinearLayout) view.findViewById(R.id.listSongListLayout);;
                    songList.removeAllViews();
                    songListArr = new ArrayList<>();
                    for (int i = 0; i < items.length() - 1; i++) {
                        JSONObject curObj;
                        try {
                            curObj = items.getJSONObject(i).getJSONObject("track");
                        } catch (JSONException je) {
                            curObj = items.getJSONObject(i);
                        }

                        SongItemView siv = new SongItemView(getContext(),
                                curObj.getString("name"),
                                curObj.getJSONArray("artists").getJSONObject(0).getString("name"),
                                curObj.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url"),
                                curObj.getString("id"),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        for (RelativeLayout view : songListArr) {
                                            view.setOnClickListener(null);
                                        }
                                        view.setBackgroundColor(Color.DKGRAY);
                                        FirebaseCommon.addSong((String) v.getTag(), mainActivity);
                                    }
                                });

                        //mainActivity.imgLoader.DisplayImage(curObj.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url"), albumArtImage);
                        songListArr.add(siv);
                        songList.addView(siv);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }
}