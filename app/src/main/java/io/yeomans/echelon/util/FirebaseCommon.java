package io.yeomans.echelon.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {

  static public void rankSong(String key, int upDown) {
    DatabaseReference ref = Dependencies.INSTANCE.getCurrentGroupReference().child("/tracks/" + key);

    String uid = Dependencies.INSTANCE.getAuth().getCurrentUser().getUid();

    if (upDown > 0) {
      ref.child("votedDown").child(uid).removeValue();
      ref.child("votedUp").child(uid).setValue(true);
    } else if (upDown < 0) {
      ref.child("votedUp").child(uid).removeValue();
      ref.child("votedDown").child(uid).setValue(true);
    } else {
      ref.child("votedUp").child(uid).removeValue();
      ref.child("votedDown").child(uid).removeValue();
    }
  }

  static public void setDisplayName(String displayName) {
    DatabaseReference dnRef = Dependencies.INSTANCE.getDatabase().getReference("display_names/" + displayName);
    dnRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        int numberToFriendCode = 0000;
        for(DataSnapshot child:dataSnapshot.getChildren()) {
          child.getKey();
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }
}
