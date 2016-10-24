package io.yeomans.echelon.ui.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.PreferenceNames;

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
    mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
    groupPref = mainActivity.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.home_fragment,
      container, false);
    View createButton = view.findViewById(R.id.createGroupButton);
    if (mainPref.getString(PreferenceNames.PREF_USER_AUTH_TYPE, "").equals("spotify") && mainPref.getBoolean(PreferenceNames.PREF_SPOTIFY_AUTHENTICATED, false) && mainPref.getString(PreferenceNames.PREF_SPOTIFY_PRODUCT, "").equals("premium")) {
      createButton.setOnClickListener(this);
    } else {
      createButton.setVisibility(View.GONE);
    }
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
      mainActivity.createCreateGroupFrag();
    } else if (v == view.findViewById(R.id.joinGroupButton)) {
      mainActivity.createJoinGroupFrag();
    } else if (v == view.findViewById(R.id.logoutButton)) {
      Log.d("Button", "Logout Button");
      mainActivity.logout();
    }
  }
}
