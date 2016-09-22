package io.yeomans.echelon.util;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by jason on 9/15/15.
 */
public class FirebaseCommon {
  public static final String TAG = FirebaseCommon.class.getSimpleName();

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

  static public void setDisplayName(final String displayName) {
    DatabaseReference dnRef = Dependencies.INSTANCE.getDatabase().getReference("display_names/" + displayName);
    dnRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        boolean codeFound = false;
        int numberToFriendCode = 0000,
          lastFriendCode = 0000;
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          int thisFriendCode = Integer.parseInt(child.getKey());
          if (thisFriendCode - lastFriendCode > 1) {
            numberToFriendCode = lastFriendCode + 1;
            codeFound = true;
          } else {
            lastFriendCode = thisFriendCode;
          }
        }
        if (!codeFound) {
          numberToFriendCode = lastFriendCode + 1;
        }
        final String finalFriendCode = "" + numberToFriendCode;
        Dependencies.INSTANCE.getCurrentParticipantReference().addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            String oldDisplayName = null, oldFriendCode = null;
            if (dataSnapshot.hasChild("display_name") && dataSnapshot.hasChild("friend_code")) {
              oldDisplayName = (String) dataSnapshot.child("display_name").getValue();
              oldFriendCode = (String) dataSnapshot.child("friend_code").getValue();
            }
            final String finaloldDisplayName = oldDisplayName, finalOldFriendCode = oldFriendCode;
            FirebaseUser curUser = Dependencies.INSTANCE.getAuth().getCurrentUser();
            final String paddedFriendCode = "0000".substring(finalFriendCode.length()) + finalFriendCode;
            Dependencies.INSTANCE.getDatabase().getReference("display_names/" + displayName + "/" + paddedFriendCode).setValue(curUser.getUid(), new DatabaseReference.CompletionListener() {
              @Override
              public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                  if (finaloldDisplayName != null && finalOldFriendCode != null) {
                    Dependencies.INSTANCE.getDatabase().getReference("display_names/" + finaloldDisplayName + "/" + finalOldFriendCode).removeValue();
                  }
                  Dependencies.INSTANCE.getCurrentParticipantReference().child("friend_code").setValue(paddedFriendCode);
                  Dependencies.INSTANCE.getCurrentParticipantReference().child("display_name").setValue(displayName);
                  Dependencies.INSTANCE.getPreferences().edit()
                    .putString(PreferenceNames.PREF_USER_DISPLAY_NAME, displayName)
                    .putString(PreferenceNames.PREF_USER_FRIEND_CODE, paddedFriendCode)
                    .apply();
                }
              }
            });
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Error accessing Firebase: " + databaseError.getMessage());
          }
        });
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Error accessing Firebase: " + databaseError.getMessage());
      }
    });
    ////////////////////////// Working on transaction
//    dnRef.runTransaction(new Transaction.Handler() {
//      @Override
//      public Transaction.Result doTransaction(MutableData mutableData) {
//        boolean codeFound = false;
//        int numberToFriendCode = 0000,
//          lastFriendCode = 0000;
//        for (MutableData child : mutableData.getChildren()) {
//          int thisFriendCode = Integer.parseInt(child.getKey());
//          if (thisFriendCode - lastFriendCode > 1) {
//            numberToFriendCode = lastFriendCode + 1;
//            codeFound = true;
//          } else {
//            lastFriendCode = thisFriendCode;
//          }
//        }
//        if (!codeFound) {
//          numberToFriendCode = lastFriendCode + 1;
//        }
//        String finalFriendCode = "" + numberToFriendCode;
//        FirebaseUser curUser = Dependencies.INSTANCE.getAuth().getCurrentUser();
//        String paddedFriendCode = "0000".substring(finalFriendCode.length()) + finalFriendCode;
//        mutableData.child(paddedFriendCode).setValue(curUser.getUid());
//        return Transaction.success(mutableData);
//      }
//
//      @Override
//      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshotTrans) {
//        Log.i(TAG, "Display Name Transaction Complete");
//        String friendCode = null;
//        final String displayName = dataSnapshotTrans.getKey();
//        for (DataSnapshot child : dataSnapshotTrans.getChildren()) {
//          if (child.getValue() == Dependencies.INSTANCE.getAuth().getCurrentUser().getUid()) {
//            friendCode = child.getKey();
//            break;
//          }
//        }
//        if (friendCode != null) {
//          final String finalFriendCode = friendCode;
//          Log.i(TAG, "Friendcode found: " + finalFriendCode);
//          Dependencies.INSTANCE.getCurrentParticipantReference().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//              Log.d(TAG, "Has display_name and friend_code: " + (dataSnapshot.hasChild("display_name") && dataSnapshot.hasChild("friend_code")));
//              if (dataSnapshot.hasChild("display_name") && dataSnapshot.hasChild("friend_code")) {
//                String oldDisplayName = (String) dataSnapshot.child("display_name").getValue(),
//                  oldFriendCode = (String) dataSnapshot.child("friend_code").getValue();
//                Log.d(TAG, "Remove old display_name ref: " + "display_names/" + oldDisplayName + "/" + oldFriendCode);
//                Dependencies.INSTANCE.getDatabase().getReference("display_names/" + oldDisplayName + "/" + oldFriendCode).removeValue();
//              }
//              Dependencies.INSTANCE.getCurrentParticipantReference().child("friend_code").setValue(finalFriendCode);
//              Dependencies.INSTANCE.getCurrentParticipantReference().child("display_name").setValue(displayName);
//              Log.i(TAG, "Done with display_name change");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//              Log.e(TAG, "Error accessing Firebase: " + databaseError.getMessage());
//            }
//          });
//        }
//      }
//    });
  }
}
