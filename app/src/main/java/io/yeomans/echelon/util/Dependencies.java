package io.yeomans.echelon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import io.github.kaaes.spotify.webapi.retrofit.v2.Spotify;
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyService;
import io.yeomans.echelon.firebase.FirebaseAnalyticsAnalytics;
import io.yeomans.echelon.firebase.FirebaseConfig;
import io.yeomans.echelon.firebase.FirebaseErrorLogger;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 6/25/16.
 */
public enum Dependencies {
    INSTANCE;

    private FirebaseAnalyticsAnalytics analytics;
    private FirebaseErrorLogger errorLogger;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseApp firebaseApp;
    private SpotifyService spotify;
    private SharedPreferences preferences, groupPreferences;

    private FirebaseConfig config;

    public void init(Context context) {
        if (needsInitialisation()) {
            Context appContext = context.getApplicationContext();
            firebaseApp = FirebaseApp.initializeApp(appContext, FirebaseOptions.fromResource(appContext), "Echelon");
            auth = FirebaseAuth.getInstance(firebaseApp);
            database = FirebaseDatabase.getInstance(firebaseApp);
            database.setPersistenceEnabled(true);

            analytics = new FirebaseAnalyticsAnalytics(FirebaseAnalytics.getInstance(appContext));
            errorLogger = new FirebaseErrorLogger();
            config = FirebaseConfig.newInstance().init(errorLogger);
            preferences = context.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
            groupPreferences = context.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);

            String spotifyAuthToken = preferences.getString(PreferenceNames.PREF_SPOTIFY_AUTH_TOKEN, null);
            if (spotifyAuthToken != null) {
                spotify = Spotify.createAuthenticatedService(spotifyAuthToken);
            } else {
                spotify = Spotify.createNotAuthenticatedService();
            }
        }
    }

    private boolean needsInitialisation() {
        return firebaseApp == null || auth == null || database == null || analytics == null || errorLogger == null || config == null;
    }

    public FirebaseAnalyticsAnalytics getAnalytics() {
        return analytics;
    }

    public FirebaseErrorLogger getErrorLogger() {
        return errorLogger;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseApp getFirebaseApp() {
        return firebaseApp;
    }

    public FirebaseConfig getConfig() {
        return config;
    }

    public SpotifyService getSpotify() {
        return spotify;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public SharedPreferences getGroupPreferences() {
        return groupPreferences;
    }
}