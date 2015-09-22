package io.yeomans.echelon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

                            user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("GetFirebaseSpotifyToken", "DATA CHANGED");
                                    SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                                    if (dataSnapshot.getValue() == null) {
                                        Log.d("GetFirebaseSpotifyToken", "New User, creating in DB");
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
