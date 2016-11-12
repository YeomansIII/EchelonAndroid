package io.yeomans.echelon.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.kaaes.spotify.webapi.core.models.PlaylistTrack;
import io.yeomans.echelon.R;
import io.yeomans.echelon.interfaces.Picker;
import io.yeomans.echelon.models.Playlist;
import io.yeomans.echelon.models.SpotifySong;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.Dependencies;
import io.yeomans.echelon.util.ModelUtils;
import io.yeomans.echelon.util.PreferenceNames;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 7/1/15.
 */
public class GroupSettingsFragment extends Fragment implements View.OnClickListener, Picker {

  private static final String TAG = GroupSettingsFragment.class.getSimpleName();

  public View view;
  public MainActivity mainActivity;
  public SharedPreferences mainPref;
  @Bind(R.id.groupSettingsPrivacyPublicRadio)
  public RadioButton rbPublic;
  @Bind(R.id.groupSettingsPrivacyPasswordRadio)
  public RadioButton rbPassword;
  @Bind(R.id.groupSettingsPrivacyFriendsRadio)
  public RadioButton rbFriends;
  @Bind(R.id.groupSettingsPrivacyInviteRadio)
  public RadioButton rbInvite;
  @Bind(R.id.groupSettingsNameEditWrapper)
  public TextInputLayout groupSettingsNameEditWrapper;
  @Bind(R.id.groupSettingsPasswordEditWrapper)
  public TextInputLayout groupSettingsPasswordEditWrapper;
  @Bind(R.id.groupSettingsSaveButton)
  public Button groupSettingsCreateButton;
  @Bind(R.id.groupSettingsDPlaylistPickButton)
  public Button groupSettingsDPlaylistPickButton;

  @Bind(R.id.groupSettingsDPlaylistLayout)
  public RelativeLayout groupSettingsDPlaylistLayout;
  @Bind(R.id.playlistItemHorImage)
  public ImageView groupSettingsDPlaylistImage;
  @Bind(R.id.playlistItemHorNameText)
  public TextView groupSettingsDPlaylistNameText;
  @Bind(R.id.playlistItemHorDescriptionText)
  public TextView groupSettingsDPlaylistDescriptionText;

  String selectedPrivacy;
  Dependencies dependencies;
  private BottomSheetBehavior playlistPickBottomSheetBehavior;
  Playlist defaultPlaylist;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dependencies = Dependencies.INSTANCE;
    mainActivity = (MainActivity) getActivity();
    mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.group_settings_fragment,
      container, false);
    ButterKnife.bind(this, view);

    mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT);
    mainActivity.getSupportActionBar().setTitle("");

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
          groupSettingsPasswordEditWrapper.setVisibility(View.VISIBLE);
          selectedPrivacy = "password";
        } else {
          groupSettingsPasswordEditWrapper.setVisibility(View.GONE);
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

    groupSettingsCreateButton.setOnClickListener(this);
    groupSettingsDPlaylistPickButton.setOnClickListener(this);

    dependencies.getCurrentGroupReference().addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
          groupSettingsNameEditWrapper.getEditText().setText((String) dataSnapshot.child("name").getValue());
          setDefaultPlaylist(dataSnapshot.child("defaultPlaylist").getValue(Playlist.class));
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    this.view = view;
    return view;
  }

  @Override
  public void onClick(View v) {
    if (v == view.findViewById(R.id.groupSettingsSaveButton)) {
      final String name = groupSettingsNameEditWrapper.getEditText().getText().toString();
      FirebaseUser fUser = dependencies.getAuth().getCurrentUser();
      if (fUser != null) {
        DatabaseReference ref = dependencies.getCurrentGroupReference();
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("created", ServerValue.TIMESTAMP);
        map.put("leader", fUser.getUid());
        if (defaultPlaylist != null) {
          Map<String, Object> playlistMap = new HashMap<>();
          Log.d(TAG, "External URL: " + defaultPlaylist.getExternalUrl());
          playlistMap.put("external_url", defaultPlaylist.getExternalUrl());
          playlistMap.put("followers", defaultPlaylist.getFollowers());
          playlistMap.put("id", defaultPlaylist.getId());
          playlistMap.put("name", defaultPlaylist.getName());
          playlistMap.put("description", defaultPlaylist.getDescription());
          playlistMap.put("image", defaultPlaylist.getImage());
          playlistMap.put("type", defaultPlaylist.getType());
          playlistMap.put("uri", defaultPlaylist.getUri());
          playlistMap.put("tracks", defaultPlaylist.getTracksMap());
          Log.d(TAG, "Playlist Map: " + playlistMap);
          map.put("defaultPlaylist", playlistMap);
        }
        // map.put("privacy", selectedPrivacy);
//                        if (selectedPrivacy.equals("password")) {
//                            map.put("password", groupSettingsPasswordEditWrapper.getEditText().getText().toString());
//                        }
        Log.d(TAG, new JSONObject(map).toString());
        ref.updateChildren(map, new DatabaseReference.CompletionListener() {
          @Override
          public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError != null) {
              Log.d(TAG, databaseError.getDetails());
              Log.d(TAG, databaseError.getMessage());
              Log.d(TAG, "" + databaseError.getCode());
            }
          }
        });

        View view = mainActivity.getCurrentFocus();
        if (view != null) {
          InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
            Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Fragment fragment = new GroupFragment();
        Bundle bundle = new Bundle();
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
          .replace(R.id.container, fragment, "GROUP_FRAG")
          .commit();
      }
    } else if (v == view.findViewById(R.id.groupSettingsDPlaylistPickButton)) {
      PlaylistPickBottomSheetFragment pickBottom = PlaylistPickBottomSheetFragment.newInstance();
      pickBottom.setTargetFragment(this, 0);
      pickBottom.show(mainActivity.getSupportFragmentManager(), "PLAYLIST_PICK");
      //    playlistPickBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  public void setDefaultPlaylist(String userId, String playlistId) {
    Call<io.github.kaaes.spotify.webapi.core.models.Playlist> call = Dependencies.INSTANCE.getSpotify().getPlaylist(userId, playlistId);
    call.enqueue(new Callback<io.github.kaaes.spotify.webapi.core.models.Playlist>() {
      @Override
      public void onResponse(Call<io.github.kaaes.spotify.webapi.core.models.Playlist> call, Response<io.github.kaaes.spotify.webapi.core.models.Playlist> response) {
        Log.i("Playlists", "Get playlist results");
        defaultPlaylist = new Playlist(response.body());
        setDefaultPlaylist(defaultPlaylist);
      }

      @Override
      public void onFailure(Call<io.github.kaaes.spotify.webapi.core.models.Playlist> call, Throwable t) {
        Log.wtf(TAG, t.toString() + "   " + t.getMessage());
      }
    });
  }

  public void setDefaultPlaylist(Playlist playlist) {
    defaultPlaylist = playlist;
    Picasso.with(groupSettingsDPlaylistImage.getContext()).load(Uri.parse(playlist.getImage())).into(groupSettingsDPlaylistImage);
    groupSettingsDPlaylistNameText.setText(playlist.getName());
    groupSettingsDPlaylistDescriptionText.setText(playlist.getDescription());
    groupSettingsDPlaylistLayout.setVisibility(View.VISIBLE);
  }

  @Override
  public void pickedPlaylist(String userId, String playlistId) {
    setDefaultPlaylist(userId, playlistId);
  }
}
