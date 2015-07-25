package io.yeomans.groupqueue;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jason on 6/30/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment,
                container, false);
        view.findViewById(R.id.loginButton).setOnClickListener(this);
        view.findViewById(R.id.createAccountButton).setOnClickListener(this);
        view.findViewById(R.id.createNewAccountButton).setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        if (v == getActivity().findViewById(R.id.loginButton)) {
            //boolean leader = true;
            Log.d("Button", "Login Button");
            login();
            //Intent groupIntent = new Intent(getApplicationContext(), GroupActivity.class);
            //Log.wtf("PuttingIntExtra", ""+leader);
            //groupIntent.putExtra("extra_stuff", new String[]{""+leader, ""+leader});
            //startActivity(groupIntent);
        } else if (v == getActivity().findViewById(R.id.createNewAccountButton)) {
            Log.d("Button", "Create New Account Button");
            view.findViewById(R.id.loginLayout).setVisibility(View.GONE);
            v.setVisibility(View.GONE);
            view.findViewById(R.id.createAccountLayout).setVisibility(View.VISIBLE);
        } else if (v == getActivity().findViewById(R.id.createAccountButton)) {
            Log.d("Button", "Create Account Button");
            createAccount();

        }
    }

    public void login() {
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("username", ((EditText) view.findViewById(R.id.usernameEdit)).getText().toString()));
        paramList.add(new BasicNameValuePair("password", ((EditText) view.findViewById(R.id.passwordEdit)).getText().toString()));
        BackendRequest be = new BackendRequest("api-token-auth/", paramList, (MainActivity) getActivity());
        BackendRequest.login(be);
    }

    public void createAccount() {
        try {
            ArrayList<NameValuePair> loginParamList = new ArrayList<NameValuePair>();
            JSONObject json = new JSONObject("{}");
            //JSONObject userArrayJson = new JSONObject("{}");
            String username = ((EditText) view.findViewById(R.id.createUsernameEdit)).getText().toString();
            boolean fieldsExist = false;
            if (!username.equals("")) {
                json.put("username", username);
                String password = ((EditText) view.findViewById(R.id.createPasswordEdit)).getText().toString();
                String password2 = ((EditText) view.findViewById(R.id.createPassword2Edit)).getText().toString();
                if (password.equals(password2) && !password.equals("")) {
                    json.put("password", password);
                    String email = ((EditText) view.findViewById(R.id.createEmailEdit)).getText().toString();
                    if (!email.equals("") && email.contains("@") && email.contains(".")) {
                        json.put("email", email);
                        fieldsExist = true;
                        loginParamList.add(new BasicNameValuePair("username", username));
                        loginParamList.add(new BasicNameValuePair("password", password));
                    }
                }
            }
            if (!fieldsExist) {
                ((TextView) view.findViewById(R.id.createAccountErrorText)).setText("Make sure all information is correct and try again");
            } else {
                //json.put("user", userArrayJson);
                Log.d("CreateAccount", json.toString());
                BackendRequest be = new BackendRequest("POST", "apiv1/create-account/", json.toString(), (MainActivity) getActivity());
                be.setParamaters(loginParamList);
                BackendRequest.createAccount(be);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }
}
