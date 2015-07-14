package io.yeomans.groupqueue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jason on 7/1/15.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity)getActivity();
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
        view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);

        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.createGroupButton)) {
            boolean leader = true;
            Log.d("Button", "Create Button");
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
            if (groupFragment == null || groupFragment.isDestroyed) {
                BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/activate-my-group/", (MainActivity) getActivity());
                BackendRequest.activateJoinGroup(be);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in a group", Toast.LENGTH_SHORT).show();
            }
        } else if (v == view.findViewById(R.id.joinGroupButton)) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
            if (groupFragment == null) {
                Log.d("Button", "Join Group Button");
                getActivity().findViewById(R.id.homeButtonsLayout).setVisibility(View.GONE);
                getActivity().findViewById(R.id.joinGroupIdLayout).setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in a group", Toast.LENGTH_SHORT).show();
            }
        } else if (v == view.findViewById(R.id.logoutButton)) {
            Log.d("Button", "Logout Button");
            SharedPreferences pref = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
            SharedPreferences pref2 = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);

            //pref.edit().remove("token").putBoolean("logged_in", false).commit();
            pref.edit().clear().commit();
            pref2.edit().clear().commit();
            Fragment fragment = new LoginFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (v == view.findViewById(R.id.joinGroupIdButton)) {
            Log.d("Button", "Join Button");
            JSONObject json = new JSONObject();
            String usernameJoin = ((TextView) getActivity().findViewById(R.id.joinGroupIdEdit)).getText().toString();
            try {
                json.put("username_join", usernameJoin);
                BackendRequest be = new BackendRequest("PUT", "apiv1/queuegroups/join-group/", json.toString(), (MainActivity) getActivity());
                BackendRequest.activateJoinGroup(be);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (v == view.findViewById(R.id.spotifyLoginButton)) {
            mainActivity.authenticateSpotify();
        }
    }
}