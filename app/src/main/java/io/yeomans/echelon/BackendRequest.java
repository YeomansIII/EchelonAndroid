package io.yeomans.echelon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
                        activity.setUpNavDrawerAndActionBar();
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
                            fragmentTransaction.commitAllowingStateLoss();
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
                            JSONArray participantsJson = responseJson.getJSONArray("participants");
                            String jsonArrayString = participantsJson.toString();
                            Log.d("BackendRequest", jsonArrayString);
                            Log.d("BackendRequest", "Wrote to storage: " + settings2.edit().putString(MainActivity.PREF_GROUP_PARTICIPANTS_JSON, jsonArrayString).commit());
                            JSONArray trackQueueJson = responseJson.getJSONArray("track_queue");
                            String spotifyResponse = "";
                            LinkedList<SpotifySong> backStack = activity.backStack;
                            LinkedList<SpotifySong> playqueue = activity.playQueue;
                            backStack.clear();
                            playqueue.clear();
                            int startAt = -1;
                            if (trackQueueJson.length() > 0) {
                                for (int i = 0; i < trackQueueJson.length(); i++) {
                                    JSONObject trackJson = trackQueueJson.getJSONObject(i);
                                    if (!trackJson.getBoolean("played")) {
                                        if (startAt == -1) {
                                            startAt = i;
                                        }
                                        spotifyTracksUrl += trackJson.getString("spotify_id") + ",";
                                    }
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
                                        JSONObject trackJson = trackQueueJson.getJSONObject(startAt + p);
                                        boolean isBackStack = trackJson.getBoolean("played");
                                        JSONObject album = spotifyTrackJson.getJSONObject("album");
                                        JSONArray images = album.getJSONArray("images");
                                        SpotifySong ss = new SpotifySong(
                                                isBackStack,
                                                spotifyTrackJson.getString("id"),
                                                spotifyTrackJson.getString("uri"),
                                                spotifyTrackJson.getString("name"),
                                                spotifyTrackJson.getJSONArray("artists").getJSONObject(0).getString("name"),
                                                album.getString("name"),
                                                spotifyTrackJson.getInt("duration_ms"),
                                                images.getJSONObject(2).getString("url"),
                                                images.getJSONObject(1).getString("url"),
                                                images.getJSONObject(0).getString("url"),
                                                trackJson.getInt("rating"),
                                                trackJson.getBoolean("now_playing")
                                        );
                                        if (ss.isBackStack()) {
                                            backStack.add(ss);
                                        } else if (ss.isNowPlaying()) {
                                            playqueue.addFirst(ss);
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
                        groupFragment.queueFragment.refreshQueueList();
                        groupFragment.participantsFragment.buildParticiantList();
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
                    AddSongFragment addSongFragment = (AddSongFragment) fragmentManager.findFragmentByTag("ADD_SONG_FRAG");
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
                    Log.d("UpdateSongPost", msg);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(activity.mainActivityClass);
//                    builder.setTitle("Title");
//                    builder.setMessage(msg);
//                    AlertDialog dialog = builder.show();
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
                    Log.d("Group", "backend leaving1");

                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    if (groupFragment != null && groupFragment.isVisible()) {
                        //groupFragment.;
                        Log.d("Group", "backend leaving2");
                        fragmentTransaction.replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
                        groupFragment.leaveGroup();
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void getSpotifyMeAuth(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("GET")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        String spotifyTracksUrl = "https://api.spotify.com/v1/me";
                        HttpClient client = new DefaultHttpClient();
                        HttpGet get = new HttpGet(spotifyTracksUrl);
                        get.addHeader("Authorization", "Bearer " + activity.spotifyAuthToken);
                        Log.d("GetSpotifyMe", get.getURI().toString());
                        HttpResponse responseGet = client.execute(get);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (Exception je) {
                        je.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.d("GetSpotifyMe", msg);
                    try {
                        JSONObject spotify = new JSONObject(msg);

                        SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(MainActivity.PREF_SPOTIFY_UID, spotify.getString("id"));
                        editor.putString(MainActivity.PREF_SPOTIFY_DISPLAY_NAME, spotify.getString("display_name"));
                        editor.putString(MainActivity.PREF_SPOTIFY_EMAIL, spotify.getString("email"));
                        editor.putString(MainActivity.PREF_SPOTIFY_EXT_URL, spotify.getJSONObject("external_urls").getString("spotify"));
                        editor.putString(MainActivity.PREF_SPOTIFY_COUNTRY, spotify.getString("country"));
                        editor.putString(MainActivity.PREF_SPOTIFY_IMAGE_URL, spotify.getJSONArray("images").getJSONObject(0).getString("url"));
                        editor.putString(MainActivity.PREF_SPOTIFY_PRODUCT, spotify.getString("product"));
                        editor.putString(MainActivity.PREF_SPOTIFY_TYPE, spotify.getString("type"));
                        editor.putString(MainActivity.PREF_SPOTIFY_URI, spotify.getString("uri"));
                        editor.apply();

                        JSONObject tokenAuth = new JSONObject();
                        tokenAuth.put("uid", spotify.getString("id") + "_spotify");
                        tokenAuth.put("access_token", activity.spotifyAuthToken);
                        BackendRequest bee = new BackendRequest("POST", MainActivity.ECHELONADO_URL + "spotify-auth/", tokenAuth.toString(), activity);
                        BackendRequest.getFirebaseSpotifyToken(bee);
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void getFirebaseSpotifyToken(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("POST")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(be.getUrl());
                        post.setEntity(new StringEntity(be.getJsonEntity()));
                        HttpResponse responseGet = client.execute(post);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.d("GetFirebaseSpotifyToken", msg);
                    activity.myFirebaseRef.authWithCustomToken(msg, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticationError(FirebaseError error) {
                            Log.wtf("GetFirebaseSpotifyToken", "Login Failed! " + error.getMessage());
                        }

                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Log.d("GetFirebaseSpotifyToken", "Login Succeeded!");
                            FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
                            activity.setUpNavDrawerAndActionBar();
                            String fUid = authData.getUid();
                            SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                            pref.edit().putString(MainActivity.PREF_FIREBASE_UID, fUid).commit();
                            Log.d("GetFirebaseSpotifyToken", fUid);
                            Firebase user = activity.myFirebaseRef.child("users").child(fUid);

                            user.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("GetFirebaseSpotifyToken", "DATA CHANGED");
                                    if (dataSnapshot.getValue() == null) {
                                        Log.d("GetFirebaseSpotifyToken", "New User, creating in DB");
                                        SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                                        Map<String, Object> userInfo = new HashMap<>();
                                        userInfo.put("id", pref.getString(MainActivity.PREF_SPOTIFY_UID, null));
                                        userInfo.put("display_name", pref.getString(MainActivity.PREF_SPOTIFY_DISPLAY_NAME, null));
                                        userInfo.put("email", pref.getString(MainActivity.PREF_SPOTIFY_EMAIL, null));
                                        userInfo.put("country", pref.getString(MainActivity.PREF_SPOTIFY_COUNTRY, null));
                                        userInfo.put("ext_url", pref.getString(MainActivity.PREF_SPOTIFY_EXT_URL, null));
                                        userInfo.put("image_url", pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, null));
                                        userInfo.put("product", pref.getString(MainActivity.PREF_SPOTIFY_PRODUCT, null));
                                        userInfo.put("type", pref.getString(MainActivity.PREF_SPOTIFY_TYPE, null));
                                        userInfo.put("uri", pref.getString(MainActivity.PREF_SPOTIFY_URI, null));

                                        String uid = pref.getString(MainActivity.PREF_FIREBASE_UID, null);

                                        if (uid != null) {
                                            Firebase user = new Firebase(MainActivity.FIREBASE_URL + "users/" + uid);
                                            user.setValue(userInfo);
                                            user.removeEventListener(this);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                        }
                    });
//                    AlertDialog.Builder builder = new AlertDialog.Builder(activity.mainActivityClass);
//                    builder.setTitle("Title");
//                    builder.setMessage(msg);
//                    AlertDialog dialog = builder.show();
                }
            }.execute(be, null, null);
        }
    }
}
