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

import java.util.Map;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private Map<String, Course> courses;

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

                    Course course = (Course) courses.values().toArray()[getBindingAdapterPosition()];

                    course.setCourseSubject(subject);
                    course.setCourseIcon(AppCompatResources.getDrawable(context, Utility.getSubjectDrawable(context, subject)));

                    notifyItemChanged(getBindingAdapterPosition());

                    DrawableCompat.setTint(DrawableCompat.wrap(flCourseIcon.getBackground()), Utility.getSubjectColor(context, subject));

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

        this.courses = courses;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View course = LayoutInflater.from(context).inflate(R.layout.row_course, parent, false);

        return new CourseHolder(course);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CourseHolder courseHolder = (CourseHolder) holder;
        Course course = (Course) courses.values().toArray()[position];

        courseHolder.tvCoursePlatform.setText(course.getCoursePlatform());

        courseHolder.tvCourseName.setText(course.getCourseName());
        courseHolder.sCourseSubject.setSelection(Utility.getSubjectPositionFromTitle(context, course.getCourseSubject()));
        courseHolder.ivCourseIcon.setImageDrawable(course.getCourseIcon());
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}