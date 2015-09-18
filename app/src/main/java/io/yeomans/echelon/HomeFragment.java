package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jason on 7/1/15.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    private SharedPreferences mainPref, groupPref;

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
        View view = inflater.inflate(R.layout.home_fragment,
                container, false);
        view.findViewById(R.id.createGroupButton).setOnClickListener(this);
        view.findViewById(R.id.joinGroupButton).setOnClickListener(this);
        view.findViewById(R.id.logoutButton).setOnClickListener(this);
        //view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);

        mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT);
        mainActivity.getSupportActionBar().setTitle("");

        this.view = view;
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        Drawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
//        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
//        actionBar.setBackgroundDrawable(colorDrawable);
//        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.createGroupButton)) {
            boolean leader = true;
            Log.d("Button", "Create Button");
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (groupPref.getString(MainActivity.PREF_GROUP_NAME, null) == null) {
                if (mainPref.getBoolean(MainActivity.PREF_SPOTIFY_AUTHENTICATED, false) && mainPref.getString(MainActivity.PREF_SPOTIFY_PRODUCT, "").equalsIgnoreCase("premium")) {
//                    Firebase refQueueGroups = mainActivity.myFirebaseRef.child("queuegroups");
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("name", "testname");
//                    refQueueGroups.push().setValue(map);
                    fragmentManager.beginTransaction().replace(R.id.container, new CreateGroupFragment(), "CREATE_GROUP_FRAG").addToBackStack(null).commit();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please login to your Spotify Premium account under settings", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in a group", Toast.LENGTH_SHORT).show();
            }
        } else if (v == view.findViewById(R.id.joinGroupButton)) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (groupPref.getString(MainActivity.PREF_GROUP_NAME, null) == null) {
                Log.d("Button", "Join Group Button");
                fragmentManager.beginTransaction().replace(R.id.container, new JoinGroupFragment(), "JOIN_GROUP_FRAG").addToBackStack(null).commit();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in a group", Toast.LENGTH_SHORT).show();
            }
        } else if (v == view.findViewById(R.id.logoutButton)) {
            Log.d("Button", "Logout Button");
            SharedPreferences pref = getActivity().getSharedPreferences(MainActivity.MAIN_PREFS_NAME, 0);
            SharedPreferences pref2 = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);

            //pref.edit().remove("token").putBoolean("logged_in", false).commit();
            pref.edit().clear().apply();
            pref2.edit().clear().apply();
            mainActivity.myFirebaseRef.unauth();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ((ControlBarFragment) fragmentManager.findFragmentByTag("CONTROL_FRAG")).unReady();
            fragmentManager.beginTransaction().replace(R.id.container, new LoginFragment()).addToBackStack(null).commit();
//        } else if (v == view.findViewById(R.id.spotifyLoginButton)) {
//            mainActivity.authenticateSpotify();
//        }
        }
    }
}