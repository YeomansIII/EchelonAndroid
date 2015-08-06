package io.yeomans.groupqueue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jason on 7/24/15.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    public MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment,
                container, false);

        view.findViewById(R.id.settingsSpotifyLogin).setOnClickListener(this);
        view.findViewById(R.id.settingsSpotifyLogout).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v == getView().findViewById(R.id.settingsSpotifyLogin)) {
            mainActivity.authenticateSpotify();
        } else if(v == getView().findViewById(R.id.settingsSpotifyLogout)) {

        }
    }
}
