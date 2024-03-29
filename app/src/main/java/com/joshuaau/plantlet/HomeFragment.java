/**
 * This is the fragment that holds the RecyclerView.
 *
 * @uthor Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.joshuaau.plantlet;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.joshuaau.plantlet.Data.Assignment;
import com.joshuaau.plantlet.Data.DateInfo;
import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.RecyclerAdapters.AssignmentRecyclerAdapter;
import com.joshuaau.plantlet.Utils.Connectivity;
import com.joshuaau.plantlet.Utils.DateUtils;
import com.joshuaau.plantlet.Utils.ItemMoveCallback;
import com.joshuaau.plantlet.Data.ListModerator;
import com.joshuaau.plantlet.Data.SaveInfo;
import com.joshuaau.plantlet.Utils.Serialize;
import com.joshuaau.plantlet.Utils.Utility;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

// TODO: CRASH WHEN CREATING NEW ASSIGNMENT THEN CHANGING PLATFORM SUBJECT

public class HomeFragment extends Fragment {

    private Context context;

    private BottomAppBar bottomAppBar;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AssignmentRecyclerAdapter recyclerViewAdapter;

    // ArrayList of priority assignments
    public static ArrayList<Assignment> priority;
    // ArrayList of upcoming assignments
    public static ArrayList<Assignment> upcoming;
    // ListModerator for the priority and upcoming arrays
    public static ListModerator<Assignment> assignmentModerator;

    public static Connectivity connectivity;
    private Connectivity.ConnectivityListener connectivityListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.home_toolbar);
        toolbar.setTitle(getString(R.string.home_title));

        ((AppCompatActivity) getActivity()).setSupportActionBar((BottomAppBar) view.findViewById(R.id.bottom_app_bar));

        setHasOptionsMenu(true);

        getParentFragmentManager().setFragmentResultListener(Utility.HOME_RESULT_KEY, this,
                new ResultListener());

        init(view, onSavedInstance);

        initListeners();

        getSavedAndUpdate();

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
     * Gets the saved instances (platformList, courseList) and updates from the platforms
     */
    private void getSavedAndUpdate() {
        if(ImportFragment.platforms == null)
            ImportFragment.platforms = ImportFragment.getSavedPlatforms(getContext(), getActivity(), getActivity().getSupportFragmentManager());

        if(CoursesFragment.courseMap == null)
            CoursesFragment.courseMap = CoursesFragment.getSavedCourseList(getContext());
    }

    /**
     * Inits the views
     * @param view Inflated Fragment
     */
    private void init(View view, Bundle onSavedInstance) {
        context = getContext();

        initArrays(onSavedInstance);

        bottomAppBar = (BottomAppBar) view.findViewById(R.id.bottom_app_bar);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        recyclerView = (RecyclerView) view.findViewById(R.id.home_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        setArrayAdapter();

        ItemTouchHelper.Callback callback = new ItemMoveCallback((ItemMoveCallback.ItemTouchHelperContract) recyclerViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recyclerViewAdapter);

        if(connectivity == null)
            connectivity = new Connectivity(context);

        connectivityListener = null;

        updateAssignmentPositions();
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
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.home_courses :
                    setExitTransition(null);
                    
                    FragmentTransaction coursesTransaction = getParentFragmentManager().beginTransaction();
                    coursesTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
                    coursesTransaction.replace(R.id.fragment_container, new CoursesFragment());
                    coursesTransaction.addToBackStack(Utility.CLASSES_FRAGMENT);
                    coursesTransaction.commit();

                    return true;
                case R.id.home_import :
                    setExitTransition(null);

                    FragmentTransaction homeTransaction = getParentFragmentManager().beginTransaction();
                    homeTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
                    homeTransaction.replace(R.id.fragment_container, new ImportFragment());
                    homeTransaction.addToBackStack(Utility.IMPORT_FRAGMENT);
                    homeTransaction.commit();

                    return true;
                case R.id.home_settings :
                    setExitTransition(null);

                    startActivity(new Intent(context, OssLicensesMenuActivity.class));
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.licenses_title));

                    return true;
                case R.id.home_privacy :
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plantlet-app.web.app/privacy"));
                    startActivity(browserIntent);
            }

            return false;
        });

        fab.setOnClickListener(view -> {
            setExitTransition(null);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            transaction.replace(R.id.fragment_container, EditFragment.newInstance(context));
            transaction.addToBackStack(Utility.EDIT_FRAGMENT);
            transaction.commit();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(!Utility.isNetworkAvailable(context))
                Utility.showBasicSnackbar(getActivity(), R.string.error_no_connection);

            List<Platform> signedInPlatforms = ImportFragment.getSignedInPlatforms();

            if(signedInPlatforms.size() == 0) {
                swipeRefreshLayout.setRefreshing(false);

                return;
            }

            AtomicInteger done = new AtomicInteger(0);

            for(Platform p : signedInPlatforms) {
                updateAssignments(p, () -> {
                    if(done.incrementAndGet() == signedInPlatforms.size())
                        swipeRefreshLayout.setRefreshing(false);
                });
            }

            updateAssignmentPositions();
        });

        connectivityListener = new Connectivity.ConnectivityListener() {
            @Override
            public void onAvailable(Network network) {}

            @Override
            public void onLost(Network network) {
                Utility.showBasicSnackbar(getActivity(), R.string.error_no_connection);
            }

            // TODO: REMOVE?
        };

        connectivity.addListener(connectivityListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Updates the lists
     */
    private void updateAssignmentPositions() {
        // Move from upcoming to priority if necessary
        for(int i = 0; i < upcoming.size(); i++) {
            if(DateUtils.inPriorityRange(context, upcoming.get(i).getDateInfo())) {
                int pos = addToList(priority, upcoming.remove(i));
                recyclerViewAdapter.notifyItemMoved(i + 2 + priority.size(), pos);
            }
        }
    }

    /**
     * Checks for new or updated assignments from the platform
     * @param p The platform
     */
    public void updateAssignments(Platform p, CoursesUpdatedListener coursesUpdatedListener) { // TODO: DELETE ASSIGNMENT IF PLATFORM ASSIGNMENT IS DELETED?
        p.getNewAssignments(assignments -> {
            for(Assignment a : assignments) {
                for(int i = 0; i < assignmentModerator.getItemCount(); i++) {
                    if(a.getId().equals(assignmentModerator.get(i).getId())) {
                        int removePos = assignmentModerator.getPosFromNoHeader(i);

                        assignmentModerator.remove(i);
                        recyclerViewAdapter.notifyItemRemoved(removePos);

                        i = assignmentModerator.getItemCount();
                    }
                }

                int pos = 0;

                if(DateUtils.inPriorityRange(context, a.getDateInfo())) {
                    boolean empty = priority.size() == 0;

                    pos = addToList(priority, a) + 1;

                    if(empty)
                        recyclerViewAdapter.notifyItemChanged(0);
                } else {
                    boolean empty = upcoming.size() == 0;

                    pos = addToList(upcoming, a) + priority.size() + 2;

                    if(empty)
                        recyclerViewAdapter.notifyItemChanged(priority.size() + 1);
                }

                recyclerViewAdapter.notifyItemInserted(pos);
            }

            Utility.serializeAssignments(context, priority, upcoming);

            coursesUpdatedListener.onCoursesUpdated();
        });
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
     * Serializes the arrays
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

        Utility.serializeAssignments(context, priority, upcoming);

        updateArrayAdapter();
    }

    /**
     * Adds an assignment to a specified list
     * @param list The list to be added to
     * @param assignment The assignment to add
     * @return Returns the position of the assignment in the list
     */
    public int addToList(List<Assignment> list, Assignment assignment) {
        if(assignment.getDateInfo().getDate().equals(DateUtils.NO_DATE)) {
            list.add(assignment);

            return list.size() - 1;
        }

        for(int i = 0; i < list.size(); i++) {
            DateInfo fromArray = list.get(i).getDateInfo();

            boolean moved = false;

            if (i != list.size() - 1)
                moved = DateUtils.compareDates(fromArray, list.get(i + 1).getDateInfo()) == DateInfo.FURTHER;

            if (!moved && DateUtils.compareDates(fromArray, assignment.getDateInfo()) == DateInfo.FURTHER) {
                list.add(i, assignment);

                return i;
            }
        }

        list.add(assignment);

        return list.size() - 1;
    }

    @Override
    public void onPause() {
        ImportFragment.savePlatforms(context); // TODO: WHY?

        connectivity.removeListener(connectivityListener);

        super.onPause();
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

    /**
     * Interface for when the courses for a platform have been updated
     */
    public interface CoursesUpdatedListener {
        /**
         * Callback for when courses have been updated
         */
        void onCoursesUpdated();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
}