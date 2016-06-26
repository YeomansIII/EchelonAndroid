package io.yeomans.echelon.util;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {

    static public void rankSong(String key, final int upDown) {
        DatabaseReference ref = Dependencies.INSTANCE.getDatabase().getReference("/queuegroups/"
                + Dependencies.INSTANCE.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, null) +
                "/tracks/" + key);

        String uid = Dependencies.INSTANCE.getAuth().getCurrentUser().getUid();

        if (upDown > 0) {
            ref.child("votedUp").child(uid).setValue(true);
        } else if (upDown < 0) {
            ref.child("votedDown").child(uid).setValue(true);
        } else {
            ref.child("votedUp").child(uid).removeValue();
            ref.child("votedDown").child(uid).removeValue();
        }
    }
}
