package com.joshuaau.plantlet.Platforms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.util.DateTime;
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

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

public class GoogleCalendar extends Platform {

    public static final String GOOGLE_CALENDAR = "Google Calendar";
    public static final String SHARED_PREFS_KEY = "Google Calendar Shared Preferences Key";

    private Activity activity;
    private Context context;

    private SharedPreferences preferences;

    private RequestQueue queue;

    public GoogleCalendar(String id, Activity activity) {
        super(R.drawable.ic_google_calendar_32dp,
                activity.getBaseContext().getString(R.string.google_calendar),
                Utility.getViewFromXML(activity, R.layout.button_google),
                true);

        ID = id.equals(AUTO_ID) ? getID() : id;

        this.activity = activity;
        this.context = activity.getBaseContext();

        preferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        queue = Volley.newRequestQueue(activity.getApplicationContext());

        oAuthHelper = new OAuthHelper(activity, ID,
                "https://accounts.google.com/.well-known/openid-configuration",
                "https://www.googleapis.com/auth/userinfo.profile " +
                "https://www.googleapis.com/auth/calendar.readonly",
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

        String url = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
        AtomicReference<String> query = new AtomicReference<>("");

        HashMap<String, Course> courseMap = new HashMap<String, Course>();
        HashMap<String, Course> courseWithExclusions = new HashMap<String, Course>();

        getCalendarPage(new CourseResponseListener() {
            @Override
            public void onCourseResponse(HashMap<String, Course> subCourseMap, String nextPageToken) {
                courseMap.putAll(subCourseMap);

                for(Map.Entry<String, Course> e : subCourseMap.entrySet()) {
                    if(!getExclusions().contains(e.getKey()))
                        courseWithExclusions.put(e.getKey(), e.getValue());
                }

                if(nextPageToken != null) {
                    query.set("?pageToken=" + nextPageToken);

                    getCalendarPage(this, url + query);
                } else {
                    CoursesFragment.processCourses(courseWithExclusions);

                    listener.onCoursesReceived(courseMap, courseWithExclusions);
                }
            }
        }, url);
    }

    private void getCalendarPage(CourseResponseListener listener, String url) {
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    // loop through pages
                    try {
                        String nextPageToken = response.optString("pageToken", null);

                        JSONArray calendars = response.getJSONArray("items");

                        HashMap<String, Course> subCourseMap = new HashMap<String, Course>();

                        for(int i = 0; i < calendars.length(); i++) {
                            JSONObject o = calendars.getJSONObject(i);

                            String courseId = Course.generateCourseId(GOOGLE_CALENDAR, o.getString("id"));

                            String other = Utility.getSubject(context, Utility.POSITION_OTHER);

                            subCourseMap.put(courseId, new Course(
                                    courseId,
                                    GOOGLE_CALENDAR,
                                    o.getString("summary"),
                                    other,
                                    Utility.getSubjectDrawableId(context, other)
                            ));
                        }

                        listener.onCourseResponse(subCourseMap, nextPageToken);
                    } catch(JSONException e) {
                        Timber.e(e, "Unable to parse JSON");
                    }
                },
                error -> {
                    try {
                        byte[] b = error.networkResponse.data;
                        Timber.e(error, new String(b));

                        if (retryPolicy.getCurrentRetryCount() - 1 != DefaultRetryPolicy.DEFAULT_MAX_RETRIES)
                            return;

                        listener.onCourseResponse(new HashMap<String, Course>(), null);

                        try {
                            JSONObject o = new JSONObject(new String(b));
                            int errorCode = o.getJSONObject("error").getInt("code");

                            switch (errorCode) {
                                case 401:
                                    onClickSignOut();
                                    callSignOutRequestListeners();

                                    Utility.showBasicSnackbar(activity, R.string.logged_out);

                                    break;
                                case 403:
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

                        listener.onCourseResponse(new HashMap<String, Course>(), null); //TODO

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

        getCourses((courses, withExclusions) ->  {
            if(withExclusions == null) {
                listener.onAssignmentReceived(new ArrayList<>());

                return;
            }

            List<Assignment> newAssignments = Collections.synchronizedList(new ArrayList<Assignment>());

            AtomicInteger numDone = new AtomicInteger(0);

            withExclusions.entrySet().stream().forEach(e -> {
                String courseId = e.getKey();

                handleAssignmentsForCourse(courseId, assignments -> {
                    for(Assignment a : assignments) {
                        String subject = CoursesFragment.courseMap.get(courseId).getCourseSubject();

                        if(subject == null)
                            subject = Utility.getSubject(context, Utility.POSITION_OTHER);

                        a.setSubject(subject);

                    }

                    newAssignments.addAll(assignments);

                    numDone.getAndIncrement();

                    System.out.println("size check: " + numDone.get() + " " + withExclusions.size());

                    if(numDone.get() == withExclusions.size()) {
                        System.out.println("ON ASSIGNMENTS RECEIVED: " + newAssignments);

                        listener.onAssignmentReceived(newAssignments);
                    }
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

        System.out.println("Course ID: " + courseId);

//        List<Assignment> assignments = new ArrayList<Assignment>\();

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        long lastUpdateMillis = preferences.getLong(courseId, 0);
        String lastUpdateRFC3339 = new SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZZZZZ").format(lastUpdateMillis);
        // change min

        System.out.println("Last Update: " + lastUpdateRFC3339);

        DateUtils.getDay(context, 7);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String timeMax = new SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZZZZZ").format(calendar.getTime());
        System.out.println("Time max: " + timeMax);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://www.googleapis.com/calendar/v3/calendars/" + courseId.substring(courseId.indexOf("|") + 1) + "/events" +
                        "?orderBy=updated&singleEvents=true&timeMin=" + lastUpdateRFC3339 + "&timeMax=" + timeMax,
                null,
                response -> {
                    try {
                        ArrayList<Assignment> assignments = new ArrayList<Assignment>();

                        JSONArray items = response.getJSONArray("items");

                        for(int i = 0; i < items.length(); i++) {
                            JSONObject o = items.getJSONObject(i);

                            // why no dateTime? one of the ones thats like full?
                            System.out.println(o.toString(4));

                            String dueDate = o.getJSONObject("end").optString("date"); // rfc3339 at "dateTime" property
                            if(dueDate.equals(""))
                                dueDate = o.getJSONObject("end").getString("dateTime");

                            int idxFirstDash = dueDate.indexOf("-");
                            int idxSecDash = dueDate.indexOf("-", idxFirstDash + 1);

                            int year = Integer.parseInt(dueDate.substring(0, idxFirstDash));
                            int month = Integer.parseInt(dueDate.substring(idxFirstDash + 1, idxSecDash));
                            int day = Integer.parseInt(dueDate.substring(idxSecDash + 1, idxSecDash + 3));

                            DateInfo dateInfo = DateUtils.getLocalDateFormat(context,
                                    day, month, year);

                            if((lastUpdateMillis == 0 && DateUtils.compareDates(dateInfo, DateUtils.getDay(context, 0)) == DateInfo.FURTHER)
                                    || (lastUpdateMillis != 0 && DateTime.parseRfc3339(o.getString("updated")).getValue() > lastUpdateMillis)) {

                                Assignment a = new Assignment();

                                a.setCourseId(courseId); // HASN'T THE PLATFORM AND PIPE ALREADY BEEN SET?
                                a.setId(courseId + "|" + o.getString("id"));
                                a.setTitle(o.getString("summary"));
                                a.setDescription(o.optString("description"));
                                a.setDateInfo(dateInfo);

                                System.out.println("calendar assignment: " + a);

                                assignments.add(a); // TODO: UNCOMMENT
                            }
                        }

                        setUpdateMillis(courseId, preferences.edit());

                        System.out.println("on assignment received");
                        listener.onAssignmentReceived(assignments);
                    } catch(JSONException e) {
                        Timber.e(e, "Unable to get Google Calendar courses");
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
                            String photoURL = response.getJSONArray("photos").getJSONObject(0).getString("url");

                            Timber.i("Photo URL: %s", photoURL);

                            setAccountIconURL(photoURL);

                            boolean ok = addAccount(context, GOOGLE_CALENDAR + response.getString("resourceName"));
                            if(ok) {
                                setSignedIn(true);
                                callSignInListeners();
                            } else {
                                onClickSignOut();

                                Utility.showBasicSnackbar(activity, R.string.already_signed_in);
                            }
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
        removeAccount(context, accountID);

        signOut();
    }

    private interface CourseResponseListener {
        void onCourseResponse(HashMap<String, Course> courseMap, String nextPageToken);
    }
}
