package com.joshuaau.plantlet.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class OptionsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Course> calendars;
    private HashSet<String> exclusionSet;

    public class OptionsViewHolder extends RecyclerView.ViewHolder {

        private MaterialCheckBox checkbox;

        public OptionsViewHolder(View itemView) {
            super(itemView);

            checkbox = (MaterialCheckBox) itemView.findViewById(R.id.op_cb_name);

            initListeners();
        }

        private void initListeners() {
            checkbox.setOnCheckedChangeListener((buttonView, checked) -> {
                Course c = calendars.get(getBindingAdapterPosition());

                if(!checked)
                    exclusionSet.add(c.getCourseId());
                else
                    exclusionSet.remove(c.getCourseId());
            });
        }
    }

    public OptionsRecyclerAdapter(HashMap<String, Course> calendarMap, ArrayList<String> exclusions) {
        calendars = new ArrayList<Course>();
        exclusionSet = new HashSet<String>(exclusions);

        for(Map.Entry<String, Course> e : calendarMap.entrySet())
            calendars.add(e.getValue());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_calendar_picker, parent, false);

        return new OptionsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        OptionsViewHolder optionsHolder = (OptionsViewHolder) holder;

        Course c = calendars.get(position);
        optionsHolder.checkbox.setText(c.getCourseName());
        optionsHolder.checkbox.setChecked(!exclusionSet.contains(c.getCourseId()));
    }

    @Override
    public int getItemCount() {
        return calendars.size();
    }

    public ArrayList<String> getExclusions() {
        return new ArrayList<String>(exclusionSet);
    }
}
