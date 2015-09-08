package io.yeomans.echelon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jason on 8/10/15.
 */
public class AddSongFragment extends Fragment {
    MainActivity mainActivity;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    QueueFragment queueFragment;
    boolean isDestroyed = false;
    private ControlBarFragment controlBar;
    private boolean leader;
    private boolean shouldExecuteOnResume;
    private SongSearchFragment songSearchFragment;
    private BrowseSongsFragment browseSongsFragment;
    private SharedPreferences groupSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_song_fragment,
                container, false);

        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);

        mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        mainActivity.getSupportActionBar().setTitle("Echelon");

        //setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();

//            ControlBarFragment controlBar = (ControlBarFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG");
//            View cL = controlBar.getView().findViewById(R.id.controlCoordinatorLayout);
//            Snackbar snackbar = Snackbar
//                    .make(cL, "Had a snack at Snackbar", Snackbar.LENGTH_LONG);
//            View snackbarView = snackbar.getView();
//            snackbarView.setBackgroundColor(Color.DKGRAY);
//            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
//            textView.setTextColor(Color.YELLOW);
//            snackbar.show();


        viewPager = (ViewPager) view.findViewById(R.id.add_song_tab_viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.add_song_tab_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        //Toast.makeText(mainActivity.context, "Tab One Selected", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        browseSongsFragment.select();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Log.d("GroupFragment", "Setting up viewPager");
        if (viewPagerAdapter == null) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
            songSearchFragment = new SongSearchFragment();
            adapter.addFrag(songSearchFragment, "Search");
            browseSongsFragment = new BrowseSongsFragment();
            adapter.addFrag(browseSongsFragment, "Browse");
            viewPagerAdapter = adapter;
            viewPager.setAdapter(adapter);
        } else {
            viewPager.setAdapter(viewPagerAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        // queueFragment.onResume();
        //participantsFragment.onResume();
    }
}
