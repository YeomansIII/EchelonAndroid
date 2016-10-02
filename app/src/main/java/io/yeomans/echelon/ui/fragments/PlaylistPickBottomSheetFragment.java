package io.yeomans.echelon.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.yeomans.echelon.R;
import io.yeomans.echelon.interfaces.Picker;
import io.yeomans.echelon.models.Participant;
import io.yeomans.echelon.ui.activities.MainActivity;
import io.yeomans.echelon.util.Dependencies;

/**
 * Created by jason on 8/8/16.
 */
public class PlaylistPickBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener, MainActivity.OnBackPressedListener, Picker {

  public static final String TAG = PlaylistPickBottomSheetFragment.class.getSimpleName();

  @Bind(R.id.pickPlaylistSearchLayout)
  public RelativeLayout pickPlaylistSearchLayout;
  @Bind(R.id.pickPlaylistLibraryLayout)
  public RelativeLayout pickPlaylistLibraryLayout;
  @Bind(R.id.pickPlaylistBrowseLayout)
  public RelativeLayout pickPlaylistBrowseLayout;
  @Bind(R.id.pickPlaylistCategoryLayout)
  public LinearLayout pickPlaylistCategoryLayout;
  MainActivity mainActivity;

  public static PlaylistPickBottomSheetFragment newInstance() {
    PlaylistPickBottomSheetFragment frag = new PlaylistPickBottomSheetFragment();
    return frag;
  }

  private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        dismiss();
      }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
    }
  };

//  @Override
//  public void setupDialog(Dialog dialog, int style) {
//    super.setupDialog(dialog, style);
//    View contentView = View.inflate(getContext(), R.layout.playlist_pick_bottom_sheet_fragment, null);
//    dialog.setContentView(contentView);
//    ButterKnife.bind(this, contentView);
//
//    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
//    CoordinatorLayout.Behavior behavior = params.getBehavior();
//
//    if (behavior != null && behavior instanceof BottomSheetBehavior) {
//      ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
//    }
//
//    pickPlaylistSearchLayout.setOnClickListener(this);
//    pickPlaylistLibraryLayout.setOnClickListener(this);
//    pickPlaylistBrowseLayout.setOnClickListener(this);
//  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && getChildFragmentManager().getBackStackEntryCount() > 0) {
          if (getChildFragmentManager().getBackStackEntryCount() == 1) {
            pickPlaylistCategoryLayout.setVisibility(View.VISIBLE);
          }
          getChildFragmentManager().popBackStack();
          return true;
        }
        return false;
      }
    });
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View contentView = inflater.inflate(R.layout.playlist_pick_bottom_sheet_fragment, null);
    ButterKnife.bind(this, contentView);
    mainActivity = (MainActivity) getActivity();
    //mainActivity.setOnBackPressedListener(this);
    pickPlaylistSearchLayout.setOnClickListener(this);
    pickPlaylistLibraryLayout.setOnClickListener(this);
    pickPlaylistBrowseLayout.setOnClickListener(this);
    return contentView;
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    FragmentManager fragmentManager = getChildFragmentManager();
    switch (id) {
      case R.id.pickPlaylistSearchLayout: {
        fragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out)
          .add(R.id.pickPlaylistFragmentContainer, new SongSearchFragment(), "SONG_SEARCH_FRAG")
          .addToBackStack(null)
          .commit();
        pickPlaylistCategoryLayout.setVisibility(View.GONE);
        break;
      }
      case R.id.pickPlaylistLibraryLayout: {
        fragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out)
          .add(R.id.pickPlaylistFragmentContainer, YourMusicFragment.newInstance(true, false, true), "YOUR_MUSIC_FRAG")
          .addToBackStack(null)
          .commit();
        pickPlaylistCategoryLayout.setVisibility(View.GONE);
        break;
      }
      case R.id.pickPlaylistBrowseLayout: {
        fragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out)
          .add(R.id.pickPlaylistFragmentContainer, BrowseSongsFragment.newInstance(true, false, true), "BROWSE_SONG_FRAG")
          .addToBackStack(null)
          .commit();
        pickPlaylistCategoryLayout.setVisibility(View.GONE);
        break;
      }
    }
  }

  @Override
  public boolean onBackPressed() {
    if (getChildFragmentManager().getBackStackEntryCount() > 0) {
      getChildFragmentManager().popBackStack();
      return true;
    }
    return false;
  }

  @Override
  public void pickedPlaylist(String userId, String playListId) {
    Fragment targetFrag = getTargetFragment();
    Log.d(TAG, "will send picked: " + (targetFrag != null && targetFrag instanceof Picker));
    if (targetFrag != null && targetFrag instanceof Picker) {
      ((Picker) targetFrag).pickedPlaylist(userId, playListId);
      dismiss();
    }
  }
}
