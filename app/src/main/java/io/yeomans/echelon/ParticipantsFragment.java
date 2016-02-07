package io.yeomans.echelon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jason on 6/26/15.
 */
public class ParticipantsFragment extends Fragment implements View.OnClickListener {

    private String playId;
    private boolean leader;
    public boolean isDestroyed;
    private boolean shouldExecuteOnResume;
    private SharedPreferences groupSettings;

    private View view;
    private MainActivity mainActivity;
    private ControlBarFragment controlBar;
    private ValueEventListener participantListener;
    private ArrayList<Participant> participantsArray;
    private Firebase thisGroupRef;
    private String groupName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        groupSettings = getActivity().getSharedPreferences(MainActivity.GROUP_PREFS_NAME, 0);
        groupName = groupSettings.getString(MainActivity.PREF_GROUP_NAME, null);
        isDestroyed = false;
        shouldExecuteOnResume = false;
        thisGroupRef = mainActivity.myFirebaseRef.child("queuegroups/" + groupName + "/participants");

        participantListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                participantsArray = new ArrayList<>();
                for (DataSnapshot o : dataSnapshot.getChildren()) {
                    participantsArray.add(o.getValue(Participant.class));
                }
                buildParticiantList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.participants_fragment,
                container, false);

        ///////
        ///////
        //((TextView) view.findViewById(R.id.groupIdText)).setText(groupSettings.getString(MainActivity.PREF_GROUP_OWNER_USERNAME, "error"));

        //mainActivity.toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        //mainActivity.getSupportActionBar().setTitle("Echelon");

        this.view = view;
//        BackendRequest be = new BackendRequest("GET", mainActivity);
//        BackendRequest.refreshGroupQueue(be);

        //view.findViewById(R.id.groupAddSongButton).setOnClickListener(this);
        //controlBar.getView().findViewById(R.id.groupAddSongButton).setVisibility(View.VISIBLE);
        thisGroupRef.addValueEventListener(participantListener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        // buildParticiantList();
        //if(shouldExecuteOnResume) {
        //BackendRequest be = new BackendRequest("GET", mainActivity);
        //BackendRequest.refreshGroupQueue(be);
        //} else {
        //    shouldExecuteOnResume = true;
        //}
        //Log.d("Group","Group onResume()");
        //refreshQueueList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thisGroupRef.removeEventListener(participantListener);
        //getView().findViewById(R.id.groupAddSongButton).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void buildParticiantList() {
        Log.d("BuildParticipantList", "Build Participant List");
        if (getActivity() != null) {
            //String participantsJsonString = pref.getString(MainActivity.PREF_GROUP_PARTICIPANTS_JSON, null);
            Log.d("BuildParticipantList", "" + participantsArray.toString());
            if (participantsArray != null) {
                LinearLayout participantList = (LinearLayout) view.findViewById(R.id.participantListLayout);
                participantList.removeAllViews();
                for (int i = 0; i < participantsArray.size(); i++) {
                    Participant curPart = participantsArray.get(i);

                    RelativeLayout rt = (RelativeLayout) mainActivity.getLayoutInflater().inflate(R.layout.user_item, null);
                    ImageView userImage = (ImageView) rt.findViewById(R.id.userImage);
                    TextView userNameText = (TextView) rt.findViewById(R.id.userNameText);

                    userNameText.setText(curPart.getDisplayName());
                    userImage.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_account_grey600_48dp));
                    if (curPart.getImageUrl() != null) {
                        Picasso.with(getContext()).load(curPart.getImageUrl()).transform(new CircleTransform()).into(userImage);
                    }
                    participantList.addView(rt);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

    }
}
