/**
 * Defines the methods for the Google Classroom method.
 *
 * ASSUMES: LifecycleOwner is a Fragment
 *
 * Discovery Doc: https://accounts.google.com/.well-known/openid-configuration
 * Scopes: "https://www.googleapis.com/auth/userinfo.profile " +
 *        "https://www.googleapis.com/auth/classroom.courses.readonly " +
 *        "https://www.googleapis.com/auth/classroom.coursework.me.readonly"
 */

package com.example.agendaapp.Platforms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.agendaapp.CoursesFragment;
import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.Course;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.MainActivity;
import com.example.agendaapp.R;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.OAuthHelper;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.util.DateTime;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private SharedPreferences preferences; // todo: do all instances use the same file to set refresh time?

    private RequestQueue queue;

    /**
     * Constructor, pass in the activity or fragment's parent activity
     * @param id The id to use; if you want to use the auto generated one, pass in Platform.NO_ID
     * @param activity The activity or fragment's parent activity
     */
    public GoogleClassroom(String id, Activity activity) {
        super(ResourcesCompat.getDrawable(activity.getBaseContext().getResources(), R.drawable.ic_google_classroom_32dp, null),
                activity.getBaseContext().getString(R.string.google_classroom),
                Utility.getViewFromXML(activity, R.layout.button_google_classroom));

        ID = id.equals(AUTO_ID) ? getID() : id;

        this.activity = activity;
        this.context = activity.getBaseContext();

        preferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        queue = Volley.newRequestQueue(activity.getApplicationContext());

        System.out.println("GC CONSTRUCTOR");

        oAuthHelper = new OAuthHelper(context, ID,
                "https://accounts.google.com/.well-known/openid-configuration",
                "https://www.googleapis.com/auth/userinfo.profile " +
                        "https://www.googleapis.com/auth/classroom.courses.readonly " +
                        "https://www.googleapis.com/auth/classroom.coursework.me.readonly",
                () -> MainActivity.homeFragment.updateAssignments(this));
    }

    @Override
    public void onCreate(LifecycleOwner owner) {
        oAuthHelper.setRegistry(((Fragment) owner).requireActivity().getActivityResultRegistry());
        oAuthHelper.setLifecycleOwner(owner);

//        launcher = registry.register("Google Classroom" + ID, owner,
//            new ActivityResultContracts.StartActivityForResult(),
//            (uri) -> {
//                if(uri.getResultCode() == Activity.RESULT_OK)
//                    getAuthToken(uri.getData());
//            });
    }

//    @Override
//    public void checkAuthTokenValid() {
//        if(authToken.equals("") || !Utility.isNetworkAvailable(context))
//            return;
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
//                "https://people.googleapis.com/v1/people/me?personFields=photos",
//                null,
//                null,
//                error -> {
//                    try {
//                        byte[] b = error.networkResponse.data;
//                        Log.e("IMPORT ERROR", new String(b), error);
//
//                        try {
//                            JSONObject json = new JSONObject(new String(b));
//                            int errorCode = json.getJSONObject("error").getInt("code");
//
//                            switch(errorCode) {
//                                case 401 :
//                                    onClickSignOut();
//                                    callSignOutRequestListeners();
//
//                                    Snackbar.make(activity.findViewById(android.R.id.content),
//                                            context.getString(R.string.logged_out), Snackbar.LENGTH_LONG).show();
//
//                                    break;
//                            }
//                        } catch(JSONException e) {
//                            Log.e("IMPORT ERROR", "Could not parse error JSON");
//                        }
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//
//                params.put("Content-Type", "application/json");
//                params.put("authorization", "Bearer " + authToken);
//
//                return params;
//            }
//        };
//
//        request.setRetryPolicy(new DefaultRetryPolicy(5000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        queue.add(request);
//    }

//    @Override
//    public void configWithPrevAuth() {
//        if(!authToken.equals(""))
//            queue.add(photoRequest);
//    }

    @Override
    public void getCourses(CoursesReceivedListener listener) {
        if(!getSignedIn() || !Utility.isNetworkAvailable(context))
            return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://classroom.googleapis.com/v1/courses?studentId=me&courseStates=ACTIVE&pageSize=0",
                null,
                // response: JSONObject
                response -> {
                    try {
                        JSONArray courses = response.getJSONArray("courses");

                        Map<String, Course> map = new HashMap<String, Course>();

                        String other = Utility.getSubject(context, Utility.POSITION_OTHER);

                        for(int i = 0; i < courses.length(); i++) {
                            JSONObject o = courses.getJSONObject(i);

                            String courseId = Course.generateCourseId(GOOGLE_CLASSROOM, o.getString("id"));

                            map.put(courseId, new Course(
                                    courseId,
                                    GOOGLE_CLASSROOM,
                                    o.getString("name"),
                                    other,
                                    AppCompatResources.getDrawable(context, Utility.getSubjectDrawable(context, other))));
                        }

                        CoursesFragment.processCourses(map);

                        listener.onCoursesReceived(map);
                    } catch(JSONException e) {
                        Log.e("IMPORT ERROR", "Unable to parse JSON");
                    }
                },
                error -> {
                    try {
                        byte[] b = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(b), error);

                        System.out.println("error right here");

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
                                case 403 :
                                    Snackbar.make(activity.findViewById(android.R.id.content),
                                            context.getString(R.string.gc_not_allowed_error), Snackbar.LENGTH_LONG).show();

                                    break;
                            }
                        } catch (JSONException e) {
                            Log.e("IMPORT ERROR", "Could not parse error JSON: " + e);
                        }
                    } catch(NullPointerException e) {
                        Snackbar.make(activity.findViewById(android.R.id.content), R.string.import_error, Snackbar.LENGTH_LONG).show();

                        listener.onCoursesReceived(new HashMap<String, Course>()); //TODO

                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + oAuthHelper.getAuthState().getAccessToken());

                return params;
            }
        };

        if(oAuthHelper.getAuthState().getNeedsTokenRefresh())
            updateAndCheckAuthState(context);

        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    @Override
    public void getNewAssignments(AssignmentReceivedListener listener) {
        if(!getSignedIn() || !Utility.isNetworkAvailable(context))
            return;

        getCourses(courses ->  {
            if(courses == null)
                return;

            List<Assignment> newAssignments = Collections.synchronizedList(new ArrayList<Assignment>());

            AtomicInteger numDone = new AtomicInteger(0);

            courses.entrySet().stream().forEach(e -> {
                String courseId = e.getKey();

                handleAssignmentsForCourse(courseId, assignments -> {
                    for(Assignment a : assignments) {
                        a.setSubject(CoursesFragment.courseMap.get(courseId).getCourseSubject());
                    }

                    newAssignments.addAll(assignments);

                    numDone.getAndIncrement();

                    if(numDone.get() == courses.size())
                        listener.onAssignmentReceived(newAssignments);
                });
            });
        });
    }

    /**
     * Handles adding the assignments for a given course to the list. **The subject will not be set**
     * @param courseId The course to get the assignments from
     */
    private void handleAssignmentsForCourse(String courseId, AssignmentReceivedListener listener) {
        if(!getSignedIn() || !Utility.isNetworkAvailable(context))
            return;

        List<Assignment> assignments = new ArrayList<Assignment>();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://classroom.googleapis.com/v1/courses/" + courseId.substring(courseId.indexOf("|") + 1) + "/courseWork?pageSize=0&orderBy=updateTime desc",
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

                                a.setCourseId(courseId); // HASN'T THE PLATFORM AND PIPE ALREADY BEEN SET?
                                a.setId(courseId + "|" + o.getString("id"));
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
                                    System.out.println("Sign out and make snackbar: handle assignments for course");

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

                    // TODO: CALL LISTENER WITH EMPTY ASSIGNMENT LIST?
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                System.out.println("right here");

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + oAuthHelper.getAuthState().getAccessToken());

                return params;
            }
        };

        if(oAuthHelper.getAuthState().getNeedsTokenRefresh())
            updateAndCheckAuthState(context);

        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    @Override
    public AuthState readAuthState() {
        return oAuthHelper.getAuthState();
    }

    @Override
    public void onClickSignIn() {
//        Intent signInIntent = AccountManager.newChooseAccountIntent(
//                null,
//                null,
//                new String[] {"com.google"},
//                false,
//                null,
//                null,
//                null,
//                null
//        );
//
//        launcher.launch(signInIntent);

        setSignedIn(true);

        System.out.println("asdfasdfasdfa");

        oAuthHelper.launchOAuth(authState -> {
            JsonObjectRequest photoRequest = new JsonObjectRequest(Request.Method.GET,
                    "https://people.googleapis.com/v1/people/me?personFields=photos",
                    null,
                    // response is a JSONObject
                    response -> {
                        try {
                            String photoURL = response.getJSONArray("photos").getJSONObject(0).getString("url");

                            setAccountIconURL(photoURL);

                            System.out.println("response??");
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

                                        Snackbar.make(activity.findViewById(android.R.id.content),
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
                    params.put("authorization", "Bearer " + authState.getAccessToken());

                    return params;
                }
            };

            if(authState.getNeedsTokenRefresh()) // TODO: MOVE ELSEWHERE -> RETRIEVE AUTH TOKEN MIGHT NOT FINISH IN TIME BEFORE GETACCESSTOKEN()
                updateAndCheckAuthState(context);

            photoRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(photoRequest);
        });
    }

    @Override
    public void onClickSignOut() {
//        AccountManager.get(context).invalidateAuthToken("com.google", authToken);
        signOut();
    }
}
