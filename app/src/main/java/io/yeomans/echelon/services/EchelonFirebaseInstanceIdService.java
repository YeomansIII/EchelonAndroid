package io.yeomans.echelon.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 5/20/16.
 */
public class EchelonFirebaseInstanceIdService extends FirebaseInstanceIdService {
  private static final String TAG = "FCM";

  @Override
  public void onTokenRefresh() {
    // Get updated InstanceID token.
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    Log.d(TAG, "Refreshed token: " + refreshedToken);

    // TODO: Implement this method to send any registration to your app's servers.
    sendRegistrationToServer(refreshedToken);
  }

  private void sendRegistrationToServer(String token) {
    SharedPreferences devicePref = Dependencies.INSTANCE.getDevicePreferences();
    devicePref.edit().putString(PreferenceNames.PREF_MESSAGING_ID, token);
    String deviceUuid = devicePref.getString(PreferenceNames.PREF_DEVICE_UUID, null);
    if (deviceUuid != null) {
      Dependencies.INSTANCE.getCurrentUserReference().child("devices/" + deviceUuid + "/messagingId").setValue(token);
    }
  }
}
