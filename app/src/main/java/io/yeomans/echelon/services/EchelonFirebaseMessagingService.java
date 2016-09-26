package io.yeomans.echelon.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 7/8/15.
 */
public class EchelonFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = "FCM";

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // ...

    // TODO(developer): Handle FCM messages here.
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    Log.d(TAG, "From: " + remoteMessage.getFrom());

    // Check if message contains a data payload.
    if (remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
    }

    // Check if message contains a notification payload.
    if (remoteMessage.getNotification() != null) {
      Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.
    sendNotification(remoteMessage);
  }

  private void sendNotification(RemoteMessage remoteMessage) {
    Map<String, String> data = remoteMessage.getData();
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("join_group", "true");
    intent.putExtra("group_name", data.get("group_name"));
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
      PendingIntent.FLAG_ONE_SHOT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Echelon Group Invite")
      .setContentText(data.get("inviter_display_name") + " has invited you to join the group " + data.get("groupName") + " on Echelon.")
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent);

    NotificationManager notificationManager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
  }
}
