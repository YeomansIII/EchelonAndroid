package io.yeomans.groupqueue;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jason on 7/1/15.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {


    private Activity parentActivity;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment,
                container, false);
        view.findViewById(R.id.createGroupButton).setOnClickListener(this);
        view.findViewById(R.id.joinGroupButton).setOnClickListener(this);
        view.findViewById(R.id.joinGroupIdButton).setOnClickListener(this);
        view.findViewById(R.id.logoutButton).setOnClickListener(this);
        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.createGroupButton)) {
            boolean leader = true;
            Log.d("Button", "Create Button");
            Intent groupIntent = new Intent(parentActivity, GroupActivity.class);
            Log.wtf("PuttingIntExtra", "" + leader);
            groupIntent.putExtra("extra_stuff", new String[]{"" + leader, "" + leader});
            startActivity(groupIntent);
        } else if (v == view.findViewById(R.id.joinGroupButton)) {
            Log.d("Button", "Join Group Button");
            parentActivity.findViewById(R.id.homeButtonsLayout).setVisibility(View.GONE);
            parentActivity.findViewById(R.id.joinGroupIdLayout).setVisibility(View.VISIBLE);
        } else if (v == view.findViewById(R.id.logoutButton)) {
            Log.d("Button", "Logout Button");
            SharedPreferences pref = parentActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
            pref.edit().remove("token").commit();
            Fragment fragment = new LoginFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (v == view.findViewById(R.id.joinGroupIdButton)) {
            Log.d("Button", "Join Button");
            JSONObject json = new JSONObject();
            String usernameJoin = ((TextView)parentActivity.findViewById(R.id.joinGroupIdEdit)).getText().toString();
            try {
                json.put("username_join",usernameJoin);
                BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/join-group/",json.toString(),parentActivity);
                BackendRequest.activateJoinGroup(be);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}