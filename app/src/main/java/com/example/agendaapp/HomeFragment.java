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

import com.example.agendaapp.Utils.DateInfo;
import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Utils.Serialize;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    CreateFragment createFragment;

    static Context context;

    Toolbar toolbar;
    FloatingActionButton fab;
    LinearLayout llRoot;

    RecyclerView recyclerView;

    RecyclerView.LayoutManager recyclerViewLayoutManager;
    AssignmentRecyclerAdapter recyclerViewAdapter;

    ItemTouchHelper.Callback callback;
    ItemTouchHelper itemTouchHelper;

    static ArrayList<DateInfo> pDateInfo;
    static ArrayList<DateInfo> uDateInfo;
    static ArrayList<String> pTitles;
    static ArrayList<String> pDescriptions;
    static ArrayList<String> uTitles;
    static ArrayList<String> uDescriptions;
    static ArrayList<Integer> pTypes;
    static ArrayList<Integer> uTypes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.home_toolbar);
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
        ViewGroupCompat.setTransitionGroup(llRoot, true);

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

    private void init(View view) {
        context = getContext();

        initArrays();

        update();

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        recyclerView = (RecyclerView) view.findViewById(R.id.home_recycler_view);
        llRoot = (LinearLayout) view.findViewById(R.id.home_ll_root);

        recyclerViewLayoutManager = new LinearLayoutManager(context);
        setArrayAdapter();

        callback = new ItemMoveCallback((ItemMoveCallback.ItemTouchHelperContract) recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);

        createFragment = new CreateFragment();

        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initArrays() {
        ArrayList[] serialized = (ArrayList[]) Serialize.deserialize(context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);

        if(serialized != null) {
            pTitles = serialized[Utility.SERIALIZATION_P_TITLES];
            pTypes = serialized[Utility.SERIALIZATION_P_SUBJECT];
            pDescriptions = serialized[Utility.SERIALIZATION_P_DESCRIPTION];
            pDateInfo = serialized[Utility.SERIALIZATION_P_DATE_INFO];
            uTitles = serialized[Utility.SERIALIZATION_U_TITLES];
            uTypes = serialized[Utility.SERIALIZATION_U_SUBJECT];
            uDescriptions = serialized[Utility.SERIALIZATION_U_DESCRIPTION];
            uDateInfo = serialized[Utility.SERIALIZATION_U_DATE_INFO];
        } else {
            pDateInfo = new ArrayList<DateInfo>();
            uDateInfo = new ArrayList<DateInfo>();
            pTitles = new ArrayList<String>();
            pDescriptions = new ArrayList<String>();
            uTitles = new ArrayList<String>();
            uDescriptions = new ArrayList<String>();
            pTypes = new ArrayList<Integer>();
            uTypes = new ArrayList<Integer>();
        }
    }

    private void initListeners() {
        fab.setOnClickListener(view -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            transaction.replace(R.id.fragment_container, createFragment);
            transaction.addToBackStack(Utility.CREATE_FRAGMENT);
            transaction.commit();
        });
    }

    private void update() {
        for(int i = 0; i < uTitles.size(); i++) {
            if(Utility.inPriorityRange(uDateInfo.get(i), context)) {
                addToPriority(uTitles.get(i), uDescriptions.get(i), uTypes.get(i), uDateInfo.get(i));
                removeFromUpcoming(i + pTitles.size() + 2);
            }
        }
    }

    private void setArrayAdapter() {
        recyclerViewAdapter = new AssignmentRecyclerAdapter(context, pTitles.toArray(new String[pTitles.size()]),
                pDateInfo.toArray(new DateInfo[pDateInfo.size()]), pDescriptions.toArray(new String[pDescriptions.size()]),
                Utility.toIntArray(pTypes), uTitles.toArray(new String[uTitles.size()]), uDateInfo.toArray(new DateInfo[uDateInfo.size()]),
                uDescriptions.toArray(new String[uDescriptions.size()]), Utility.toIntArray(uTypes));
    }

    private void updateArrayAdapter() {
        recyclerViewAdapter.pTitles = pTitles.toArray(new String[pTitles.size()]);
        recyclerViewAdapter.pDateInfos = pDateInfo.toArray(new DateInfo[pDateInfo.size()]);
        recyclerViewAdapter.pDescriptions = pDescriptions.toArray(new String[pDescriptions.size()]);
        recyclerViewAdapter.pTypes = Utility.toIntArray(pTypes);
        recyclerViewAdapter.uTitles = uTitles.toArray(new String[uTitles.size()]);
        recyclerViewAdapter.uDateInfos = uDateInfo.toArray(new DateInfo[uDateInfo.size()]);
        recyclerViewAdapter.uDescriptions = uDescriptions.toArray(new String[uDescriptions.size()]);
        recyclerViewAdapter.uTypes = Utility.toIntArray(uTypes);
    }

    private void save(Bundle bundle) {
        String title = bundle.getString(Utility.EDIT_BUNDLE_TITLE_KEY);
        String subject = bundle.getString(Utility.EDIT_BUNDLE_SUBJECT_KEY);
        String description = bundle.getString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY);
        DateInfo dateInfo = new DateInfo(bundle.getString(Utility.EDIT_BUNDLE_DUE_DATE_KEY),
                bundle.getInt(Utility.EDIT_BUNDLE_DAY_KEY), bundle.getInt(Utility.EDIT_BUNDLE_MONTH_KEY),
                bundle.getInt(Utility.EDIT_BUNDLE_YEAR_KEY));

        boolean priority = bundle.getBoolean(Utility.EDIT_BUNDLE_PRIORITY_KEY);

        int originalPosition = bundle.getInt(Utility.EDIT_BUNDLE_POSITION_KEY, -1);
        boolean createNew = bundle.getBoolean(Utility.EDIT_BUNDLE_CREATE_NEW_KEY, true);

        if(!createNew) {
            if(originalPosition <= pTitles.size()) {
                removeFromPriority(originalPosition);
            } else {
                removeFromUpcoming(originalPosition);
            }
        }

        String[] sArray = getResources().getStringArray(R.array.subject_array);

        int subjectDrawable = 0;

        if(subject.equals(sArray[0])) { // Art
            subjectDrawable = R.drawable.ic_brush_black_24dp;
        } else if(subject.equals(sArray[1])) { // History
            subjectDrawable = R.drawable.ic_history_edu_black_24dp;
        } else if(subject.equals(sArray[2])) { // Language
            subjectDrawable = R.drawable.ic_language_black_24dp;
        } else if(subject.equals(sArray[3])) { // Literature
            subjectDrawable = R.drawable.ic_book_black_24dp;
        } else if(subject.equals(sArray[4])) { // Math
            subjectDrawable = R.drawable.ic_calculate_black_24dp;
        } else if(subject.equals(sArray[5])) { // Music
            subjectDrawable = R.drawable.ic_music_note_black_24dp;
        } else if(subject.equals(sArray[6])) { // Science
            subjectDrawable = R.drawable.ic_science_black_24dp;
        } else if(subject.equals(sArray[7])) { // Other
            subjectDrawable = R.drawable.ic_miscellaneous_services_black_24dp;
        }

        if(priority) {
            addToPriority(title, description, subjectDrawable, dateInfo);
        } else {
            addToUpcoming(title, description, subjectDrawable, dateInfo);
        }

        serializeArrays();

        updateArrayAdapter();
    }

    private void addToPriority(String title, String description, int subjectDrawable, DateInfo dateInfo) {
        for(int i = 0; i < pDateInfo.size(); i++) {
            DateInfo fromArray = pDateInfo.get(i);

            boolean moved = false;

            if(i != pDateInfo.size() - 1) {
                moved = Utility.compareDates(fromArray, pDateInfo.get(i + 1)) == Utility.FURTHER;
            }

            if(!moved && Utility.compareDates(fromArray, dateInfo) == Utility.FURTHER) {
                pDateInfo.add(i, dateInfo);

                pTitles.add(i, title);
                pDescriptions.add(i, description);
                pTypes.add(i, subjectDrawable);

                recyclerView.scrollToPosition(i + 1);

                return;
            }
        }

        pDateInfo.add(dateInfo);
        pTitles.add(title);
        pDescriptions.add(description);
        pTypes.add(subjectDrawable);
    }

    private void addToUpcoming(String title, String description, int subjectDrawable, DateInfo dateInfo) {
        for(int i = 0; i < uDateInfo.size(); i++) {
            DateInfo fromArray = uDateInfo.get(i);

            boolean moved = false;

            if(i != uDateInfo.size() - 1) {
                moved = Utility.compareDates(fromArray, uDateInfo.get(i + 1)) == Utility.FURTHER;
            }

            if(!moved && Utility.compareDates(fromArray, dateInfo) == Utility.FURTHER) {
                uDateInfo.add(i, dateInfo);

                uTitles.add(i, title);
                uDescriptions.add(i, description);
                uTypes.add(i, subjectDrawable);

                recyclerView.scrollToPosition(i + pTitles.size() + 2);

                return;
            }
        }

        uDateInfo.add(dateInfo);
        uTitles.add(title);
        uDescriptions.add(description);
        uTypes.add(subjectDrawable);
    }

    private void removeFromPriority(int overallPosition) {
        pDateInfo.remove(overallPosition - 1);
        pTitles.remove(overallPosition - 1);
        pDescriptions.remove(overallPosition - 1);
        pTypes.remove(overallPosition - 1);
    }

    private void removeFromUpcoming(int overallPosition) {
        uDateInfo.remove(overallPosition - pTitles.size() - 2);
        uTitles.remove(overallPosition - pTitles.size() - 2);
        uDescriptions.remove(overallPosition - pTitles.size() - 2);
        uTypes.remove(overallPosition - pTitles.size() - 2);
    }

    public static void serializeArrays() {
        ArrayList[] serialize = new ArrayList[10];
        serialize[Utility.SERIALIZATION_P_TITLES] = pTitles;
        serialize[Utility.SERIALIZATION_P_SUBJECT] = pTypes;
        serialize[Utility.SERIALIZATION_P_DESCRIPTION] = pDescriptions;
        serialize[Utility.SERIALIZATION_P_DATE_INFO] = pDateInfo;
        serialize[Utility.SERIALIZATION_U_TITLES] = uTitles;
        serialize[Utility.SERIALIZATION_U_SUBJECT] = uTypes;
        serialize[Utility.SERIALIZATION_U_DESCRIPTION] = uDescriptions;
        serialize[Utility.SERIALIZATION_U_DATE_INFO] = uDateInfo;

        Serialize.serialize(serialize, context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);
    }

    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            save(bundle);
        }
    }
}