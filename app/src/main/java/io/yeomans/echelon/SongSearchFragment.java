package io.yeomans.echelon;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
public class SongSearchFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ArrayList<RelativeLayout> songListArr;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_search_fragment,
                container, false);

        view.findViewById(R.id.searchSongButton).setOnClickListener(this);

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == getActivity().findViewById(R.id.searchSongButton)) {
            Log.d("Button", "Song Search Button");
            searchSongs(((TextInputLayout) view.findViewById(R.id.searchSongEditWrapper)).getEditText().getText().toString());
        }
    }

    public void searchSongs(String query) {
        final SongSearchFragment songSearchFrag = this;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String query = params[0];
                    HttpClient client = new DefaultHttpClient();
                    String spotifyTracksUrl = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query, "UTF-8") + "&type=track";

                    HttpGet get2 = new HttpGet(spotifyTracksUrl);
                    Log.d("SearchSongs", get2.getURI().toString());
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
                Log.d("SearchSongs", "" + msg);

                try {
                    JSONObject json = new JSONObject(msg).getJSONObject("tracks");
                    JSONArray items = json.getJSONArray("items");
                    Log.d("Search Song", items.toString());
                    LinearLayout songList = (LinearLayout) view.findViewById(R.id.searchSongListLayout);
                    songList.removeAllViews();
                    songListArr = new ArrayList<>();
                    for (int i = 0; i < items.length() - 1; i++) {
                        JSONObject curObj = items.getJSONObject(i);

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
                                FragmentManager fm = mainActivity.getSupportFragmentManager();
                                GroupFragment gf = (GroupFragment) fm.findFragmentByTag("GROUP_FRAG");
                                fm.beginTransaction().replace(R.id.container, gf).commit();
                            }
                        });
                        songListArr.add(rt);
                        songList.addView(rt);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(query, null, null);
    }
}