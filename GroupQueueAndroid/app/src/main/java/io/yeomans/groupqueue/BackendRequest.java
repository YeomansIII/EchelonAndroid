package io.yeomans.groupqueue;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by jason on 6/30/15.
 */
public class BackendRequest {

    public static final String BASE_URL = "http://192.168.2.16:8000/";

    private String url;
    private String method;
    private Header[] headers;
    private ArrayList<NameValuePair> paramaters;
    private String jsonEntity;
    private Activity mainActivity;

    public BackendRequest(String method, String url, String jsonEntity, Activity mainActivity) {
        this.method = method;
        this.url = url;
        this.jsonEntity = jsonEntity;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String method, String url, Activity mainActivity) {
        this.method = method;
        this.url = url;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String url, Header[] headers, Activity mainActivity) {
        this.method = "GET";
        this.url = url;
        this.headers = headers;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String method, String url, Header[] headers, Activity mainActivity) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.mainActivity = mainActivity;
    }

    public BackendRequest(String url, ArrayList<NameValuePair> paramaters, Activity mainActivity) {
        this.method = "POST";
        this.url = url;
        this.paramaters = paramaters;
        this.mainActivity = mainActivity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public ArrayList<NameValuePair> getParamaters() {
        return paramaters;
    }

    public void setParamaters(ArrayList<NameValuePair> paramaters) {
        this.paramaters = paramaters;
    }

    public String getJsonEntity() {
        return jsonEntity;
    }

    public void setJsonEntity(String jsonEntity) {
        this.jsonEntity = jsonEntity;
    }

    public Activity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static void login(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("POST")) {
            final Activity activity = be.getMainActivity();
            AsyncTask<BackendRequest, Void, String> get = new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(BASE_URL + be.getUrl());
                        post.setEntity(new UrlEncodedFormEntity(be.getParamaters()));
                        HttpResponse responsePost = client.execute(post);
                        HttpEntity resEntityPost = responsePost.getEntity();
                        String response = "";
                        if (resEntityPost != null) {
                            //do something with the response
                            response = EntityUtils.toString(resEntityPost);
                            if (response.contains("token")) {
                                JSONObject jsonT = new JSONObject(response);
                                String token = jsonT.getString("token");
                                Log.d("login", "Token: " + token);
                                SharedPreferences settings = activity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("token", token);

                                HttpGet get = new HttpGet(BASE_URL + "apiv1/listeners/my-user-info/");
                                get.addHeader("Authorization", "Token " + token);
                                HttpResponse responseGet = client.execute(get);
                                HttpEntity resEntityGet = responseGet.getEntity();
                                String response2 = EntityUtils.toString(resEntityGet);

                                JSONObject json = new JSONObject(response2);
                                editor.putInt("listener_pk", json.getInt("pk"));
                                editor.putString("listener_owner_of", json.getString("owner_of"));
                                JSONObject jsonUser = json.getJSONObject("user");
                                editor.putInt("user_pk", jsonUser.getInt("pk"));
                                editor.putString("listener_username", jsonUser.getString("username"));
                                //editor.putString("listener_email", jsonUser.getString("email"));
                                editor.commit();

                                return response;
                            }
                            return response;
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.d("Login", msg);
                    if (msg.contains("non_field_errors")) {
                        TextView loginErrorText = (TextView) activity.findViewById(R.id.loginErrorText);
                        try {
                            JSONObject json = new JSONObject(msg);
                            String loginError = json.getJSONArray("non_field_errors").getString(0);
                            Log.d("Login", "Error: " + loginError);
                            loginErrorText.setText(loginError);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loginErrorText.setText("Unknown Error Occurred");
                        }
                        //loginErrorText.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    } else if (msg.contains("token")) {
                        Fragment fragment = new HomeFragment();
                        FragmentManager fragmentManager = activity.getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else {
                        TextView loginErrorText = (TextView) activity.findViewById(R.id.loginErrorText);
                        loginErrorText.setText("Could not connect to server.");
                    }
                }
            }.execute(be, null, null);
        }
    }

    public static void activateJoinGroup(BackendRequest be) {
        if (be.getMethod().equalsIgnoreCase("PUT")) {
            final BackendRequest be2 = be;
            new AsyncTask<BackendRequest, Void, String>() {
                @Override
                protected String doInBackground(BackendRequest... params) {
                    try {
                        BackendRequest be = params[0];
                        HttpClient client = new DefaultHttpClient();
                        HttpPut put = new HttpPut(BASE_URL + be.getUrl());
                        SharedPreferences settings = be.getMainActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
                        String token = settings.getString("token", null);
                        put.addHeader("Authorization", "Token " + token);
                        if (be.getJsonEntity() != null) {
                            put.setEntity(new StringEntity(be.getJsonEntity()));
                        }
                        Log.d("Group", put.getURI().toString());
                        HttpResponse responseGet = client.execute(put);
                        HttpEntity resEntityGet = responseGet.getEntity();
                        if (resEntityGet != null) {
                            //do something with the response
                            return EntityUtils.toString(resEntityGet);
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    return "{\"error\":\"error\"}";
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.d("Group", "" + msg);
                    Activity activity = be2.getMainActivity();
                    JSONObject json;
                    try {
                        json = new JSONObject(msg);
                        SharedPreferences groupSettings = activity.getSharedPreferences(GroupActivity.PREFS_NAME, 0);
                        SharedPreferences.Editor editor = groupSettings.edit();
                        editor.putInt("group_pk", json.getInt("pk"));
                        JSONObject ownerUserJson = json.getJSONObject("owner").getJSONObject("user");
                        String ownerUsername = ownerUserJson.getString("username");
                        editor.putString("group_owner_pk", ownerUsername);
                        editor.putString("group_owner_username", ownerUsername);
                        editor.commit();
                        Log.d("Group", "Group: " + ownerUsername);
                        if (be2.getUrl().contains("join")) {
                            Boolean leader = false;
                            Intent groupIntent = new Intent(activity, GroupActivity.class);
                            Log.wtf("PuttingIntExtra", "" + leader);
                            groupIntent.putExtra("extra_stuff", new String[]{"" + leader, "" + leader});
                            activity.startActivity(groupIntent);
                        } else {
                            ((TextView) activity.findViewById(R.id.groupIdText)).setText(ownerUsername);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute(be, null, null);
        }
    }
}
