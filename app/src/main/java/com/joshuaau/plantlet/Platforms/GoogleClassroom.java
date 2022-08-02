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

package com.joshuaau.plantlet.Platforms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
import com.joshuaau.plantlet.CoursesFragment;
import com.joshuaau.plantlet.Data.Assignment;
import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.Data.DateInfo;
import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.MainActivity;
import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.DateUtils;
import com.joshuaau.plantlet.Utils.OAuthHelper;
import com.joshuaau.plantlet.Utils.Utility;
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

import timber.log.Timber;

public class GoogleClassroom extends Platform {

    // TODO: fix repeated import error toast

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

        oAuthHelper = new OAuthHelper(activity, ID,
                "https://accounts.google.com/.well-known/openid-configuration",
                "https://www.googleapis.com/auth/userinfo.profile " +
                        "https://www.googleapis.com/auth/classroom.courses.readonly " +
                        "https://www.googleapis.com/auth/classroom.coursework.me.readonly",
                () -> MainActivity.homeFragment.updateAssignments(this, () -> {}));
    }

    @Override
    public void onCreate(LifecycleOwner owner) {
        oAuthHelper.setRegistry(((Fragment) owner).requireActivity().getActivityResultRegistry());
        oAuthHelper.setLifecycleOwner(owner);
    }

    @Override
    public void getCourses(CoursesReceivedListener listener) {
        if(!getSignedIn() || !Utility.isNetworkAvailable(context))
            return;

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

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
                        Timber.e(e, "Unable to parse JSON");
                    }
                },
                error -> {
                    try {
                        byte[] b = error.networkResponse.data;
                        Timber.e(error, new String(b));

                        if(retryPolicy.getCurrentRetryCount() - 1 != DefaultRetryPolicy.DEFAULT_MAX_RETRIES)
                            return;

                        listener.onCoursesReceived(new HashMap<>());

                        try {
                            JSONObject o = new JSONObject(new String(b));
                            int errorCode = o.getJSONObject("error").getInt("code");

                            switch (errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Utility.showBasicSnackbar(activity, R.string.logged_out);

                                    break;
                                case 403 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Utility.showBasicSnackbar(activity, R.string.gc_not_allowed_error);

                                    break;
                            }
                        } catch (JSONException e) {
                            Timber.e(e, "Could not parse error JSON");
                        }
                    } catch(NullPointerException e) {
                        Utility.showBasicSnackbar(activity, R.string.import_error);

                        listener.onCoursesReceived(new HashMap<String, Course>()); //TODO

                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                //TODO: CALL PERFORMACTIONWITHFRESHTOKENS AND THEN INSIDE OF THAT GET THE ACCESS TOKEN?

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + oAuthHelper.getAuthState().getAccessToken());

                return params;
            }
        };

        request.setRetryPolicy(retryPolicy);

        if(oAuthHelper.getAuthState().getNeedsTokenRefresh()) {
            updateAndCheckAuthState(activity, authState -> {
                queue.add(request);
            });
        } else {
            queue.add(request);
        }
    }

    @Override
    public void getNewAssignments(AssignmentReceivedListener listener) {
        if(!getSignedIn() || !Utility.isNetworkAvailable(context)) {
            listener.onAssignmentReceived(new ArrayList<>());

            return;
        }

        getCourses(courses ->  {
            if(courses == null) {
                listener.onAssignmentReceived(new ArrayList<>());

                return;
            }

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
        if(!getSignedIn() || !Utility.isNetworkAvailable(context)) {
            listener.onAssignmentReceived(new ArrayList<>());

            return;
        }

        List<Assignment> assignments = new ArrayList<Assignment>();

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

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
                        Timber.e(e, "Unable to get Google Classroom courses");
                    }
                },
                error -> {
                    try {
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Timber.e(error, new String(htmlBodyBytes));

                        if(retryPolicy.getCurrentRetryCount() - 1 != DefaultRetryPolicy.DEFAULT_MAX_RETRIES)
                            return;

                        try {
                            JSONObject json = new JSONObject(new String(htmlBodyBytes));
                            int errorCode = json.getJSONObject("error").getInt("code");

                            switch(errorCode) {
                                case 401 :
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Utility.showBasicSnackbar(activity, R.string.logged_out);

                                    break;
                            }
                        } catch(JSONException e) {
                            Timber.e(e, "Could not parse error JSON");
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

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + oAuthHelper.getAuthState().getAccessToken());

                return params;
            }
        };

        request.setRetryPolicy(retryPolicy);

        if(oAuthHelper.getAuthState().getNeedsTokenRefresh()) {
            updateAndCheckAuthState(activity, authState -> {
                queue.add(request);
            });
        } else {
            queue.add(request);
        }
    }

    @Override
    public AuthState readAuthState() {
        return oAuthHelper.getAuthState();
    }

    @Override
    public void onClickSignIn() {
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        oAuthHelper.launchOAuth(authState -> {
            JsonObjectRequest photoRequest = new JsonObjectRequest(Request.Method.GET,
                    "https://people.googleapis.com/v1/people/me?personFields=photos",
                    null,
                    // response is a JSONObject
                    response -> {
                        try {
                            setSignedIn(true);

                            String photoURL = response.getJSONArray("photos").getJSONObject(0).getString("url");

                            setAccountIconURL(photoURL);

                            callSignInListeners();
                        } catch(JSONException e) {
                            Timber.e(e, "Error handling photo response");

                            Utility.showBasicSnackbar(activity, R.string.import_error);
                        }
                    },
                    error -> {
                        // 401 error = expired token; call AccountManager.invalidateAuthToken() and get auth again
                        // 403 error = missing API key (make sure to override geHeaders and add in auth token)
                        try {
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Timber.e(error, new String(htmlBodyBytes));

                            if(retryPolicy.getCurrentRetryCount() - 1 != DefaultRetryPolicy.DEFAULT_MAX_RETRIES)
                                return;

                            try {
                                JSONObject json = new JSONObject(new String(htmlBodyBytes));
                                int errorCode = json.getJSONObject("error").getInt("code");

                                switch(errorCode) {
                                    case 401 :
                                        onClickSignOut();
                                        callSignOutRequestListeners();

                                        Utility.showBasicSnackbar(activity, R.string.logged_out);

                                        break;
                                }
                            } catch(JSONException e) {
                                Timber.e(e, "Could not parse error JSON");
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
                    params.put("authorization", "Bearer " + authState.getAccessToken());

                    return params;
                }
            };

            photoRequest.setRetryPolicy(retryPolicy);

            if(oAuthHelper.getAuthState().getNeedsTokenRefresh()) {
                updateAndCheckAuthState(activity, authState1 -> {
                    queue.add(photoRequest);
                });
            } else {
                queue.add(photoRequest);
            }
        });
    }

    @Override
    public void onClickSignOut() {
        signOut();
    }
}
