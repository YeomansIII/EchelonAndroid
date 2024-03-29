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
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.PreferenceNames;

/**
 * Created by jason on 10/5/15.
 */
public class CurrentGroupDialogFragment extends DialogFragment {
    SharedPreferences mainPref, groupPref;
    MainActivity mainActivity;
    Dependencies dependencies;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dependencies = Dependencies.INSTANCE;
        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
        groupPref = mainActivity.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Active Group");
        builder.setMessage("You are currently part of a group, what would you like to do?")
                .setPositiveButton("Go To Group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                        Fragment fragment = new GroupFragment();
                        Bundle bundle = new Bundle();
                        bundle.putStringArray("extra_stuff", new String[]{"" + false, "" + false});
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, fragment, "GROUP_FRAG")
                                .commit();
                    }
                })
                .setNegativeButton("Leave Group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String fUid = mainPref.getString(PreferenceNames.PREF_FIREBASE_UID, null);
                        String groupName = groupPref.getString(PreferenceNames.PREF_GROUP_NAME, null);
                        if (fUid != null && groupName != null) {
                            dependencies.getCurrentUserReference().child("/cur_group").removeValue();
                            dependencies.getCurrentGroupReference().child("participants/" + fUid).removeValue();
                            groupPref.edit().clear().apply();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}