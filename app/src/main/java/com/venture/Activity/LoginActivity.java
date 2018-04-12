package com.venture.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.venture.R;
import com.venture.Utilites.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText userEmail, password;
    private Button signIn;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        token = preferences.getString("token", "");

        init();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInRequest();
            }
        });
    }

    private void init() {
        userEmail = findViewById(R.id.userEmail);
        password = findViewById(R.id.passWord);
        signIn = findViewById(R.id.signIn);
    }

    private void signInRequest() {

        JSONObject jsonObject = makeJSONObjectFromInput();

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        String requestURL = Constants.BASE_URL + "/api/auth";

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                requestURL, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.e(TAG, response.toString());
                try {
                    boolean status = response.getBoolean("status");

                    if (status) {

                        JSONObject data = response.getJSONObject("data");

                        String token = data.getString("token");

                        Log.e(TAG, token);

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", token);
                        editor.apply();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();

                startActivity(new Intent(LoginActivity.this, CalendarActivity.class));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
                Log.e(TAG, "" + error);
            }
        }) /*{
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<String, String>();
                // add headers <key,value>
                String credentials = userEmail.getText().toString() + ":" + password.getText().toString();

                Log.e(TAG, credentials);

                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                Log.e(TAG, auth);

                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                setRetryPolicy(new DefaultRetryPolicy(
                        5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                return super.getRetryPolicy();
            }
        }*/;

        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject makeJSONObjectFromInput() {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("email", userEmail.getText().toString());
            jsonObject.put("password", password.getText().toString());

            Log.e(TAG, jsonObject.toString());

        } catch (Exception e) {

        }
        return jsonObject;
    }
}
