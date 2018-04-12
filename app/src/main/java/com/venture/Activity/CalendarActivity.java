package com.venture.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.venture.R;
import com.venture.Utilites.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private ProgressDialog progressDialog;
    private static final String TAG = CalendarActivity.class.getSimpleName();
    private String token = null;
    private ListView meetingListView;
    //private ArrayList meetingList = new ArrayList<String>();
    private CalendarView simpleCalendarView;
    private TextView selectedDate;
    private ArrayAdapter arrayAdapter;
    Map<String, ArrayList<String>> map = new HashMap<>();
    private boolean isMeeting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CalendarActivity.this);
        token = preferences.getString("token", "");

        Log.e(TAG, token);

        meetingDateRequest();

        init();

        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                long selectedDateMS = simpleCalendarView.getDate();
                Log.e(TAG, "" + selectedDateMS + " year " + i + " mont " + i1 + " day " + i2);
                int mon = i1 + 1;
                selectedDate.setText(i2 + "/" + mon + "/" + i);

                String timeString = "" + selectedDateMS;

                isMeeting = false;
                for (String key : map.keySet()) {
                    if (key.equals(timeString)) {

                        isMeeting = true;
                        ArrayList arrayList = map.get(timeString);

                        arrayAdapter = new ArrayAdapter(CalendarActivity.this, android.R.layout.simple_list_item_1, arrayList);
                        meetingListView.setAdapter(arrayAdapter);
                    }
                }
                if (!isMeeting) {

                    ArrayList arrayList = new ArrayList();

                    arrayAdapter = new ArrayAdapter(CalendarActivity.this, android.R.layout.simple_list_item_1, arrayList);
                    meetingListView.setAdapter(arrayAdapter);
                }
            }
        });
    }

    private void init() {

        meetingListView = findViewById(R.id.meetingListView);
        simpleCalendarView = findViewById(R.id.simpleCalendarView);
        selectedDate = findViewById(R.id.selectedDate);
        //arrayAdapter = new ArrayAdapter(CalendarActivity.this, android.R.layout.simple_list_item_1, meetingList);
        // meetingListView.setAdapter(arrayAdapter);

    }

    private void meetingDateRequest() {

        RequestQueue requestQueue = Volley.newRequestQueue(CalendarActivity.this);

        String requestURL = Constants.BASE_URL + "/api/meetings?page=1&limit=20&year=2018&month=4";

        progressDialog = new ProgressDialog(CalendarActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                requestURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.e(TAG, response.toString());
                try {
                    boolean status = response.getBoolean("status");

                    String message = response.getString("message");

                    Log.e(TAG, message);
                    if (status) {

                        JSONArray data = response.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {

                            JSONObject arrayItem = data.getJSONObject(i);

                            String date = arrayItem.getString("date");
                            Log.e(TAG, date);
                            JSONArray meetings = arrayItem.getJSONArray("meetings");

                            long timeInMilliseconds = 0;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date mDate = sdf.parse(date);
                                timeInMilliseconds = mDate.getTime();
                                Log.e(TAG, "Date in milli : " + timeInMilliseconds);
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            ArrayList meetingList = new ArrayList<String>();

                            Map<String, ArrayList<String>> meetingMap = new HashMap<>();

                            for (int j = 0; j < meetings.length(); j++) {
                                JSONObject meetingsItem = meetings.getJSONObject(j);

                                String meeting_title = meetingsItem.getString("title");
                                Log.e(TAG, meeting_title);
                                meetingList.add(meeting_title);

                                int id = meetingsItem.getInt("id");

                                //meetingMap.put(""+id, );
                            }
                            map.put("" + timeInMilliseconds, meetingList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
                Log.e(TAG, "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<String, String>();

                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
