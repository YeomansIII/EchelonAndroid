package io.yeomans.echelon.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.kaaes.spotify.webapi.core.models.UserPrivate;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.BackendSpotifyAuth;
import io.yeomans.echelon.models.Token;
import io.yeomans.echelon.ui.fragments.HomeFragment;
import io.yeomans.echelon.util.BackendService;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jason on 6/25/16.
 */
public class WelcomeActivity extends AppCompatActivity implements ConnectionStateCallback {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    public static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    public static final String REDIRECT_URI = "echelonapp://callback";
    public static final int REQUEST_CODE = 9001;

    public AuthenticationResponse authResponse;
    String spotifyAuthToken;
    Dependencies dependencies;
    Snackbar echelonErrorSnackie, spotifyErrorSnackie;
    UserPrivate spotifyUser;
    AuthResult authResulter;
    WelcomeActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_fragment);
        ButterKnife.bind(this);
        Dependencies.INSTANCE.init(getApplicationContext());
        dependencies = Dependencies.INSTANCE;
        activity = this;
        echelonErrorSnackie = Snackbar.make(findViewById(R.id.loginRootRelativeLayout), "Error logging in to Echelon", Snackbar.LENGTH_SHORT);
        spotifyErrorSnackie = Snackbar.make(findViewById(R.id.loginRootRelativeLayout), "Error logging in to Spotify", Snackbar.LENGTH_SHORT);
        if (dependencies.getPreferences().getBoolean(PreferenceNames.PREF_SPOTIFY_AUTHENTICATED, false)) {
            authenticateSpotify();
        }
    }

    @OnClick(R.id.spotifyLoginButton)
    public void authenticateSpotify() {
        Log.d("Authentication", "Authenticating Anonymously with Spotify");
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(MainActivity.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, MainActivity.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, MainActivity.REQUEST_CODE, request);
    }

    @OnClick(R.id.anonymousLoginButton)
    public void authenticateAnonymously() {
        Log.d("Authentication", "Authenticating Anonymously");
        dependencies.getAuth().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                DatabaseReference thisParticipantRef = dependencies.getDatabase().getReference("participants/" + authResult.getUser().getUid());
                thisParticipantRef.child("online").onDisconnect().setValue(false);
                thisParticipantRef.child("online").setValue(true);
                thisParticipantRef.child("display_name").setValue("Anonymous");
                dependencies.getPreferences().edit().putString(PreferenceNames.PREF_FIREBASE_UID, authResult.getUser().getUid())
                        .putString(PreferenceNames.PREF_USER_AUTH_TYPE, "anonymous")
                        .putString(PreferenceNames.PREF_USER_DISPLAY_NAME, "Anonymous")
                        .apply();
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment groupFragment = fragmentManager.findFragmentByTag("GROUP_FRAGMENT");
                if (groupFragment == null || !groupFragment.isVisible()) {
                    fragmentManager.beginTransaction().replace(R.id.container, new HomeFragment(), "HOME_FRAG").commit();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            authResponse = AuthenticationClient.getResponse(resultCode, intent);
            switch (authResponse.getType()) {
                case TOKEN:
                    spotifyAuthToken = authResponse.getAccessToken();
                    dependencies.getPreferences().edit()
                            .putBoolean(PreferenceNames.PREF_SPOTIFY_AUTHENTICATED, true)
                            .putString(PreferenceNames.PREF_SPOTIFY_AUTH_TOKEN, spotifyAuthToken)
                            .apply();
                    dependencies.authSpotify(spotifyAuthToken);
                    Call<UserPrivate> call = dependencies.getSpotify().getMe();
                    call.enqueue(new Callback<UserPrivate>() {
                        @Override
                        public void onResponse(Call<UserPrivate> call, Response<UserPrivate> response) {
                            spotifyUser = response.body();
                            SharedPreferences.Editor editor = dependencies.getPreferences().edit();
                            editor.putString(PreferenceNames.PREF_SPOTIFY_UID, spotifyUser.id);
                            editor.putString(PreferenceNames.PREF_SPOTIFY_DISPLAY_NAME, spotifyUser.display_name);
                            editor.putString(PreferenceNames.PREF_SPOTIFY_EMAIL, spotifyUser.email);
                            editor.putString(PreferenceNames.PREF_SPOTIFY_EXT_URL, spotifyUser.external_urls.get("spotify"));
                            editor.putString(PreferenceNames.PREF_SPOTIFY_COUNTRY, spotifyUser.country);
                            if (spotifyUser.images.size() > 0) {
                                editor.putString(PreferenceNames.PREF_SPOTIFY_IMAGE_URL, spotifyUser.images.get(0).url);
                            }
                            editor.putString(PreferenceNames.PREF_SPOTIFY_PRODUCT, spotifyUser.product);
                            editor.putString(PreferenceNames.PREF_SPOTIFY_TYPE, spotifyUser.type);
                            editor.putString(PreferenceNames.PREF_SPOTIFY_URI, spotifyUser.uri);
                            editor.apply();

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(MainActivity.ECHELON_PROD_WORKER_URL)
                                    .client(new OkHttpClient())
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            BackendService backendService = retrofit.create(BackendService.class);
                            BackendSpotifyAuth spotifyAuth = new BackendSpotifyAuth(spotifyUser.id + "_spotify", spotifyAuthToken);
                            Call<Token> call2 = backendService.authSpotify(spotifyAuth);
                            call2.enqueue(new Callback<Token>() {
                                @Override
                                public void onResponse(Call<Token> call, Response<Token> response) {
                                    dependencies.getAuth().signInWithCustomToken(response.body().getToken())
                                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                @Override
                                                public void onSuccess(AuthResult authResult) {
                                                    authResulter = authResult;
                                                    checkUser();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            echelonErrorSnackie.show();
                                            Log.d(TAG, e.toString() + "   " + e.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<Token> call, Throwable t) {
                                    echelonErrorSnackie.show();
                                    Log.d(TAG, t.toString() + "   " + t.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<UserPrivate> call, Throwable t) {
                            echelonErrorSnackie.show();
                            Log.d(TAG, t.toString() + "   " + t.getMessage());
                        }
                    });
                    break;
                case ERROR:
                    Log.e("SpotifyAuth", authResponse.getError());
                    spotifyErrorSnackie.show();
                    break;
                default:
                    // Handle other cases
                    spotifyErrorSnackie.show();
                    Log.e("SpotifyAuth", authResponse.getError());
            }
        }
    }

    private void checkUser() {
        String fUid = authResulter.getUser().getUid();
        dependencies.getPreferences().edit().putString(PreferenceNames.PREF_FIREBASE_UID, fUid).putString(PreferenceNames.PREF_USER_AUTH_TYPE, "spotify").commit();
        DatabaseReference userReference = dependencies.getUserReference(fUid);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GetFirebaseSpotifyToken", "DATA CHANGED");
                String fUid = authResulter.getUser().getUid();
                SharedPreferences pref = getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
                DatabaseReference participant = dependencies.getParticipantReference(fUid);
                if (dataSnapshot.getValue() == null) {
                    createUser();
                } else {
                    SharedPreferences.Editor prefEdit = pref.edit();
                    if (dataSnapshot.hasChild("display_name")) {
                        prefEdit.putString(PreferenceNames.PREF_USER_DISPLAY_NAME, (String) dataSnapshot.child("display_name").getValue());
                    }
                    participant.child("ext_url").setValue(pref.getString(PreferenceNames.PREF_SPOTIFY_EXT_URL, null));
                    prefEdit.putString(PreferenceNames.PREF_USER_EXT_URL, pref.getString(PreferenceNames.PREF_SPOTIFY_EXT_URL, null));

                    participant.child("image_url").setValue(pref.getString(PreferenceNames.PREF_SPOTIFY_IMAGE_URL, null));
                    prefEdit.putString(PreferenceNames.PREF_USER_IMAGE_URL, pref.getString(PreferenceNames.PREF_SPOTIFY_IMAGE_URL, null));

                    prefEdit.apply();
                }
                participant.child("online").onDisconnect().setValue(false);
                participant.child("online").setValue(true);

                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString() + "   " + databaseError.getMessage());
                echelonErrorSnackie.show();
            }
        });
    }

    private void createUser() {
        Log.d("GetFirebaseSpotifyToken", "New User, creating in DB");
        SharedPreferences pref = dependencies.getPreferences();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", pref.getString(PreferenceNames.PREF_SPOTIFY_EMAIL, null));
        userInfo.put("product", pref.getString(PreferenceNames.PREF_SPOTIFY_PRODUCT, null));
        userInfo.put("type", pref.getString(PreferenceNames.PREF_SPOTIFY_TYPE, null));

        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("country", pref.getString(PreferenceNames.PREF_SPOTIFY_COUNTRY, null));
        participantInfo.put("display_name", pref.getString(PreferenceNames.PREF_SPOTIFY_DISPLAY_NAME, null));
        participantInfo.put("id", pref.getString(PreferenceNames.PREF_SPOTIFY_UID, null));
        participantInfo.put("ext_url", pref.getString(PreferenceNames.PREF_SPOTIFY_EXT_URL, null));
        participantInfo.put("uri", pref.getString(PreferenceNames.PREF_SPOTIFY_URI, null));
        participantInfo.put("image_url", pref.getString(PreferenceNames.PREF_SPOTIFY_IMAGE_URL, null));

        String fUid = authResulter.getUser().getUid();
        dependencies.getUserReference(fUid).setValue(userInfo);
        dependencies.getParticipantReference(fUid).setValue(participantInfo);
    }

    @Override
    public void onLoggedIn() {
        dependencies.getPreferences().edit().putBoolean(PreferenceNames.PREF_SPOTIFY_AUTHENTICATED, true).apply();
        Log.d("Authentication", "Logged In to Spotify");
    }

    @Override
    public void onLoggedOut() {
        dependencies.getPreferences().edit().putBoolean(PreferenceNames.PREF_SPOTIFY_AUTHENTICATED, false).apply();
        Log.d("Authentication", "Logged Out of Spotify");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }
}
