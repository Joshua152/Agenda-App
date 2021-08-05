/**
 * Defines the methods for the Google Classroom method.
 *
 * ASSUMES: LifecycleOwner is a Fragment
 */

package com.example.agendaapp.Platform;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.R;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleClassroom extends Platform {

    public static final String GOOGLE_CLASSROOM = "Google Classroom";

    public static final String SHARED_PREFS_KEY = "Google Classroom Shared Preferences Key";

    private Activity activity;
    private Context context;

    private SharedPreferences preferences;

    private ActivityResultLauncher<Intent> launcher;

    private RequestQueue queue;
    private JsonObjectRequest photoRequest;

    /**
     * Constructor, pass in the activity or fragment's parent activity
     * @param activity The activity or fragment's parent activity
     */
    public GoogleClassroom(Activity activity) {
        super(ResourcesCompat.getDrawable(activity.getBaseContext().getResources(), R.drawable.ic_google_classroom_32dp, null),
                activity.getBaseContext().getString(R.string.google_classroom),
                Utility.getViewFromXML(activity, R.layout.button_google_classroom));

        this.activity = activity;
        this.context = activity.getBaseContext();

        preferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        launcher = null;

        queue = Volley.newRequestQueue(activity.getApplicationContext());
        photoRequest = null;

        initRequests();
    }

    @Override
    public void onCreate(LifecycleOwner owner) {
        ActivityResultRegistry registry = ((Fragment) owner).requireActivity().getActivityResultRegistry();

        launcher = registry.register("Google Classroom" + ID, owner,
            new ActivityResultContracts.StartActivityForResult(),
            (uri) -> {
                if(uri.getResultCode() == Activity.RESULT_OK)
                    getAuthToken(uri.getData());
            });
    }

    /*
     * Inits http requests
     */
    public void initRequests() {
        photoRequest = new JsonObjectRequest(Request.Method.GET,
                "https://people.googleapis.com/v1/people/me?personFields=photos",
                null,
                // response is a JSONObject
                response -> {
                    try {
                        String photoURL = response.getJSONArray("photos").getJSONObject(0).getString("url");

                        setAccountIconURL(photoURL);

                        callSignInListeners();
                    } catch(JSONException e) {
                        Log.e("IMPORT ERROR", e.toString());
                        Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 401 error = expired token; call AccountManager.invalidateAuthToken() and get auth again
                    // 403 error = missing API key (make sure to override geHeaders and add in auth token)
                    try {
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(htmlBodyBytes), error);

                        try {
                            JSONObject json = new JSONObject(new String(htmlBodyBytes));
                            int errorCode = json.getJSONObject("error").getInt("code");

                            switch(errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Snackbar.make(activity.findViewById(R.id.import_ll_recycler),
                                            context.getString(R.string.logged_out), Snackbar.LENGTH_LONG).show();

                                    break;
                            }
                        } catch(JSONException e) {
                            Log.e("IMPORT ERROR", "Could not parse error JSON");
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                  //  Toast.makeText(context, R.string.import_info_error, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + authToken);

                return params;
            }
        };
    }

    /**
     * Makes the call to AccountManager.getAuthToken()
     * @param data Intent data for the account
     */
    public void getAuthToken(Intent data) {
        AccountManager manager = AccountManager.get(context);
        Bundle options = new Bundle();

        manager.getAuthToken(
                new Account(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)),
                "oauth2:https://www.googleapis.com/auth/userinfo.profile " +
                        "https://www.googleapis.com/auth/classroom.courses.readonly " +
                        "https://www.googleapis.com/auth/classroom.coursework.me.readonly ",
                options,
                activity,
                // on token acquired
                // AccountManagerCallback<Bundle>
                (AccountManagerFuture<Bundle> result) -> {
                    try {
                        Bundle bundle = result.getResult();

                        // insufficient credentials
                        Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);

                        if(launch != null)
                            launcher.launch(launch);

                        authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                        queue.add(photoRequest);
                    } catch(AuthenticatorException | IOException | OperationCanceledException e) {
                        System.err.println(e.toString());
                        Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();
                    }
                },
                new Handler(Looper.myLooper(), (msg) -> {
                    Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();

                    return false;
                })
        );
    }

    @Override
    public void checkAuthTokenValid() {
        if(authToken.equals(""))
            return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://people.googleapis.com/v1/people/me?personFields=photos",
                null,
                null,
                error -> {
                    try {
                        byte[] b = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(b), error);

                        try {
                            JSONObject json = new JSONObject(new String(b));
                            int errorCode = json.getJSONObject("error").getInt("code");

                            switch(errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Snackbar.make(activity.findViewById(R.id.import_ll_recycler),
                                            context.getString(R.string.logged_out), Snackbar.LENGTH_LONG).show();

                                    break;
                            }
                        } catch(JSONException e) {
                            Log.e("IMPORT ERROR", "Could not parse error JSON");
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + authToken);

                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void signInWithPrevAuth() {
        if(!authToken.equals(""))
            queue.add(photoRequest);
    }

    @Override
    public void getNewAssignments(AssignmentReceivedListener listener) {
        if(authToken.equals(""))
            return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://classroom.googleapis.com/v1/courses?studentId=me&courseStates=ACTIVE&pageSize=0",
                null,
                // response: JSONObject
                response -> {
                    try {
                        List<Assignment> newAssignments = Collections.synchronizedList(new ArrayList<Assignment>());

                        JSONArray courses = response.getJSONArray("courses");

                        AtomicInteger numDone = new AtomicInteger(0);

                        for(int i = 0; i < courses.length(); i++) {
                            JSONObject o = courses.getJSONObject(i);

                            handleAssignmentsForCourse(o.getString("id"), new ArrayList<Assignment>(), (assignments -> {
                                for(Assignment a : assignments)
                                    a.setSubject("Other");

                                newAssignments.addAll(assignments);

                                numDone.getAndIncrement();

                                if(numDone.get() == courses.length())
                                    listener.onAssignmentReceived(newAssignments);
                            }));
                        }
                    } catch(JSONException e) {
                        Log.e("IMPORT ERROR", "Unable to parse JSON");
                    }
                },
                error -> {
                    try {
                        byte[] b = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(b), error);

                        try {
                            JSONObject o = new JSONObject(new String(b));
                            int errorCode = o.getJSONObject("error").getInt("code");

                            switch (errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Snackbar.make(activity.findViewById(android.R.id.content),
                                            context.getString(R.string.logged_out), Snackbar.LENGTH_LONG).show();

                                    break;
                            }
                        } catch (JSONException e) {
                            Log.e("IMPORT ERROR", "Could not parse error JSON");
                        }
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + authToken);

                return params;
            }
        };

        queue.add(request);
    }

    // TOOO: FOR LAST IMPORTED MILLIS USE SHARED PREF SO IT'S NOT EXCLUSIVE TO ONE PLATFORM INSTANCE (<PLATFORM><COURSE ID>:<LAST IMPORTED MILLIS>)

    /**
     * Handles adding the assignments for a given course to the list. **The subject will not be set
     * @param courseId The course to get the assignments from
     * @param assignments The list to add the course's assignments to
     */
    private void handleAssignmentsForCourse(String courseId, List<Assignment> assignments, AssignmentReceivedListener listener) {
        JsonObjectRequest request =  new JsonObjectRequest(Request.Method.GET,
                "https://classroom.googleapis.com/v1/courses/" + courseId + "/courseWork?pageSize=0&orderBy=updateTime desc",
                null,
                // response: JSONObject
                response -> {
                    try {
                        JSONArray courseWork = response.getJSONArray("courseWork");

                        long lastUpdateMillis = preferences.getLong(courseId, 0);

                        for(int i = 0; i < courseWork.length(); i++) {
                            JSONObject o = courseWork.getJSONObject(i);

                            JSONObject date = o.optJSONObject("dueDate");
                            DateInfo dateInfo = DateUtils.getNoneInstance(context);

                            if(date != null) {
                                dateInfo = DateUtils.getLocalDateFormat(context, date.getInt("day"),
                                        date.getInt("month"), date.getInt("year"));
                            }

                            if((lastUpdateMillis == 0 && DateUtils.compareDates(dateInfo, DateUtils.getDay(context, 0)) == DateInfo.FURTHER)
                                    || (lastUpdateMillis != 0 && DateTime.parseRfc3339(o.getString("updateTime")).getValue() > lastUpdateMillis)) {

                                Assignment a = new Assignment();

                                a.setId(GOOGLE_CLASSROOM + "|" + courseId + "|" + o.getString("id"));
                                a.setTitle(o.getString("title"));
                                a.setDescription(o.optString("description"));
                                a.setDateInfo(dateInfo);

                                assignments.add(a);
                            }
                        }

                        setUpdateMillis(courseId, preferences.edit());

                        listener.onAssignmentReceived(assignments);
                    } catch(JSONException e) {
                        Log.e("IMPORT ERROR", e.toString());
                    }
                },
                error -> {
                    try {
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(htmlBodyBytes), error);

                        try {
                            JSONObject json = new JSONObject(new String(htmlBodyBytes));
                            int errorCode = json.getJSONObject("error").getInt("code");

                            switch(errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Snackbar.make(activity.findViewById(android.R.id.content),
                                            context.getString(R.string.logged_out), Snackbar.LENGTH_LONG).show();

                                    break;
                            }
                        } catch(JSONException e) {
                            Log.e("IMPORT ERROR", "Could not parse error JSON");
                        }
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + authToken);

                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onClickSignIn() {
        Intent signInIntent = AccountManager.newChooseAccountIntent(
                null,
                null,
                new String[] {"com.google"},
                false,
                null,
                null,
                null,
                null
        );

        launcher.launch(signInIntent);
    }

    @Override
    public void onClickSignOut() {
        AccountManager.get(context).invalidateAuthToken("com.google", authToken);

        clearData();
    }
}
