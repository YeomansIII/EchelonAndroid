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
public class CreateGroupFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    private SharedPreferences mainPref;
    private RadioButton rbPublic, rbPassword, rbFriends, rbInvite;
    private TextInputLayout createGroupNameEditWrapper, createGroupPasswordEditWrapper;
    String selectedPrivacy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
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
            String name = createGroupNameEditWrapper.getEditText().getText().toString();
            String fUid = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);
            Firebase refQueueGroups = mainActivity.myFirebaseRef.child("queuegroups/" + name);
            refQueueGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        createGroupNameEditWrapper.setErrorEnabled(true);
                        createGroupNameEditWrapper.setError("That group name is already taken, try another one");
                    } else {
                        String name2 = createGroupNameEditWrapper.getEditText().getText().toString();
                        String fUid2 = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);
                        Firebase ref = mainActivity.myFirebaseRef.child("queuegroups");
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name2);
                        map.put("created", ServerValue.TIMESTAMP);
                        map.put("leader", fUid2);
                        map.put("privacy", selectedPrivacy);
//                        if (selectedPrivacy.equals("password")) {
//                            map.put("password", createGroupPasswordEditWrapper.getEditText().getText().toString());
//                        }
                        ref.child(name2).setValue(map);
                        ref.child(name2).child("participants").child(fUid2).setValue(true);

                        SharedPreferences groupSettings = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
                        groupSettings.edit().putString(MainActivity.PREF_GROUP_NAME, name2).putString(MainActivity.PREF_GROUP_LEADER_UID, fUid2).apply();
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

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
}