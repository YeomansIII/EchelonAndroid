package io.yeomans.echelon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import io.github.kaaes.spotify.webapi.core.models.FeaturedPlaylists;
import io.github.kaaes.spotify.webapi.core.models.Playlist;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.fragments.GroupFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

  static public void setDefaultPlaylist(String userId, String playlistId) {
    DatabaseReference ref = Dependencies.INSTANCE.getCurrentGroupReference();
    Call<Playlist> call = Dependencies.INSTANCE.getSpotify().getPlaylist(userId, playlistId);
    call.enqueue(new Callback<Playlist>() {
      @Override
      public void onResponse(Call<Playlist> call, Response<Playlist> response) {
        Log.i("Playlists", "Get playlist results");
        Playlist playlist = response.body();
      }

      @Override
      public void onFailure(Call<Playlist> call, Throwable t) {
        Log.wtf("WhatList", t.toString() + "   " + t.getMessage());
      }
    });
  }

  static public void joinGroup(final String groupName, final AppCompatActivity activity) {
    Dependencies dependencies = Dependencies.INSTANCE;
    SharedPreferences mainPref = dependencies.getPreferences();
    SharedPreferences groupPref = dependencies.getGroupPreferences();
    String fUid2 = mainPref.getString(PreferenceNames.PREF_FIREBASE_UID, null);
    Dependencies.INSTANCE.getDatabase().getReference("users/" + fUid2 + "/cur_group").setValue(groupName);
    DatabaseReference ref = dependencies.getDatabase().getReference("queuegroups/" + groupName);
    ref.child("participants/" + fUid2).setValue(true);
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String leaderUid = (String) dataSnapshot.child("leader").getValue();
        Dependencies.INSTANCE.getGroupPreferences().edit().putString(PreferenceNames.PREF_GROUP_NAME, dataSnapshot.getKey()).putString(PreferenceNames.PREF_GROUP_LEADER_UID, leaderUid).apply();
        String fUid3 = Dependencies.INSTANCE.getPreferences().getString(PreferenceNames.PREF_FIREBASE_UID, null);
        Fragment fragment = new GroupFragment();
        Bundle bundle = new Bundle();
        if (leaderUid.equals(fUid3)) {
          bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
        } else {
          bundle.putStringArray("extra_stuff", new String[]{"" + false, "" + false});
        }
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
          .replace(R.id.container, fragment, "GROUP_FRAG")
          .commit();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
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
