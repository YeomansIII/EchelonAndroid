package io.yeomans.echelon;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jason on 6/30/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    TextInputLayout usernameWrapper, passwordWrapper;
    private boolean creatingAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment,
                container, false);

        view.findViewById(R.id.spotifyLoginButton).setOnClickListener(this);
        view.findViewById(R.id.anonymousLoginButton).setOnClickListener(this);

        mainActivity.toolbar.setVisibility(View.GONE);
        mainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        creatingAccount = false;

        this.view = view;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view2 = getActivity().getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        mainActivity.toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v == view.findViewById(R.id.spotifyLoginButton)) {
            Log.d("Login", "Spotify login touched");
            mainActivity.authenticateSpotify();
        } else if (v == view.findViewById(R.id.anonymousLoginButton)) {
            Log.d("Login", "Anonymous login touched");
            mainActivity.authenticateAnonymously();
        }
    }

    public interface OnEchelonLoginListener {
        void userLoggedIn();

        void userAccountCreated();
    }
}
