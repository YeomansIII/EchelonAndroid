package io.yeomans.echelon.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;

public class EchelonUtils {
  public static void CopyStream(InputStream is, OutputStream os) {
    final int buffer_size = 1024;
    try {
      byte[] bytes = new byte[buffer_size];
      for (; ; ) {
        int count = is.read(bytes, 0, buffer_size);
        if (count == -1)
          break;
        os.write(bytes, 0, count);
      }
    } catch (Exception ex) {
    }
  }

  public static boolean isServiceRunning(Activity activity, Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  public static String getGroup() {
    return Dependencies.INSTANCE.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, null);
  }
}
