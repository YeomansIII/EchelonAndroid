package io.yeomans.groupqueue;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by jason on 6/30/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private Activity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment,
                container, false);
        ((Button)parentActivity.findViewById(R.id.loginButton)).setOnClickListener(this);
        ((Button)parentActivity.findViewById(R.id.createAccountButton)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == parentActivity.findViewById(R.id.loginButton)) {
            //boolean leader = true;
            Log.d("Button", "Login Button");
            //Intent groupIntent = new Intent(getApplicationContext(), GroupActivity.class);
            //Log.wtf("PuttingIntExtra", ""+leader);
            //groupIntent.putExtra("extra_stuff", new String[]{""+leader, ""+leader});
            //startActivity(groupIntent);
        } else if(v == parentActivity.findViewById(R.id.createAccountButton)) {
            Log.d("Button", "Create Account Button");
        }
    }

    public void login() {

    }

    public void createAccount() {

    }
}
