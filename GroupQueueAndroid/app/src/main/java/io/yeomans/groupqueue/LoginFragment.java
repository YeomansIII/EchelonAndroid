package io.yeomans.groupqueue;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.jar.Attributes;

/**
 * Created by jason on 6/30/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private Activity parentActivity;
    private View view;

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
        view.findViewById(R.id.loginButton).setOnClickListener(this);
        view.findViewById(R.id.createAccountButton).setOnClickListener(this);
        this.view = view;
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == parentActivity.findViewById(R.id.loginButton)) {
            //boolean leader = true;
            Log.d("Button", "Login Button");
            login();
            //Intent groupIntent = new Intent(getApplicationContext(), GroupActivity.class);
            //Log.wtf("PuttingIntExtra", ""+leader);
            //groupIntent.putExtra("extra_stuff", new String[]{""+leader, ""+leader});
            //startActivity(groupIntent);
        } else if(v == parentActivity.findViewById(R.id.createAccountButton)) {
            Log.d("Button", "Create Account Button");
        }
    }

    public void login() {
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("username",((EditText)view.findViewById(R.id.usernameEdit)).getText().toString()));
        paramList.add(new BasicNameValuePair("password",((EditText)view.findViewById(R.id.passwordEdit)).getText().toString()));
        BackendRequest be = new BackendRequest("api-token-auth/", paramList, parentActivity);
        BackendRequest.login(be);
    }

    public void createAccount() {

    }
}
