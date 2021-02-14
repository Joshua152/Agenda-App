/**
 * This is the fragment that holds the RecyclerView.
 *
 * @uthor Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Utils.Assignment;
import com.example.agendaapp.Utils.DateInfo;
import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Utils.ListModerator;
import com.example.agendaapp.Utils.Serialize;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private Context context;

    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private AssignmentRecyclerAdapter recyclerViewAdapter;

    // ArrayList of priority assignments
    public static ArrayList<Assignment> priority;
    // ArrayList of upcoming assignments
    public static ArrayList<Assignment> upcoming;
    // ListModerator for the priority and upcoming array s
    public static ListModerator<Assignment> assignmentModerator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.home_toolbar);
        toolbar.setTitle(getString(R.string.home_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        getParentFragmentManager().setFragmentResultListener(Utility.HOME_RESULT_KEY, this,
                new ResultListener());

        init(view);

        initListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle onSavedInstance) {
        ViewGroupCompat.setTransitionGroup(view.findViewById(R.id.home_ll_root), true);

        setExitTransition(new MaterialElevationScale(false));
        setReenterTransition(new MaterialElevationScale(true));

        postponeEnterTransition();

        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();

                return true;
            }
        });
    }

    /**
     * Inits the views
     * @param view Inflated Fragment
     */
    private void init(View view) {
        context = getContext();

        initArrays();

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        recyclerView = (RecyclerView) view.findViewById(R.id.home_recycler_view);

        setArrayAdapter();

        ItemTouchHelper.Callback callback = new ItemMoveCallback((ItemMoveCallback.ItemTouchHelperContract) recyclerViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recyclerViewAdapter);

        update();
    }

    /**
     * Inits the priority and upcoming arrays
     */
    private void initArrays() {
        ArrayList[] serialized = (ArrayList[]) Serialize.deserialize(context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);

        if(serialized != null) {
            priority = serialized[Utility.SERIALIZATION_PRIORITY];
            upcoming = serialized[Utility.SERIALIZATION_UPCOMING];
        } else {
            priority = new ArrayList<Assignment>();
            upcoming = new ArrayList<Assignment>();
        }

        assignmentModerator = new ListModerator<Assignment>(priority, upcoming);
    }

    /**
     * Initializes the onClickListeners
     */
    private void initListeners() {
        fab.setOnClickListener(view -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            transaction.replace(R.id.fragment_container, new CreateFragment());
            transaction.addToBackStack(Utility.CREATE_FRAGMENT);
            transaction.commit();
        });
    }

    /**
     * Updates the lists
     */
    private void update() {
        // Move from upcoming to priority if necessary
        for(int i = 0; i < upcoming.size(); i++) {
            if(Utility.inPriorityRange(upcoming.get(i).getDateInfo(), context))
                addToPriority(upcoming.get(i));
        }
    }

    /**
     * Sets the array adapter for the RecyclerView
     */
    private void setArrayAdapter() {
        recyclerViewAdapter = new AssignmentRecyclerAdapter(context, priority, upcoming);
    }

    /**
     * Updates the array adapter with new arrays
     */
    private void updateArrayAdapter() {
        recyclerViewAdapter.setArrays(priority, upcoming);
    }

    /**
     * SSerializes the arrays
     * @param bundle The bundle to save from
     */
    private void save(Bundle bundle) {
        Assignment assignment = bundle.getParcelable(Utility.ASSIGNMENT_KEY);

        boolean isPriority = bundle.getBoolean(Utility.PRIORITY_KEY);

        int originalPosition = bundle.getInt(Utility.POSITION_KEY, -1);
        boolean createNew = bundle.getBoolean(Utility.CREATE_NEW_KEY, true);

        System.out.println((assignmentModerator == null) + " " + assignmentModerator.getList(0).size() + " " + createNew + " " + originalPosition);

        System.out.println(priority);

        // If do not need to create a new assignment (assignment is being moved)
        if(!createNew)
            assignmentModerator.removeOverall(originalPosition);

        System.out.println(priority);

        if(isPriority)
            addToPriority(assignment);
        else
            addToUpcoming(assignment);

        Utility.serializeArrays(context, priority, upcoming);

        updateArrayAdapter();
    }

    /**
     * Adds to the priority array
     * @param assignment The assignment to be added to the array
     */
    private void addToPriority(Assignment assignment) {
        for(int i = 0; i < priority.size(); i++) {
            DateInfo fromArray = priority.get(i).getDateInfo();

            boolean moved = false;

            if(i != priority.size() - 1) {
                moved = Utility.compareDates(fromArray, priority.get(i + 1).getDateInfo()) == DateInfo.FURTHER;
            }

            if(!moved && Utility.compareDates(fromArray, assignment.getDateInfo()) == DateInfo.FURTHER) {
                priority.add(i, assignment);

                recyclerView.scrollToPosition(i + 1);

                return;
            }
        }

        priority.add(assignment);
    }

    /**
     * Adds to the upcoming array
     * @param assignment The assignment to be added to the array
     */
    private void addToUpcoming(Assignment assignment) {
        for(int i = 0; i < upcoming.size(); i++) {
            DateInfo fromArray = upcoming.get(i).getDateInfo();

            boolean moved = false;

            if(i != upcoming.size() - 1)
                moved = Utility.compareDates(fromArray, upcoming.get(i + 1).getDateInfo()) == DateInfo.FURTHER;

            if(!moved && Utility.compareDates(fromArray, assignment.getDateInfo()) == DateInfo.FURTHER) {
                upcoming.add(i, assignment);

                recyclerView.scrollToPosition(i + priority.size() + 2);

                return;
            }
        }

        upcoming.add(assignment);
    }

    /*
      For Fragment communication
     */
    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            save(bundle);
        }
    }
}