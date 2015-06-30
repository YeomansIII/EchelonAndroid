package io.yeomans.groupqueue;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

//Groupify
public class MainActivity extends Activity implements View.OnClickListener {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "8b81e3deddce42c4b0f2972e181b8a3a";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "groupqueue://callback";

    private static final int REQUEST_CODE = 9001;
    private static final int CONTENT_VIEW_ID = 10101010;

    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new ViewGroup.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (savedInstanceState == null) {
            Fragment newFragment = new LoginFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(CONTENT_VIEW_ID, newFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onClick(final View view) {

        if (view == findViewById(R.id.createGroupButton)) {
            boolean leader = true;
            Log.d("Button", "Create Button");
            Intent groupIntent = new Intent(getApplicationContext(), GroupActivity.class);
            Log.wtf("PuttingIntExtra", ""+leader);
            groupIntent.putExtra("extra_stuff", new String[]{""+leader, ""+leader});
            startActivity(groupIntent);
        } else if(view == findViewById(R.id.joinGroupButton)) {
            Log.d("Button", "Join Button");
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}