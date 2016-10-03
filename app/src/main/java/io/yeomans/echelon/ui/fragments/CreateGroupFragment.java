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
import io.github.kaaes.spotify.webapi.core.models.Playlist;
import io.github.kaaes.spotify.webapi.core.models.PlaylistTrack;
import io.yeomans.echelon.R;
import io.yeomans.echelon.interfaces.Picker;
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
public class CreateGroupFragment extends Fragment implements View.OnClickListener, Picker {

  private static final String TAG = CreateGroupFragment.class.getSimpleName();

  public View view;
  public MainActivity mainActivity;
  public SharedPreferences mainPref;
  @Bind(R.id.createGroupPrivacyPublicRadio)
  public RadioButton rbPublic;
  @Bind(R.id.createGroupPrivacyPasswordRadio)
  public RadioButton rbPassword;
  @Bind(R.id.createGroupPrivacyFriendsRadio)
  public RadioButton rbFriends;
  @Bind(R.id.createGroupPrivacyInviteRadio)
  public RadioButton rbInvite;
  @Bind(R.id.createGroupNameEditWrapper)
  public TextInputLayout createGroupNameEditWrapper;
  @Bind(R.id.createGroupPasswordEditWrapper)
  public TextInputLayout createGroupPasswordEditWrapper;
  @Bind(R.id.createGroupCreateButton)
  public Button createGroupCreateButton;
  @Bind(R.id.createGroupDPlaylistPickButton)
  public Button createGroupDPlaylistPickButton;

  @Bind(R.id.createGroupDPlaylistLayout)
  public RelativeLayout createGroupDPlaylistLayout;
  @Bind(R.id.playlistItemHorImage)
  public ImageView createGroupDPlaylistImage;
  @Bind(R.id.playlistItemHorNameText)
  public TextView createGroupDPlaylistNameText;
  @Bind(R.id.playlistItemHorDescriptionText)
  public TextView createGroupDPlaylistDescriptionText;

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
    View view = inflater.inflate(R.layout.create_group_fragment,
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
          createGroupPasswordEditWrapper.setVisibility(View.VISIBLE);
          selectedPrivacy = "password";
        } else {
          createGroupPasswordEditWrapper.setVisibility(View.GONE);
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

    createGroupCreateButton.setOnClickListener(this);
    createGroupDPlaylistPickButton.setOnClickListener(this);

    this.view = view;
    return view;
  }

  @Override
  public void onClick(View v) {
    if (v == view.findViewById(R.id.createGroupCreateButton)) {
      final String name = createGroupNameEditWrapper.getEditText().getText().toString();
      DatabaseReference refQueueGroups = dependencies.getDatabase().getReference("queuegroups/" + name + "/name");
      refQueueGroups.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() != null) {
            createGroupNameEditWrapper.setErrorEnabled(true);
            createGroupNameEditWrapper.setError("That group name is already taken, try another one");
          } else {
            FirebaseUser fUser = dependencies.getAuth().getCurrentUser();
            if (fUser != null) {
              dependencies.getCurrentUserReference().child("cur_group").setValue(name);
              DatabaseReference ref = dependencies.getDatabase().getReference("queuegroups");
              Map<String, Object> map = new HashMap<>();
              map.put("name", name);
              map.put("created", ServerValue.TIMESTAMP);
              map.put("leader", fUser.getUid());
              if (defaultPlaylist != null) {
                Map<String, Object> playlistMap = new HashMap<>();
                playlistMap.put("external_url", defaultPlaylist.external_urls.get("spotify"));
                playlistMap.put("followers", defaultPlaylist.followers.total);
                playlistMap.put("id", defaultPlaylist.id);
                playlistMap.put("name", defaultPlaylist.name);
                playlistMap.put("description", defaultPlaylist.description);
                playlistMap.put("image", defaultPlaylist.images.get(0).url);
                playlistMap.put("type", defaultPlaylist.type);
                playlistMap.put("uri", defaultPlaylist.uri);
                Map<String, Object> trackMap = new HashMap<>();
                for (PlaylistTrack track : defaultPlaylist.tracks.items) {
                  String pushKey = ref.push().getKey();
                  trackMap.put(pushKey, ModelUtils.createTrackMap(track.track, pushKey));
                }
                playlistMap.put("tracks", trackMap);
                map.put("defaultPlaylist", playlistMap);
              }
              // map.put("privacy", selectedPrivacy);
//                        if (selectedPrivacy.equals("password")) {
//                            map.put("password", createGroupPasswordEditWrapper.getEditText().getText().toString());
//                        }
              Log.d(TAG, new JSONObject(map).toString());
              ref.child(name).setValue(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                  if (databaseError != null) {
                    Log.d(TAG, databaseError.getDetails());
                    Log.d(TAG, databaseError.getMessage());
                    Log.d(TAG, "" + databaseError.getCode());
                  }
                }
              });
              ref.child(name + "/participants/" + fUser.getUid()).setValue(true);

              SharedPreferences groupSettings = mainActivity.getSharedPreferences(PreferenceNames.GROUP_PREFS_NAME, 0);
              groupSettings.edit().putString(PreferenceNames.PREF_GROUP_NAME, name).putString(PreferenceNames.PREF_GROUP_LEADER_UID, fUser.getUid()).apply();
              View view = mainActivity.getCurrentFocus();
              if (view != null) {
                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
                  Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
              }
              Fragment fragment = new GroupFragment();
              Bundle bundle = new Bundle();
              bundle.putStringArray("extra_stuff", new String[]{"" + true, "" + true});
              fragment.setArguments(bundle);
              FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
              fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                .replace(R.id.container, fragment, "GROUP_FRAG")
                .commit();
            }
          }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {

        }
      });
    } else if (v == view.findViewById(R.id.createGroupDPlaylistPickButton)) {
      PlaylistPickBottomSheetFragment pickBottom = PlaylistPickBottomSheetFragment.newInstance();
      pickBottom.setTargetFragment(this, 0);
      pickBottom.show(mainActivity.getSupportFragmentManager(), "PLAYLIST_PICK");
      //    playlistPickBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  public void setDefaultPlaylist(String userId, String playlistId) {
    Call<Playlist> call = Dependencies.INSTANCE.getSpotify().getPlaylist(userId, playlistId);
    call.enqueue(new Callback<Playlist>() {
      @Override
      public void onResponse(Call<Playlist> call, Response<Playlist> response) {
        Log.i("Playlists", "Get playlist results");
        defaultPlaylist = response.body();
        Picasso.with(createGroupDPlaylistImage.getContext()).load(Uri.parse(defaultPlaylist.images.get(0).url)).into(createGroupDPlaylistImage);
        createGroupDPlaylistNameText.setText(defaultPlaylist.name);
        createGroupDPlaylistDescriptionText.setText(defaultPlaylist.description);
        createGroupDPlaylistLayout.setVisibility(View.VISIBLE);
      }

      @Override
      public void onFailure(Call<Playlist> call, Throwable t) {
        Log.wtf(TAG, t.toString() + "   " + t.getMessage());
      }
    });
  }

  @Override
  public void pickedPlaylist(String userId, String playlistId) {
    setDefaultPlaylist(userId, playlistId);
  }
}
