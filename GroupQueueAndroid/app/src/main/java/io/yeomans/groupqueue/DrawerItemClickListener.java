package io.yeomans.groupqueue;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by jason on 7/6/15.
 */
public class DrawerItemClickListener implements ListView.OnItemClickListener {

    private Activity parentActivity;
    private CharSequence mTitle;

    public DrawerItemClickListener(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        Log.d("Drawer", "Position: " + position);
        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();
//
//        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
    }

//    @Override
//    public void setTitle(CharSequence title) {
//        mTitle = title;
//        parentActivity.getActionBar().setTitle(mTitle);
//    }
}