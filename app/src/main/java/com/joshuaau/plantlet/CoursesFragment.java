/**
 * Fragment to view all the classes
 */

package com.joshuaau.plantlet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.RecyclerAdapters.CoursesRecyclerAdapter;
import com.joshuaau.plantlet.Utils.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CoursesFragment extends Fragment {

    // TODO: ADD TEXT FOR 'NO COURSES'

    public static final String ERROR_NO_CONNECTION = "Error No Connection";

    private Context context;

    private Activity activity;

    private RecyclerView recyclerView;
    private CoursesRecyclerAdapter recyclerAdapter;

    private TextView tvNone;

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

        tvNone = (TextView) view.findViewById(R.id.courses_tv_none);

        if(courseMap == null)
            courseMap = new HashMap<String, Course>(); // set equal to course list in prefs

        initRecyclerAdapter();
    }

    /**
     * Inits the recycler view adapter
     */
    private void initRecyclerAdapter() {
        updateCourseMap(context, (courseMap, error) -> {
            if(courseMap != null) {
                CoursesFragment.courseMap = courseMap;
            }

            if(error != null) {
                switch(error) {
                    case ERROR_NO_CONNECTION :
                        Utility.showBasicSnackbar(activity, R.string.error_no_connection);
                }
            }

            if(CoursesFragment.courseMap.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                tvNone.setVisibility(View.VISIBLE);
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
    public static void updateCourseMap(Context context, CoursesProcessedListener listener) {
        if(!Utility.isNetworkAvailable(context)) {
            listener.onCoursesProcessed(null, ERROR_NO_CONNECTION);

            return;
        }

        List<Platform> platforms = ImportFragment.getSignedInPlatforms();

        if(platforms.size() == 0) {
            listener.onCoursesProcessed(new HashMap<>(), null);

            return;
        }

        AtomicInteger i = new AtomicInteger(0);

        Map<String, Course> fullCourseMap = new HashMap<String, Course>();

        for(Platform p : platforms) {
            p.getCourses((courseMap, withExclusions) -> {
                fullCourseMap.putAll(withExclusions);

                processCourses(withExclusions);

                if(i.incrementAndGet() >= platforms.size())
                    listener.onCoursesProcessed(fullCourseMap, null);
            });
        }
    }

    /**
     * Adds the given courses to the map **CALL METHOD IN THE PLATFORM'S {@link com.joshuaau.plantlet.Data.Platform#getCourses(Platform.CoursesReceivedListener)} METHOD
     * @param courseMap The courses to add
     */
    public static void processCourses(Map<String, Course> courseMap) {
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
    }

    /**
     * Deserializes the course map
     * @param context The context
     * @return Returns the serialized course map
     */
    public static Map<String, Course> getSavedCourseList(Context context) {
        Map<String, Course> map = Utility.deserializeCourses(context);

        if(map != null) {
            for(Map.Entry<String, Course> e : map.entrySet()) {
                Course c = e.getValue();

                c.setCourseIconId(Utility.getSubjectDrawableId(context, c.getCourseSubject()));
            }

            return map;
        }

        return new TreeMap<String, Course>();
    }

    @Override
    public void onPause() {
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
