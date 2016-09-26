package io.yeomans.echelon.ui.fragments;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.FirebaseCommon;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 7/1/15.
 */
public class JoinGroupFragment extends Fragment implements View.OnClickListener {

  private View view;
  private MainActivity mainActivity;
  private SharedPreferences mainPref, groupPref;
  private Dependencies dependencies;
  //private RadioButton rbPublic, rbPassword, rbFriends, rbInvite;
  private TextInputLayout joinGroupNameEditWrapper, joinGroupPasswordEditWrapper;
  //String selectedPrivacy;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    dependencies = Dependencies.INSTANCE;

    mainActivity = (MainActivity) getActivity();
    mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
    groupPref = mainActivity.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.join_group_fragment,
      container, false);
    //view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);

    mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT);
    mainActivity.getSupportActionBar().setTitle("");

    joinGroupNameEditWrapper = (TextInputLayout) view.findViewById(R.id.joinGroupNameEditWrapper);
    joinGroupPasswordEditWrapper = (TextInputLayout) view.findViewById(R.id.joinGroupPasswordEditWrapper);

    view.findViewById(R.id.joinGroupCreateButton).setOnClickListener(this);

    this.view = view;
    return view;
  }

  @Override
  public void onClick(View v) {
    if (v == view.findViewById(R.id.joinGroupCreateButton)) {
      String name = joinGroupNameEditWrapper.getEditText().getText().toString();
      DatabaseReference refQueueGroups = dependencies.getDatabase().getReference("queuegroups/" + name + "/name");
      refQueueGroups.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() == null) {
            joinGroupNameEditWrapper.setErrorEnabled(true);
            joinGroupNameEditWrapper.setError("That group does not exist");
          } else {
            String name2 = joinGroupNameEditWrapper.getEditText().getText().toString();
            FirebaseCommon.joinGroup(name2, mainActivity);
            View view = mainActivity.getCurrentFocus();
            if (view != null) {
              InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
