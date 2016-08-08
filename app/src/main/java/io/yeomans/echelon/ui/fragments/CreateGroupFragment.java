package io.yeomans.echelon.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 7/1/15.
 */
public class CreateGroupFragment extends Fragment implements View.OnClickListener {

  private View view;
  private MainActivity mainActivity;
  private SharedPreferences mainPref;
  private RadioButton rbPublic, rbPassword, rbFriends, rbInvite;
  private TextInputLayout createGroupNameEditWrapper, createGroupPasswordEditWrapper;
  String selectedPrivacy;
  Dependencies dependencies;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dependencies = Dependencies.INSTANCE;
    mainActivity = (MainActivity) getActivity();
    mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.create_group_fragment,
      container, false);
    //view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);

    mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT);
    mainActivity.getSupportActionBar().setTitle("");

    rbPublic = (RadioButton) view.findViewById(R.id.createGroupPrivacyPublicRadio);
    rbPassword = (RadioButton) view.findViewById(R.id.createGroupPrivacyPasswordRadio);
    rbFriends = (RadioButton) view.findViewById(R.id.createGroupPrivacyFriendsRadio);
    rbInvite = (RadioButton) view.findViewById(R.id.createGroupPrivacyInviteRadio);

    createGroupNameEditWrapper = (TextInputLayout) view.findViewById(R.id.createGroupNameEditWrapper);
    createGroupPasswordEditWrapper = (TextInputLayout) view.findViewById(R.id.createGroupPasswordEditWrapper);

    rbPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          rbFriends.setChecked(false);
          rbPassword.setChecked(false);
          rbInvite.setChecked(false);
          selectedPrivacy = "public";
        }
      }
    });

    rbPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          rbPublic.setChecked(false);
          rbFriends.setChecked(false);
          rbInvite.setChecked(false);
          createGroupPasswordEditWrapper.setVisibility(View.VISIBLE);
          selectedPrivacy = "password";
        } else {
          createGroupPasswordEditWrapper.setVisibility(View.GONE);
        }
      }
    });

    rbFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          rbPublic.setChecked(false);
          rbPassword.setChecked(false);
          rbInvite.setChecked(false);
          selectedPrivacy = "friends";
        }
      }
    });

    rbInvite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          rbPublic.setChecked(false);
          rbPassword.setChecked(false);
          rbFriends.setChecked(false);
          selectedPrivacy = "invite";
        }
      }
    });

    view.findViewById(R.id.createGroupCreateButton).setOnClickListener(this);

    this.view = view;
    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
  }

  @Override
  public void onClick(View v) {
    if (v == view.findViewById(R.id.createGroupCreateButton)) {
      final String name = createGroupNameEditWrapper.getEditText().getText().toString();
      DatabaseReference refQueueGroups = dependencies.getDatabase().getReference("queuegroups/" + name + "/name");
      refQueueGroups.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() != null) {
            createGroupNameEditWrapper.setErrorEnabled(true);
            createGroupNameEditWrapper.setError("That group name is already taken, try another one");
          } else {
            FirebaseUser fUser = dependencies.getAuth().getCurrentUser();
            if (fUser != null) {
              dependencies.getCurrentUserReference().child("cur_group").setValue(name);
              DatabaseReference ref = dependencies.getDatabase().getReference("queuegroups");
              Map<String, Object> map = new HashMap<>();
              map.put("name", name);
              map.put("created", ServerValue.TIMESTAMP);
              map.put("leader", fUser.getUid());
              // map.put("privacy", selectedPrivacy);
//                        if (selectedPrivacy.equals("password")) {
//                            map.put("password", createGroupPasswordEditWrapper.getEditText().getText().toString());
//                        }
              ref.child(name).setValue(map);
              ref.child(name + "/participants/" + fUser.getUid()).setValue(true);

              SharedPreferences groupSettings = mainActivity.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);
              groupSettings.edit().putString(PreferenceNames.PREF_GROUP_NAME, name).putString(PreferenceNames.PREF_GROUP_LEADER_UID, fUser.getUid()).apply();
              View view = mainActivity.getCurrentFocus();
              if (view != null) {
                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
                  Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
              }
              Fragment fragment = new GroupFragment();
              Bundle bundle = new Bundle();
              bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
              fragment.setArguments(bundle);
              FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
              fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                .replace(R.id.container, fragment, "GROUP_FRAG")
                .commit();
            }
          }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {

        }
      });
    }
  }
}
