package io.yeomans.echelon.callbacks;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.github.kaaes.spotify.webapi.core.models.Track;
import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.fragments.GroupFragment;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.ModelUtils;
import io.yeomans.echelon.util.PreferenceNames;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 6/25/16.
 */
public class AddSongCallback implements Callback<Track> {

  AppCompatActivity activity;

  public AddSongCallback(AppCompatActivity activity) {
    this.activity = activity;
  }

  @Override
  public void onResponse(Call<Track> call, Response<Track> response) {
    Track track = response.body();
    DatabaseReference push = Dependencies.INSTANCE.getDatabase().getReference("queuegroups/" + Dependencies.INSTANCE.getGroupPreferences().getString(PreferenceNames.PREF_GROUP_NAME, "") + "/tracks").push();
    push.setValue(ModelUtils.createTrackMap(track, push.getKey()));
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    GroupFragment groupFragment = (GroupFragment) fragmentManager.findFragmentByTag("GROUP_FRAG");
    if (groupFragment != null && !groupFragment.isVisible()) {
      View view = activity.getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
          Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }
      fragmentTransaction.replace(R.id.container, groupFragment).commit();
    }
  }


  @Override
  public void onFailure(Call<Track> call, Throwable t) {

  }
}
