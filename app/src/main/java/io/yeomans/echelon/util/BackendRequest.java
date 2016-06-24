package io.yeomans.echelon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.yeomans.echelon.BuildConfig;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.ui.fragments.HomeFragment;

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
                        if (BuildConfig.DEBUG_MODE) {
                            tokenAuth.put("development", true);
                        }
                        BackendRequest bee = new BackendRequest("POST", MainActivity.ECHELON_PROD_WORKER_URL + "spotify-auth/", tokenAuth.toString(), activity);
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
                    FirebaseAuth auth;
                    if (BuildConfig.DEBUG) {
                        auth = FirebaseAuth.getInstance(activity.firebaseApp);
                    } else {
                        auth = FirebaseAuth.getInstance();
                    }
                    auth.signInWithCustomToken(msg).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Log.d("GetFirebaseSpotifyToken", "Login Succeeded!");
                            String fUid = authResult.getUser().getUid();
                            DatabaseReference user = activity.myFirebaseRef.child("users/" + fUid);
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
                                    DatabaseReference user = activity.myFirebaseRef.child("users/" + uid);
                                    DatabaseReference participant = activity.myFirebaseRef.child("participants/" + uid);
                                    if (dataSnapshot.getValue() == null) {
                                        Log.d("GetFirebaseSpotifyToken", "New User, creating in DB");
                                        Map<String, Object> userInfo = new HashMap<>();
                                        userInfo.put("email", pref.getString(MainActivity.PREF_SPOTIFY_EMAIL, null));
                                        userInfo.put("product", pref.getString(MainActivity.PREF_SPOTIFY_PRODUCT, null));
                                        userInfo.put("type", pref.getString(MainActivity.PREF_SPOTIFY_TYPE, null));

                                        Map<String, Object> participantInfo = new HashMap<>();
                                        participantInfo.put("country", pref.getString(MainActivity.PREF_SPOTIFY_COUNTRY, null));
                                        participantInfo.put("display_name", pref.getString(MainActivity.PREF_SPOTIFY_DISPLAY_NAME, null));
                                        participantInfo.put("id", pref.getString(MainActivity.PREF_SPOTIFY_UID, null));
                                        participantInfo.put("ext_url", pref.getString(MainActivity.PREF_SPOTIFY_EXT_URL, null));
                                        participantInfo.put("uri", pref.getString(MainActivity.PREF_SPOTIFY_URI, null));
                                        participantInfo.put("image_url", pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, null));


                                        if (uid != null) {
                                            user.setValue(userInfo);
                                            participant.setValue(participantInfo);
                                        }
                                    } else {
                                        SharedPreferences.Editor prefEdit = pref.edit();
                                        if (dataSnapshot.hasChild("display_name")) {
                                            prefEdit.putString(MainActivity.PREF_USER_DISPLAY_NAME, (String) dataSnapshot.child("display_name").getValue());
                                        }
                                        participant.child("ext_url").setValue(pref.getString(MainActivity.PREF_SPOTIFY_EXT_URL, null));
                                        prefEdit.putString(MainActivity.PREF_USER_EXT_URL, pref.getString(MainActivity.PREF_SPOTIFY_EXT_URL, null));

                                        participant.child("image_url").setValue(pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, null));
                                        prefEdit.putString(MainActivity.PREF_USER_IMAGE_URL, pref.getString(MainActivity.PREF_SPOTIFY_IMAGE_URL, null));

                                        prefEdit.apply();
                                    }
                                    participant.child("online").onDisconnect().setValue(false);
                                    participant.child("online").setValue(true);
                                    activity.completeLogin();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("GetFirebaseSpotifyToken", "Login Failed! " + e.getMessage());
                        }
                    });
                }
            }.execute(be, null, null);
        }
    }
}
