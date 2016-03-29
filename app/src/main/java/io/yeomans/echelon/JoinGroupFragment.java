package io.yeomans.echelon;

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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 7/1/15.
 */
public class JoinGroupFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    private SharedPreferences mainPref, groupPref;
    //private RadioButton rbPublic, rbPassword, rbFriends, rbInvite;
    private TextInputLayout joinGroupNameEditWrapper, joinGroupPasswordEditWrapper;
    //String selectedPrivacy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        groupPref = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.joinGroupCreateButton)) {
            String name = joinGroupNameEditWrapper.getEditText().getText().toString();
            Firebase refQueueGroups = mainActivity.myFirebaseRef.child("queuegroups/" + name);
            refQueueGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        joinGroupNameEditWrapper.setErrorEnabled(true);
                        joinGroupNameEditWrapper.setError("That group does not exist");
                    } else {
                        String name2 = joinGroupNameEditWrapper.getEditText().getText().toString();
                        String fUid2 = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);
                        String leaderUid = (String) dataSnapshot.child("leader").getValue();
                        mainActivity.myFirebaseRef.child("users/" + fUid2 + "/cur_group").setValue(name2);
                        Firebase ref = mainActivity.myFirebaseRef.child("queuegroups/" + name2);
                        ref.child("participants/" + fUid2).setValue(true);
                        groupPref.edit().putString(MainActivity.PREF_GROUP_NAME, name2).putString(MainActivity.PREF_GROUP_LEADER_UID, leaderUid).apply();

                        View view = mainActivity.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        Fragment fragment = new GroupFragment();
                        Bundle bundle = new Bundle();
                        if (leaderUid.equals(fUid2)) {
                            bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
                        } else {
                            bundle.putStringArray("extra_stuff", new String[]{"" + false, "" + false});
                        }
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, fragment, "GROUP_FRAG")
                                .commit();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
}