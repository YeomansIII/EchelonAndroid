package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;

import org.json.JSONException;
import org.json.JSONObject;

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
    private TextInputLayout createGroupPasswordEditWrapper;
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
            TextInputLayout groupName = (TextInputLayout) view.findViewById(R.id.createGroupNameEditWrapper);
            TextInputLayout groupPass = (TextInputLayout) view.findViewById(R.id.createGroupPasswordEditWrapper);
            String name = groupName.getEditText().getText().toString();
            Firebase refQueueGroups = mainActivity.myFirebaseRef.child("queuegroups");
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("created", ServerValue.TIMESTAMP);
            map.put("leader", mainPref.getString(MainActivity.PREF_FIREBASE_UID, null));
            map.put("privacy", selectedPrivacy);
            if (selectedPrivacy.equals("password")) {
                map.put("password", groupPass.getEditText().getText().toString());
            }
            refQueueGroups.child(name).setValue(map);

            String fUid = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);

            SharedPreferences groupSettings = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
            groupSettings.edit().putString(MainActivity.PREF_GROUP_NAME, name).putString(MainActivity.PREF_GROUP_LEADER_UID, fUid).apply();

            Fragment fragment = new GroupFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, "GROUP_FRAG")
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
    }
}