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
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by jason on 8/10/15.
 */
public class GroupFragment extends Fragment {
    MainActivity mainActivity;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    QueueFragment queueFragment;
    ParticipantsFragment participantsFragment;
    boolean isDestroyed = false;
    private SharedPreferences groupSettings;
    private ControlBarFragment controlBar;
    private boolean leader;
    private boolean shouldExecuteOnResume;
    SharedPreferences groupPref;
    ChildEventListener groupWatchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        groupPref = mainActivity.getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_fragment,
                container, false);

        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);

        mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        mainActivity.getSupportActionBar().setTitle(groupPref.getString(MainActivity.PREF_GROUP_NAME, "error"));

        setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();
        isDestroyed = false;
        shouldExecuteOnResume = false;

        Bundle startingIntentBundle = this.getArguments();
        if (startingIntentBundle != null) {
            String[] extras = startingIntentBundle.getStringArray("extra_stuff");
            //playId = extras[0];
            leader = Boolean.parseBoolean(extras[1]);
            Log.wtf("Test", "leader: " + leader);
        }
        controlBar = (ControlBarFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("CONTROL_FRAG");
        if (controlBar != null) {
            if (leader) {
                controlBar.ready(true);
                if (!mainActivity.playerReady) {
                    //mainActivity.configPlayer();
                }
            } else {
                Log.d("Group", "Not leader");
            }
        }

        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.groupFragmentFrame, new QueueFragment(), "QUEUE_FRAG").commit();
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Log.d("GroupFragment", "Setting up viewPager");
        if (viewPagerAdapter == null) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
            queueFragment = new QueueFragment();
            adapter.addFrag(queueFragment, "Queue");
            participantsFragment = new ParticipantsFragment();
            adapter.addFrag(participantsFragment, "Participants");
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

    public void leaveGroup() {
        if (mainActivity.mPlayer != null) {
            mainActivity.mPlayer.pause();
            mainActivity.mPlayerCherry = true;
        }
        groupSettings.edit().clear().commit();
        controlBar.getView().setVisibility(View.GONE);
        Log.d("Group", "Leaving Group");
        isDestroyed = true;
    }
}
