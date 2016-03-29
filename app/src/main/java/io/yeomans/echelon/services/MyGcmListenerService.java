package io.yeomans.echelon.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by jason on 7/8/15.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String action = data.getString("action");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Action: " + action);

        Intent intent = new Intent("gcm_intent");

        //put whatever data you want to send, if any
        intent.putExtra("action", action);

        //send broadcast
        getApplicationContext().sendBroadcast(intent);


        //BackendRequest be = new BackendRequest("GET", getApplicationContext());


        //sendNotification(message);
    }
}
