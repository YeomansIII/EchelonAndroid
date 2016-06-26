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
public class CurrentGroupLeaderDialogFragment extends DialogFragment {
    Dependencies dependencies;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dependencies = Dependencies.INSTANCE;
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Active Group");
        builder.setMessage("You are currently the leader of a group, what would you like to do?")
                .setPositiveButton("Go To Group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
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
                        String fUid = dependencies.getPreferences().getString(PreferenceNames.PREF_FIREBASE_UID, null);
                        String groupName = dependencies.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, null);
                        if (fUid != null && groupName != null) {
                            dependencies.getDatabase().getReference("users/" + fUid + "/cur_group").removeValue();
                            dependencies.getDatabase().getReference("queuegroups/" + groupName).removeValue();
                            dependencies.getGroupPreferences().edit().clear().apply();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}