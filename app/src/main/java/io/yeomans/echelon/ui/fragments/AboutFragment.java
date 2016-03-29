package io.yeomans.echelon.ui.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.yeomans.echelon.R;
import io.yeomans.echelon.ui.activities.MainActivity;

/**
 * Created by jason on 7/10/15.
 */
public class AboutFragment extends Fragment {

    private View view;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment,
                container, false);

        PackageInfo pInfo = null;
        try {
            pInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
            ((TextView) view.findViewById(R.id.aboutVersionText)).setText("Version: " + pInfo.versionName + " - " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        this.view = view;
        return view;
    }
}