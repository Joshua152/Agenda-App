/**
 * Fragment to view all the classes
 */

package com.example.agendaapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Course;
import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.RecyclerAdapters.CoursesRecyclerAdapter;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CoursesFragment extends Fragment {

    public static final String ERROR_NO_CONNECTION = "Error No Connection";

    private Context context;

    private Activity activity;

    private RecyclerView recyclerView;
    private CoursesRecyclerAdapter recyclerAdapter;

    public static Map<String, Course> courseMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.courses_toolbar);
        toolbar.setTitle(getString(R.string.courses_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        setHasOptionsMenu(true);

        init(view);

        return view;
    }

    /**
     * Inits the views
     * @param view The inflated fragment layout
     */
    private void init(View view) {
        context = getContext();

        this.activity = getActivity();

        recyclerView = (RecyclerView) view.findViewById(R.id.courses_recycler_view);

        if(courseMap == null)
            courseMap = new TreeMap<String, Course>(); // set equal to course list in prefs

        initRecyclerAdapter();
    }

    /**
     * Inits the recycler view adapter
     */
    private void initRecyclerAdapter() {
        getCourseList(context, (courseMap, error) -> {
            if(courseMap != null)
                CoursesFragment.courseMap = courseMap;

            if(error != null) {
                switch(error) {
                    case ERROR_NO_CONNECTION :
                        Snackbar.make(context, activity.findViewById(android.R.id.content),
                                getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
                                .setAction(R.string.ok, view -> {})
                                .show();
                }
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new CoursesRecyclerAdapter(context, CoursesFragment.courseMap));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction homeTransaction = getParentFragmentManager().beginTransaction();
                homeTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                homeTransaction.replace(R.id.fragment_container, MainActivity.homeFragment);
                homeTransaction.addToBackStack(Utility.HOME_FRAGMENT);
                homeTransaction.commit();

                return true;
        }

        return false;
    }

    /**
     * Gets and updates the course JSON and adds to a List<Course>
     * @param context The context
     * @param listener The listener for when the list has finished being processed
     */
    public static void getCourseList(Context context, CoursesProcessedListener listener) {
        if(!Utility.isNetworkAvailable(context)) {
            listener.onCoursesProcessed(null, ERROR_NO_CONNECTION);

            return;
        }

        Map<String, Course> newCourseMap = new TreeMap<String, Course>();

        List<Platform> platforms = ImportFragment.getSignedInPlatforms();

        AtomicInteger n = new AtomicInteger(0);

        for(int i = 0; i < platforms.size(); i++) {
            Platform p = platforms.get(i);

            p.getCourses(courseMap -> {
                if (courseMap != null) {

                    courseMap.entrySet().stream().forEach(e -> {
                        String courseId = e.getKey();

                        if(CoursesFragment.courseMap.containsKey(courseId)) {
                            Course course = CoursesFragment.courseMap.get(courseId);

                            e.getValue().setCourseSubject(course.getCourseSubject());
                            e.getValue().setCourseIconId(course.getCourseIconId());
                        } else {
                            CoursesFragment.courseMap.put(e.getKey(), e.getValue());
                        }
                    });

//                    for(Course c : courseList) {
//                        String subject = Utility.getSubject(context, Utility.POSITION_OTHER);
//
//                        try {
//                            if(courseList.get != null)
//                                subject = jsonCourse.optString(JSON_SUBJECT_NAME);
//
//                            newCourseList.add(new Course(c.getCourseId(), p.getPlatformName(), c.getCourseName(),
//                                    subject, AppCompatResources.getDrawable(context, Utility.getSubjectDrawable(context, subject))));
//                        } catch(JSONException e) {
//                            Log.e("Error at course JSON", e.toString());
//
////                        Snackbar.make(getActivity().findViewById(android.R.id.content), context.getString(R.string.import_error), Snackbar.LENGTH_SHORT).show();
//                        }
//                    }
                }

                if(n.incrementAndGet() >= platforms.size())
                    listener.onCoursesProcessed(courseMap, null);
            });
        }
    }

    /**
     * Deserializes the course map
     * @param context The context
     * @return Returns the serialized course map
     */
    public static Map<String, Course> getSavedCourseList(Context context) {
        Map<String, Course> map = Utility.deserializeCourses(context);

        if(map != null)
            return map;

        return new TreeMap<String, Course>();
    }

    @Override
    public void onPause() {
        System.out.println("on pause");

        Utility.serializeCourses(context, courseMap);
        Utility.serializeAssignments(context, HomeFragment.priority, HomeFragment.upcoming);

        super.onPause();
    }

    /**
     * An interface for the onCoursesProcessed() method
     */
    public interface CoursesProcessedListener {
        /**
         * Callback method for when the courses have been fully retrieved and processed
         * @param courseMap The map of courses
         * @param error The error if one occurs (String constant)
         */
        public void onCoursesProcessed(Map<String, Course> courseMap, String error);
    }
}
