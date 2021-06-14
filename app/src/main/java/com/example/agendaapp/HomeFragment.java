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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.RecyclerAdapters.AssignmentRecyclerAdapter;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Data.ListModerator;
import com.example.agendaapp.Data.SaveInfo;
import com.example.agendaapp.Data.Serialize;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;
import java.util.List;

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

        init(view, onSavedInstance);

        initListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle onSavedInstance) {
        ViewGroupCompat.setTransitionGroup(view.findViewById(R.id.home_ll_root), true);

        // setExitTransition(new MaterialElevationScale(false)); // can be laggy
        setExitTransition(new Hold());
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
    private void init(View view, Bundle onSavedInstance) {
        context = getContext();

        initArrays(onSavedInstance);

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
    private void initArrays(Bundle onSavedInstance) {
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
            setExitTransition(null);

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
            if(DateUtils.inPriorityRange(context, upcoming.get(i).getDateInfo()))
                addToList(priority, upcoming.get(i));
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
        SaveInfo info = bundle.getParcelable(Utility.SAVE_INFO);

        if(!info.getCreateNew())
            assignmentModerator.removeOverall(info.getPosition());

        if(info.getIsPriority())
            addToList(priority, info.getAssignment());
        else
            addToList(upcoming, info.getAssignment());

        Utility.serializeArrays(context, priority, upcoming);

        updateArrayAdapter();
    }

    /**
     * Adds an assignment to a specified list
     * @param list The list to be added to
     * @param assignment The assignment to add
     */
    public void addToList(List<Assignment> list, Assignment assignment) {
        for(int i = 0; i < list.size(); i++) {
            DateInfo fromArray = list.get(i).getDateInfo();

            boolean moved = false;

            if (i != list.size() - 1)
                moved = DateUtils.compareDates(fromArray, list.get(i + 1).getDateInfo()) == DateInfo.FURTHER;

            if (!moved && DateUtils.compareDates(fromArray, assignment.getDateInfo()) == DateInfo.FURTHER) {
                list.add(i, assignment);

                return;
            }
        }

        list.add(assignment);
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