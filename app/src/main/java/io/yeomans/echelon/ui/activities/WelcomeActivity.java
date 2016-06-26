package io.yeomans.echelon.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.BackendSpotifyAuth;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 6/25/16.
 */
public class WelcomeActivity extends AppCompatActivity implements ConnectionStateCallback {

    public static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    public static final String REDIRECT_URI = "echelonapp://callback";
    public static final int REQUEST_CODE = 9001;

    public AuthenticationResponse authResponse;
    String spotifyAuthToken;
    Dependencies dependencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_fragment);
        ButterKnife.bind(this);
        dependencies = Dependencies.INSTANCE;
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
                    BackendSpotifyAuth spotifyAuth = new BackendSpotifyAuth(, );
                    break;
                case ERROR:
                    Log.e("SpotifyAuth", authResponse.getError());
                    break;
                default:
                    // Handle other cases
                    Log.e("SpotifyAuth", authResponse.getError());
            }
        }
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
