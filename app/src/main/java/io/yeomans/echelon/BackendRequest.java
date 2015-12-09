package io.yeomans.echelon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getJsonEntity() {
        return jsonEntity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static void getSpotifyMeAuth(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("GET")) {
            final MainActivity activity = be.getMainActivity();
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    String response = "";
                    URL url;
                    HttpURLConnection urlConnection = null;
                    try {
                        url = new URL("https://api.spotify.com/v1/me");

                        urlConnection = (HttpURLConnection) url
                                .openConnection();
                        urlConnection.setRequestProperty("Authorization", "Bearer " + activity.spotifyAuthToken);
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
                        if (spotify.getJSONArray("images").length() > 0) {
                            editor.putString(MainActivity.PREF_SPOTIFY_IMAGE_URL, spotify.getJSONArray("images").getJSONObject(0).getString("url"));
                        }
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
                    BackendRequest be = params[0];
                    String response = "";
                    URL url;
                    HttpURLConnection urlConnection = null;
                    try {
                        url = new URL(be.getUrl());
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoOutput(true);
                        urlConnection.setDoInput(true);
                        urlConnection.setUseCaches(false);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("content-type", "application/json");
                        DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());
                        request.writeBytes(be.getJsonEntity());
                        request.flush();
                        request.close();

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
                    Log.d("GetFirebaseSpotifyToken", msg);
                    activity.myFirebaseRef.authWithCustomToken(msg, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticationError(FirebaseError error) {
                            Log.d("GetFirebaseSpotifyToken", "Login Failed! " + error.getMessage());
                        }

                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Log.d("GetFirebaseSpotifyToken", "Login Succeeded!");
                            String fUid = authData.getUid();
                            Firebase user = activity.myFirebaseRef.child("users/" + fUid);
                            SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                            pref.edit().putString(MainActivity.PREF_FIREBASE_UID, fUid).putString(MainActivity.PREF_USER_AUTH_TYPE, "spotify").commit();
                            FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            Fragment groupFragment = fragmentManager.findFragmentByTag("GROUP_FRAGMENT");
                            if (groupFragment == null || !groupFragment.isVisible()) {
                                fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
                            }
                            activity.setUpNavDrawerAndActionBar();
                            Log.d("GetFirebaseSpotifyToken", fUid);

                            user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("GetFirebaseSpotifyToken", "DATA CHANGED");
                                    SharedPreferences pref = activity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
                                    String uid = pref.getString(MainActivity.PREF_FIREBASE_UID, null);
                                    Firebase user = new Firebase(MainActivity.FIREBASE_URL + "users/" + uid);
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

                                        if (uid != null) {
                                            user.setValue(userInfo);
                                        }
                                    } else {
                                        SharedPreferences.Editor prefEdit = pref.edit();
                                        if (dataSnapshot.hasChild("display_name")) {
                                            prefEdit.putString(MainActivity.PREF_USER_DISPLAY_NAME, (String) dataSnapshot.child("display_name").getValue());
                                        }
                                        if (dataSnapshot.hasChild("ext_url")) {
                                            prefEdit.putString(MainActivity.PREF_USER_EXT_URL, (String) dataSnapshot.child("ext_url").getValue());
                                        }
                                        if (dataSnapshot.hasChild("image_url")) {
                                            prefEdit.putString(MainActivity.PREF_USER_IMAGE_URL, (String) dataSnapshot.child("image_url").getValue());
                                        }
                                        prefEdit.apply();
                                        activity.checkGroup();
                                    }
                                    user.child("online").onDisconnect().setValue(false);
                                    user.child("online").setValue(true);
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
