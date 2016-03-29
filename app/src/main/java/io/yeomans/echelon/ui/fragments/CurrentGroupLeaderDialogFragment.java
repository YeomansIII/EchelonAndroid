package io.yeomans.echelon.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 10/5/15.
 */
public class CurrentGroupLeaderDialogFragment extends DialogFragment {
    SharedPreferences mainPref, groupPref;
    MainActivity mainActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
        groupPref = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Active Group");
        builder.setMessage("You are currently the leader of a group, what would you like to do?")
                .setPositiveButton("Go To Group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                        Fragment fragment = new GroupFragment();
                        Bundle bundle = new Bundle();
                        bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, fragment, "GROUP_FRAG")
                                .commit();
                    }
                })
                .setNegativeButton("Destroy Group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String fUid = mainPref.getString(MainActivity.PREF_FIREBASE_UID, null);
                        String groupName = groupPref.getString(MainActivity.PREF_GROUP_NAME, null);
                        if (fUid != null && groupName != null) {
                            mainActivity.myFirebaseRef.child("users/" + fUid + "/cur_group").removeValue();
                            mainActivity.myFirebaseRef.child("queuegroups/" + groupName).removeValue();
                            groupPref.edit().clear().apply();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}