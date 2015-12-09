package io.yeomans.echelon;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        final BrowseSongsFragment songSearchFrag = this;
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String response = "";
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    String spotifyTracksUrl = "https://api.spotify.com/v1/browse/featured-playlists?limit=6";
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
                Log.d("GettingPlaylists", msg);

                try {
                    JSONObject msgJson = new JSONObject(msg);
                    ((TextView) view.findViewById(R.id.featuredPlaylistsMessage)).setText(msgJson.getString("message"));
                    JSONObject json = msgJson.getJSONObject("playlists");
                    JSONArray items = json.getJSONArray("items");
                    Log.d("GettingPlaylists", items.toString());
                    LinearLayout playlistListLeft = (LinearLayout) view.findViewById(R.id.featuredPlaylistsListLayoutLeft);
                    LinearLayout playlistListRight = (LinearLayout) view.findViewById(R.id.featuredPlaylistsListLayoutRight);
                    playlistListLeft.removeAllViews();
                    playlistListRight.removeAllViews();
                    playlistListArr = new ArrayList<>();
                    boolean colLeft = true;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject curObj = items.getJSONObject(i);

                        RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.playlist_item, null);
                        ImageView albumArtImage = (ImageView) rt.findViewById(R.id.playlistArtImage);
                        TextView songTitleText = (TextView) rt.findViewById(R.id.playlistTitleText);

                        songTitleText.setText(curObj.getString("name"));
                        //new ImageLoadTask(curObj.getJSONArray("images").getJSONObject(0).getString("url"), albumArtImage).execute();
                        mainActivity.imgLoader.DisplayImage(curObj.getJSONArray("images").getJSONObject(0).getString("url"), albumArtImage);
                        String url = "https://api.spotify.com/v1/users/"
                                + curObj.getJSONObject("owner").getString("id")
                                + "/playlists/"
                                + curObj.getString("id");
                        rt.setTag(R.string.get_url, url);
                        rt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
                                ListSongFragment lsf = new ListSongFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("get_url", v.getTag(R.string.get_url).toString());
                                lsf.setArguments(bundle);
                                ft.replace(R.id.container, lsf, "SONG_LIST_FRAG").addToBackStack(null).commit();
                            }
                        });
                        playlistListArr.add(rt);
                        if (colLeft) {
                            playlistListLeft.addView(rt);
                            colLeft = false;
                        } else {
                            playlistListRight.addView(rt);
                            colLeft = true;
                        }
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }
}