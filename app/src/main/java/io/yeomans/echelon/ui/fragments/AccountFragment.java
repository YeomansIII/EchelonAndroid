package io.yeomans.echelon.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError ;
import com.google.firebase.database.ValueEventListener;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 7/1/15.
 */
public class AccountFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    private SharedPreferences mainPref, groupPref;
    //private RadioButton rbPublic, rbPassword, rbFriends, rbInvite;
    private TextInputLayout accountDisplayNameEditWrapper;
    //String selectedPrivacy;
    private DatabaseReference thisUserRef, thisParticipantRef;
    private String uid;
    private String accountType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        groupPref = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        uid = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);
        accountType = mainPref.getString(MainActivity.PREF_USER_AUTH_TYPE, "none");
        thisUserRef = mainActivity.myFirebaseRef.child("users/" + uid);
        thisParticipantRef = mainActivity.myFirebaseRef.child("participants/" + uid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.account_fragment,
                container, false);
        this.view = view1;
        //view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);

        mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        mainActivity.getSupportActionBar().setTitle("Account");
        if (uid != null) {
            if (accountType.equals("anonymous")) {
                view.findViewById(R.id.accountEmailStaticText).setVisibility(View.GONE);
                view.findViewById(R.id.accountEmailText).setVisibility(View.GONE);
            }

            accountDisplayNameEditWrapper = (TextInputLayout) view.findViewById(R.id.accountDisplayNameEditWrapper);
            view.findViewById(R.id.accountLogoutButton).setOnClickListener(this);

            thisUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        ((TextView) view.findViewById(R.id.accountIdText)).setText(dataSnapshot.getKey());
                        if (dataSnapshot.hasChild("email")) {
                            ((TextView) view.findViewById(R.id.accountEmailText)).setText((String) dataSnapshot.child("email").getValue());
                        }
                    } else {
                        ((TextView) view.findViewById(R.id.accountIdText)).setText(mainPref.getString(MainActivity.PREF_FIREBASE_UID, "error"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError  firebaseError) {
                    Toast.makeText(mainActivity.getApplicationContext(), "Error getting data", Toast.LENGTH_SHORT).show();
                }
            });
            thisParticipantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        if (dataSnapshot.hasChild("display_name")) {
                            accountDisplayNameEditWrapper.getEditText().setText((String) dataSnapshot.child("display_name").getValue());
                        }
                        accountDisplayNameEditWrapper.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                thisUserRef.child("display_name").setValue(s.toString());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError  firebaseError) {
                    Toast.makeText(mainActivity.getApplicationContext(), "Error getting data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(mainActivity.getApplicationContext(), "Error, no user identified", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.accountLogoutButton)) {
            mainActivity.logout();
        }
    }
}