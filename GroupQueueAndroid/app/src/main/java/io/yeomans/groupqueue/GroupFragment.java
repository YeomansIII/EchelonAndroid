package io.yeomans.groupqueue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jason on 6/26/15.
 */
public class GroupFragment extends Fragment {

    private String groupId;
    private String playId;
    private boolean leader;

    private View view;
    private ArrayList<RelativeLayout> songListArr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        playId = "";
        Bundle startingIntentBundle = this.getArguments();
        if (startingIntentBundle != null) {
            String[] extras = startingIntentBundle.getStringArray("extra_stuff");
            //playId = extras[0];
            leader = Boolean.parseBoolean(extras[1]);
            Log.wtf("Test", "leader: " + leader);
        }
        Log.wtf("Intent Extras", playId);
        if (leader) {
            //((TextView) findViewById(R.id.syncRoom)).setText("Sync Room:" + playId);

//            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
//                    AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
//            builder.setScopes(new String[]{"user-read-private", "streaming"});
//            AuthenticationRequest request = builder.build();
//            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        } else {
            Log.d("Group", "Not leader");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_fragment,
                container, false);

        ///////
        SharedPreferences groupSettings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        ///////
        ((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString("group_owner_username", "error"));

        this.view = view;
        refreshQueueFromPref();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (view != null) {
            // refreshQueueFromPref();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void refreshQueueFromPref() {
        Log.d("RefreshQueue", "Refresh Queue From Perf");

        try {
            SharedPreferences pref = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
            String queue_json = pref.getString("group_current_queue_json", null);

            if(queue_json != null) {
                JSONObject json = new JSONObject(queue_json);
                JSONArray items = json.getJSONArray("tracks");
                Log.d("RefreshQueue", items.toString());
                LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
                songList.removeAllViews();
                songListArr = new ArrayList<RelativeLayout>();
                for (int i = 0; i < items.length() - 1; i++) {
                    JSONObject curObj = items.getJSONObject(i);

                    RelativeLayout rt = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.song_item, null);
                    ImageView albumArtImage = (ImageView) rt.findViewById(R.id.albumArtImage);
                    TextView songTitleText = (TextView) rt.findViewById(R.id.songTitleText);
                    TextView songArtistText = (TextView) rt.findViewById(R.id.songArtistText);

                    songTitleText.setText(curObj.getString("name"));
                    songArtistText.setText(curObj.getJSONArray("artists").getJSONObject(0).getString("name"));
                    new ImageLoadTask(curObj.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url"), albumArtImage).execute();
                    //tv.setTag(curObj.getString("uri"));
                    rt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //trackUri = (String) v.getTag();
                            for (RelativeLayout view : songListArr) {
                                view.setBackgroundColor(Color.TRANSPARENT);
                            }
                            v.setBackgroundColor(Color.GRAY);
                        }
                    });
                    songListArr.add(rt);
                    songList.addView(rt);
                }
            } else {
                LinearLayout songList = (LinearLayout) view.findViewById(R.id.queueListLayout);
                TextView tv = new TextView(getActivity().getApplicationContext());
                tv.setText("No songs in queue. Search for a song!");
                songList.addView(tv);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }


//    public String searchSongs(String searchTerms) {
//
//        try {
//            AsyncTask<String, Void, String> get = new AsyncTask<String, Void, String>() {
//                @Override
//                protected String doInBackground(String... params) {
//                    String a = "";
//                    HttpURLConnection urlConnection;
//                    try {
//                        Log.d("GET", "URL: " + params[0]);
//                        URL urlget = new URL(params[0]);
//                        urlConnection = (HttpURLConnection) urlget.openConnection();
//                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                        java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
//
//                        //return "works";
//                        String returner = s.hasNext() ? s.next() : "";
//                        urlConnection.disconnect();
//                        return returner;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return "{\"success\":false, \"error\":\"could not connect to server\"}";
//                }
//
//                @Override
//                protected void onPostExecute(String msg) {
//                    try {
//                        Log.d("GET", "PostExecute Song Search");
//                        JSONObject json = new JSONObject(msg);
//                        JSONArray items = json.getJSONObject("tracks").getJSONArray("items");
//                        LinearLayout songList = (LinearLayout) findViewById(R.id.songList);
//                        songListArr = new ArrayList<TextView>();
//                        for (int i = 0; i < items.length(); i++) {
//                            JSONObject curObj = items.getJSONObject(i);
//                            TextView tv = new TextView(getApplicationContext());
//                            tv.setText(curObj.getString("name") + " by " + curObj.getJSONArray("artists").getJSONObject(0).getString("name"));
//                            tv.setTextSize(30f);
//                            tv.setTextColor(Color.WHITE);
//                            tv.setTag(curObj.getString("uri"));
//                            tv.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    trackUri = (String) v.getTag();
//                                    for (TextView view : songListArr) {
//                                        view.setBackgroundColor(Color.TRANSPARENT);
//                                    }
//                                    v.setBackgroundColor(Color.GRAY);
//                                }
//                            });
//                            songListArr.add(tv);
//                            songList.addView(tv);
//                        }
//                    } catch (JSONException je) {
//                        je.printStackTrace();
//                    }
//                }
//            }.execute("https://api.spotify.com/v1/search?q=" + URLEncoder.encode(searchTerms, "UTF-8") + "&type=track", null, null);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
}
