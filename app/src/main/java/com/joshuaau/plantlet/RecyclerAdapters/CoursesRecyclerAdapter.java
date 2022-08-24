/**
 * Recycler adapter for viewing imported classes
 */

package com.joshuaau.plantlet.RecyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.Assignment;
import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.HomeFragment;
import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private ArrayList<Course> courseList;

    /**
     * Class to represent a single course in the recycler view
     */
    private class CourseHolder extends RecyclerView.ViewHolder {

        private TextView tvCoursePlatform;

        private TextView tvCourseName;

        private FrameLayout flCourseIcon;
        private ImageView ivCourseIcon;

        private Spinner sCourseSubject;

        public CourseHolder(View itemView) {
            super(itemView);

            tvCoursePlatform = (TextView) itemView.findViewById(R.id.course_tv_platform);

            tvCourseName = (TextView) itemView.findViewById(R.id.course_tv_name);

            ivCourseIcon = (ImageView) itemView.findViewById(R.id.course_iv_course);
            flCourseIcon = (FrameLayout) itemView.findViewById(R.id.course_frame_icon);

            sCourseSubject = (AppCompatSpinner) itemView.findViewById(R.id.course_s_subject);

            init();

            initListeners();
        }

        /**
         * Configures the views
         */
        public void init() {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.subject_array, android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            sCourseSubject.setAdapter(adapter);
        }

        /**
         * Inits the listeners (ex. onClick)
         */
        public void initListeners() {
            sCourseSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    String subject = sCourseSubject.getSelectedItem().toString();

                    Course course = courseList.get(getBindingAdapterPosition());

                    course.setCourseSubject(subject);
                    course.setCourseIconId(Utility.getSubjectDrawableId(context, subject));

                    flCourseIcon.setBackground(AppCompatResources.getDrawable(context, R.drawable.circle_drawable));
                    DrawableCompat.setTint(DrawableCompat.wrap(flCourseIcon.getBackground()).mutate(), Utility.getSubjectColor(context, subject));

                    Timber.i("Utility.getSubjectColor(%s) -> %d into %s", subject, Utility.getSubjectColor(context, subject), flCourseIcon.getBackground());

                    notifyItemChanged(getBindingAdapterPosition());

                    // update assignments

                    for(int i = 0; i < HomeFragment.assignmentModerator.getItemCount(); i++) {
                        Assignment a = HomeFragment.assignmentModerator.get(i);

                        if(a.getCourseId().equals(course.getCourseId()))
                            a.setSubject(subject);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }
    }

    public CoursesRecyclerAdapter(Context context, Map<String, Course> courses) {
        this.context = context;

        courseList = new ArrayList<Course>();

        for(Map.Entry<String, Course> e : courses.entrySet())
            courseList.add(e.getValue());

        Collections.sort(courseList, (o1, o2) -> {
            if(o1.getCourseName().compareTo(o2.getCourseName()) > 0)
                return 1;

            if(o1.getCourseName().compareTo(o2.getCourseName()) < 0)
                return -1;

            return 0;
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View course = LayoutInflater.from(context).inflate(R.layout.row_course, parent, false);

        return new CourseHolder(course);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CourseHolder courseHolder = (CourseHolder) holder;
        Course course = courseList.get(position);

        courseHolder.tvCoursePlatform.setText(course.getCoursePlatform());

        courseHolder.tvCourseName.setText(course.getCourseName());
        courseHolder.sCourseSubject.setSelection(Utility.getSubjectPositionFromTitle(context, course.getCourseSubject()));
        courseHolder.ivCourseIcon.setImageResource(course.getCourseIconId());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
}
