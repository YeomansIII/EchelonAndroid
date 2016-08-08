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
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.kaaes.spotify.webapi.core.models.ArtistSimple;
import io.github.kaaes.spotify.webapi.core.models.Track;
import io.yeomans.echelon.R;
import io.yeomans.echelon.util.Dependencies;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 8/8/16.
 */
public class SongDetailBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

  public static final String TAG = SongDetailBottomSheetFragment.class.getSimpleName();

  @Bind(R.id.songDetailTitleText)
  public TextView songDetailTitleText;
  @Bind(R.id.songDetailArtistText)
  public TextView songDetailArtistText;
  @Bind(R.id.songDetailViewSpotifyButton)
  public TextView songDetailViewSpotifyButton;
  @Bind(R.id.songDetailAddPlaylistButton)
  public TextView songDetailAddPlaylistButton;
  private String trackUid;
  private Track trackData;

  public static SongDetailBottomSheetFragment newInstance(String trackuid) {
    SongDetailBottomSheetFragment frag = new SongDetailBottomSheetFragment();
    Bundle argsBundle = new Bundle();
    argsBundle.putString("trackuid", trackuid);
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
    View contentView = View.inflate(getContext(), R.layout.song_bottom_sheet_fragment, null);
    dialog.setContentView(contentView);
    ButterKnife.bind(this, contentView);
    trackUid = bundle.getString("trackuid");

    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
    CoordinatorLayout.Behavior behavior = params.getBehavior();

    Call<Track> call = Dependencies.INSTANCE.getSpotify().getTrack(trackUid);
    call.enqueue(new Callback<Track>() {
      @Override
      public void onResponse(Call<Track> call, Response<Track> response) {
        trackData = response.body();
        songDetailTitleText.setText(trackData.name);
        String artists = "";
        for (ArtistSimple artist : trackData.artists) {
          artists += artist.name + ", ";
        }
        artists = artists.replaceAll(", $", "");
        songDetailArtistText.setText(artists);
      }

      @Override
      public void onFailure(Call<Track> call, Throwable t) {

      }
    });

    if (behavior != null && behavior instanceof BottomSheetBehavior) {
      ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
    }

    songDetailViewSpotifyButton.setOnClickListener(this);

    return dialog;
  }

  @Override
  public void onClick(View view) {
    if (view == songDetailViewSpotifyButton) {
      Log.d(TAG, trackData.external_urls.get("spotify"));
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trackData.external_urls.get("spotify")));
      startActivity(browserIntent);
    }
  }
}
