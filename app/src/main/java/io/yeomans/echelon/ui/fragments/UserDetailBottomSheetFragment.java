package io.yeomans.echelon.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.kaaes.spotify.webapi.core.models.ArtistSimple;
import io.github.kaaes.spotify.webapi.core.models.Track;
import io.yeomans.echelon.R;
import io.yeomans.echelon.models.Participant;
import io.yeomans.echelon.util.Dependencies;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 8/8/16.
 */
public class UserDetailBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

  public static final String TAG = UserDetailBottomSheetFragment.class.getSimpleName();

  @Bind(R.id.userDetailProfileImage)
  public ImageView userDetailProfileImage;
  @Bind(R.id.userDetailNameText)
  public TextView userDetailNameText;
  @Bind(R.id.userDetailOnlineImage)
  public ImageView userDetailOnlineImage;
  @Bind(R.id.userDetailOnlineText)
  public TextView userDetailOnlineText;
  private String uid;
  private Participant participant;

  public static UserDetailBottomSheetFragment newInstance(String uid) {
    UserDetailBottomSheetFragment frag = new UserDetailBottomSheetFragment();
    Bundle argsBundle = new Bundle();
    argsBundle.putString("uid", uid);
    frag.setArguments(argsBundle);
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

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();
    BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
    View contentView = View.inflate(getContext(), R.layout.user_bottom_sheet_fragment, null);
    dialog.setContentView(contentView);
    ButterKnife.bind(this, contentView);
    uid = bundle.getString("uid");

    final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
    CoordinatorLayout.Behavior behavior = params.getBehavior();

    Dependencies.INSTANCE.getParticipantReference(uid).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        participant = dataSnapshot.getValue(Participant.class);
        userDetailNameText.setText(participant.getDisplay_name() + "#" + participant.getFriend_code());
        Picasso.with(getContext()).load(participant.getImage_url()).placeholder(R.drawable.account).into(userDetailProfileImage);
        userDetailOnlineText.setText(participant.isOnline() ? "Online" : "Offline");
        if (participant.isOnline()) {
          userDetailOnlineImage.setSelected(true);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    if (behavior != null && behavior instanceof BottomSheetBehavior) {
      ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
    }

    //songDetailViewSpotifyButton.setOnClickListener(this);

    return dialog;
  }

  @Override
  public void onClick(View view) {
//    if (view == songDetailViewSpotifyButton) {
//      Log.d(TAG, trackData.external_urls.get("spotify"));
//      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trackData.external_urls.get("spotify")));
//      startActivity(browserIntent);
//    }
  }
}
