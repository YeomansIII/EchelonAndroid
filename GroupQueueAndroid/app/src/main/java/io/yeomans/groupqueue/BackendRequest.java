package io.yeomans.groupqueue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by jason on 6/30/15.
 */
public class BackendRequest {

    //PROD
    public static final String BASE_URL = "https://api.echelonapp.io/";

    //DEV
    //public static final String BASE_URL = "http://192.168.1.8:8000/";

    private String url;
    private String method;
    private Header[] headers;
    private ArrayList<NameValuePair> paramaters;
    private String jsonEntity;
    private MainActivity mainActivity;
    private Context mainContext;

    public BackendRequest(String method, MainActivity mainActivity) {
        this.method = method;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String method, String url, String jsonEntity, MainActivity mainActivity) {
        this.method = method;
        this.url = url;
        this.jsonEntity = jsonEntity;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String method, String url, MainActivity mainActivity) {
        this.method = method;
        this.url = url;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String url, Header[] headers, MainActivity mainActivity) {
        this.method = "GET";
        this.url = url;
        this.headers = headers;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String method, String url, Header[] headers, MainActivity mainActivity) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String url, ArrayList<NameValuePair> paramaters, MainActivity mainActivity) {
        this.method = "POST";
        this.url = url;
        this.paramaters = paramaters;
        this.mainActivity = mainActivity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public ArrayList<NameValuePair> getParamaters() {
        return paramaters;
    }

    public void setParamaters(ArrayList<NameValuePair> paramaters) {
        this.paramaters = paramaters;
    }

    public String getJsonEntity() {
        return jsonEntity;
    }

    public void setJsonEntity(String jsonEntity) {
        this.jsonEntity = jsonEntity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Context getMainContext() {
        return mainContext;
    }

    public void setMainContext(Context mainContext) {
        this.mainContext = mainContext;
    }

    public static void login(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("POST")) {
            final MainActivity activity = be.getMainActivity();
            AsyncTask<BackendRequest, Void, String> get = new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(BASE_URL + be.getUrl());
                        post.setEntity(new UrlEncodedFormEntity(be.getParamaters()));
                        HttpResponse responsePost = client.execute(post);
                        HttpEntity resEntityPost = responsePost.getEntity();
                        String response = "";
                        if (resEntityPost != null) {
                            //do something with the response
                            response = EntityUtils.toString(resEntityPost);
                            if (response.contains("token")) {
                                JSONObject jsonT = new JSONObject(response);
                                String token = jsonT.getString("token");
                                Log.d("login", "Token: " + token);
                                SharedPreferences settings = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(MainActivity.PREF_ECHELON_API_TOKEN, token);

                                HttpGet get = new HttpGet(BASE_URL + "apiv1/listeners/my-user-info/");
                                get.addHeader("Authorization", "Token " + token);
                                HttpResponse responseGet = client.execute(get);
                                HttpEntity resEntityGet = responseGet.getEntity();
                                String response2 = EntityUtils.toString(resEntityGet);

                                JSONObject json = new JSONObject(response2);

                                HttpPut put = new HttpPut(BASE_URL + "apiv1/listeners/" + json.getInt("pk") + "/");
                                JSONObject putJson = new JSONObject("{}");
                                put.addHeader("Authorization", "Token " + token);
                                String regId = settings.getString(MainActivity.PROPERTY_REG_ID, null);
                                putJson.put("gcm_id", regId);
                                StringEntity se = new StringEntity(putJson.toString());
                                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                put.setEntity(se);
                                HttpResponse responsePut = client.execute(put);

                                editor.putInt(MainActivity.PREF_LISTENER_PK, json.getInt("pk"));
                                editor.putString(MainActivity.PREF_LISTENER_OWNER_OF, json.getString("owner_of"));
                                JSONObject jsonUser = json.getJSONObject("user");
                                editor.putInt(MainActivity.PREF_LISTENER_USER_PK, jsonUser.getInt("pk"));
                                editor.putString(MainActivity.PREF_LISTENER_USERNAME, jsonUser.getString("username"));
                                //editor.putString(MainActivity.PREF_LISTENER_EMAIL, jsonUser.getString("email"));
                                editor.commit();

                                return response;
                            }
                            return response;
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    if (msg.contains("non_field_errors")) {
                        TextView loginErrorText = (TextView) activity.findViewById(R.id.loginErrorText);
                        try {
                            JSONObject json = new JSONObject(msg);
                            String loginError = json.getJSONArray("non_field_errors").getString(0);
                            loginErrorText.setText(loginError);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loginErrorText.setText("Unknown Error Occurred");
                        }
                        //loginErrorText.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    } else if (msg.contains("token")) {
                        SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        pref.edit().putBoolean(MainActivity.PREF_LISTENER_LOGGED_IN, true).commit();

                        if (activity.checkPlayServices()) {
                            activity.gcm = GoogleCloudMessaging.getInstance(activity);
                            activity.registerInBackground();
                        } else {
                            Log.i("GCM", "No valid Google Play Services APK found.");
                        }

                        Fragment fragment = new HomeFragment();
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else {
                        TextView loginErrorText = (TextView) activity.findViewById(R.id.loginErrorText);
                        loginErrorText.setText("Could not connect to server.");
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void createAccount(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("POST")) {
            final BackendRequest be2 = be;
            AsyncTask<BackendRequest, Void, String> get = new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(BASE_URL + be.getUrl());
                        post.setEntity(new StringEntity(be.getJsonEntity()));
                        HttpResponse responsePost = client.execute(post);
                        HttpEntity resEntityPost = responsePost.getEntity();
                        String response = "";
                        if (resEntityPost != null) {
                            //do something with the response
                            response = EntityUtils.toString(resEntityPost);
                            return response;
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    if (msg.contains("account_created")) {
                        be2.setUrl("api-token-auth/");
                        BackendRequest.login(be2);
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void activateJoinGroup(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("PUT")) {
            final BackendRequest be2 = be;
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPut put = new HttpPut(BASE_URL + be.getUrl());
                        SharedPreferences settings = be.getMainActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        String token = settings.getString(MainActivity.PREF_ECHELON_API_TOKEN, null);
                        put.addHeader("Authorization", "Token " + token);
                        if (be.getJsonEntity() != null) {
                            put.setEntity(new StringEntity(be.getJsonEntity()));
                        }
                        Log.d("Group", put.getURI().toString());
                        HttpResponse responseGet = client.execute(put);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    Activity activity = be2.getMainActivity();
                    if (msg.contains("join_errors")) {
                        String joinError = "Unknown error occurred";
                        try {
                            joinError = (new JSONObject(msg)).getJSONArray("join_errors").getString(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ((TextView) activity.findViewById(R.id.joinGroupIdError)).setText(joinError);
                    } else {
                        JSONObject json;
                        try {
                            json = new JSONObject(msg);
                            SharedPreferences groupSettings = activity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
                            SharedPreferences.Editor editor = groupSettings.edit();
                            editor.putInt(MainActivity.PREF_GROUP_PK, json.getInt("pk"));
                            JSONObject ownerUserJson = json.getJSONObject("owner").getJSONObject("user");
                            String ownerUsername = ownerUserJson.getString("username");
                            editor.putInt(MainActivity.PREF_GROUP_OWNER_PK, ownerUserJson.getInt("pk"));
                            editor.putString(MainActivity.PREF_GROUP_OWNER_USERNAME, ownerUsername);
                            editor.commit();
                            Log.d("Group", "Group: " + ownerUsername);
                            Boolean leader = true;
                            if (be2.getUrl().contains("join")) {
                                leader = false;
                            }
                            Fragment fragment = new GroupFragment();
                            Bundle bundle = new Bundle();
                            bundle.putStringArray("extra_stuff", new String[]{"" + leader, "" + leader});
                            fragment.setArguments(bundle);
                            FragmentManager fragmentManager = be2.getMainActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container, fragment, "GROUP_FRAG");
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute(be, null, null);
        }
    }


    public static void refreshGroupQueue(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("GET")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        SharedPreferences settings = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        SharedPreferences settings2 = activity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
                        HttpGet get = new HttpGet(BASE_URL + "apiv1/queuegroups/" + settings2.getInt("group_pk", -1));
                        String token = settings.getString(MainActivity.PREF_ECHELON_API_TOKEN, null);
                        get.addHeader("Authorization", "Token " + token);
                        Log.d("RefreshGroupQueue", get.getURI().toString());
                        HttpResponse responseGet = client.execute(get);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            JSONObject responseJson = new JSONObject(EntityUtils.toString(resEntityGet));

                            String spotifyTracksUrl = "https://api.spotify.com/v1/tracks/?ids=";
                            JSONArray trackQueueJson = responseJson.getJSONArray("track_queue");
                            String spotifyResponse = "";
                            ArrayList<SpotifySong> backStack = activity.backStack;
                            ArrayList<SpotifySong> playqueue = activity.playQueue;
                            backStack.clear();
                            playqueue.clear();
                            if (trackQueueJson.length() > 0) {
                                for (int i = 0; i < trackQueueJson.length(); i++) {
                                    JSONObject trackJson = trackQueueJson.getJSONObject(i);
                                    spotifyTracksUrl += trackJson.getString("spotify_id") + ",";
                                }
                                spotifyTracksUrl = spotifyTracksUrl.replaceAll(",$", "");

                                HttpGet get2 = new HttpGet(spotifyTracksUrl);
                                Log.d("RefreshGroupQueue", get2.getURI().toString());
                                HttpResponse responseGet2 = client.execute(get2);
                                HttpEntity resEntityGet2 = responseGet2.getEntity();
                                if (resEntityGet2 != null) {
                                    spotifyResponse = EntityUtils.toString(resEntityGet2);
                                    JSONObject json = new JSONObject(spotifyResponse);
                                    JSONArray items = json.getJSONArray("tracks");
                                    Log.d("RefreshQueue", "tracks: " + items.length());
                                    for (int p = 0; p < items.length(); p++) {
                                        JSONObject spotifyTrackJson = items.getJSONObject(p);
                                        JSONObject trackJson = trackQueueJson.getJSONObject(p);
                                        boolean isBackStack = trackJson.getBoolean("played");
                                        JSONObject album = spotifyTrackJson.getJSONObject("album");
                                        JSONArray images = album.getJSONArray("images");
                                        SpotifySong ss = new SpotifySong(
                                                trackJson.getInt("pk"),
                                                isBackStack,
                                                spotifyTrackJson.getString("id"),
                                                spotifyTrackJson.getString("uri"),
                                                spotifyTrackJson.getString("name"),
                                                spotifyTrackJson.getJSONArray("artists").getJSONObject(0).getString("name"),
                                                album.getString("name"),
                                                spotifyTrackJson.getInt("duration_ms"),
                                                images.getJSONObject(2).getString("url"),
                                                images.getJSONObject(1).getString("url"),
                                                images.getJSONObject(0).getString("url")
                                        );
                                        if (ss.isBackStack()) {
                                            backStack.add(ss);
                                        } else {
                                            playqueue.add(ss);
                                        }
                                    }
                                }
                            } else {

                            }
                            SharedPreferences.Editor edit = settings2.edit();
                            //edit.putString("group_current_queue_json", spotifyResponse).commit();
                            return spotifyResponse;
                        }
                    } catch (Exception je) {
                        je.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                    if (groupFragment != null && groupFragment.isVisible()) {
                        groupFragment.refreshQueueList();
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void queueNewSong(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("PUT")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPut put = new HttpPut(BASE_URL + be.getUrl());
                        SharedPreferences settings = be.getMainActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        String token = settings.getString(MainActivity.PREF_ECHELON_API_TOKEN, null);
                        put.addHeader("Authorization", "Token " + token);
                        put.setEntity(new StringEntity(be.getJsonEntity()));
                        Log.d("QueueSong", put.getURI().toString());
                        HttpResponse responseGet = client.execute(put);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    SongSearchFragment songSearchFragment = (SongSearchFragment) fragmentManager.findFragmentByTag("SEARCH_FRAG");
                    GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //if(groupFragment != null && groupFragment.isVisible()) {

                    View view = activity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    Log.d("QueueSong", "Song Queue Complete");
                    fragmentTransaction.replace(R.id.container, groupFragment).commit();
                    //}
                }
            }.execute(be, null, null);
        }
    }

    public static void updateSong(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("PUT")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPut put = new HttpPut(BASE_URL + be.getUrl());
                        SharedPreferences settings = be.getMainActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        String token = settings.getString(MainActivity.PREF_ECHELON_API_TOKEN, null);
                        put.addHeader("Authorization", "Token " + token);
                        put.setEntity(new StringEntity(be.getJsonEntity()));
                        Log.d("QueueSong", put.getURI().toString());
                        HttpResponse responseGet = client.execute(put);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                }
            }.execute(be, null, null);
        }
    }

    public static void resetGroup(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("GET")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpGet get = new HttpGet(BASE_URL + be.getUrl());
                        SharedPreferences settings = be.getMainActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        String token = settings.getString(MainActivity.PREF_ECHELON_API_TOKEN, null);
                        get.addHeader("Authorization", "Token " + token);
                        Log.d("Group", get.getURI().toString());
                        HttpResponse responseGet = client.execute(get);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    if (groupFragment != null && groupFragment.isVisible()) {
                        //groupFragment.;
                        fragmentTransaction.remove(groupFragment).add(R.id.container, new HomeFragment(), "HOME_FRAG").addToBackStack(null).commit();
                        groupFragment.onDestroy();
                    }
                }
            }.execute(be, null, null);
        }
    }
}
