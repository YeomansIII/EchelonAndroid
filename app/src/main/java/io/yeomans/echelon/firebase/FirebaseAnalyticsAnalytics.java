package io.yeomans.echelon.firebase;

/**
 * Created by jason on 6/25/16.
 */
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import static com.google.firebase.analytics.FirebaseAnalytics.Event;
import static com.google.firebase.analytics.FirebaseAnalytics.Param;

public class FirebaseAnalyticsAnalytics {

    private static final String PARAM_CHANNEL_NAME = "channel_name";

    private final FirebaseAnalytics firebaseAnalytics;

    public FirebaseAnalyticsAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public void trackSignInStarted(String method) {
        Bundle bundle = new Bundle();
        bundle.putString(Param.SIGN_UP_METHOD, method);
        firebaseAnalytics.logEvent(Event.SIGN_UP, bundle);
    }

}