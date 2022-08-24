package com.joshuaau.plantlet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.RecyclerAdapters.OptionsRecyclerAdapter;
import com.joshuaau.plantlet.Utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class OptionsFragment extends Fragment {

    private final static String SAVE_PLATFORM = "Save Platform";
    private final static String SAVE_COURSE_MAP = "Save Course Map";

    private Context context;

    private Platform platform;

    private HashMap<String, Course> courseMap;
    private ArrayList<String> exclusions;

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.cal_options_toolbar);
        toolbar.setTitle(getString(R.string.options_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        init(view, onSavedInstance);
        initCallbacks(view);

        return view;
    }

    private void init(View itemView, Bundle onSavedInstance) {
        context = requireContext();

        platform = (Platform) getArguments().getSerializable(SAVE_PLATFORM);

        courseMap = new HashMap<String, Course>();
        exclusions = platform.getExclusions();

        if(onSavedInstance != null)
            courseMap = (HashMap<String, Course>) onSavedInstance.getSerializable(SAVE_COURSE_MAP);

        recyclerView = (RecyclerView) itemView.findViewById(R.id.cal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(onSavedInstance == null) {
            platform.getCourses((courses, withExclusions) -> {
                courseMap = (HashMap<String, Course>) courses;
                recyclerView.setAdapter(new OptionsRecyclerAdapter(courseMap, exclusions));

                if(courseMap.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    itemView.findViewById(R.id.op_tv_none).setVisibility(View.VISIBLE);
                }
            });
        } else {
            recyclerView.setAdapter(new OptionsRecyclerAdapter(courseMap, exclusions));

            if(courseMap.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                itemView.findViewById(R.id.op_tv_none).setVisibility(View.VISIBLE);
            }
        }
    }

    private void initCallbacks(View itemView) {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_options, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case android.R.id.home :
                        FragmentTransaction homeTransaction = getParentFragmentManager().beginTransaction();
                        homeTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                        homeTransaction.replace(R.id.fragment_container, new ImportFragment());
                        homeTransaction.addToBackStack(Utility.CAL_OPTIONS_FRAGMENT);
                        homeTransaction.commit();

                        return true;
                    case R.id.options_done :
                        updateExclusions();
                        ImportFragment.savePlatforms(context);

                        FragmentTransaction doneTransaction = getParentFragmentManager().beginTransaction();
                        doneTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        doneTransaction.replace(R.id.fragment_container, new ImportFragment());
                        doneTransaction.addToBackStack(Utility.CAL_OPTIONS_FRAGMENT);
                        doneTransaction.commit();

                        return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Updates the exclusion list with the one from the adapter
     */
    public void updateExclusions() {
        platform.setExclusions(((OptionsRecyclerAdapter) recyclerView.getAdapter()).getExclusions());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        updateExclusions();

        outState.putSerializable(SAVE_COURSE_MAP, courseMap);
    }

    /**
     * Creates a new instance of the OptionsFragment
     * @param platform The platform
     * @return Returns an OptionsFragment instance
     */
    public static OptionsFragment newInstance(Platform platform) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SAVE_PLATFORM, platform);

        OptionsFragment fragment = new OptionsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
